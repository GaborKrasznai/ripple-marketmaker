package marketmaker.services.algorithms;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Calendar;
import java.util.HashMap;

import org.javatuples.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;

import com.ripple.client.Account;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.Issue;
import com.ripple.core.coretypes.PathSet;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.types.known.tx.txns.Payment;

import marketmaker.entities.Channels;

/***
 * 
 * @author rmartins
 *
 */
public class RippexArbitrager implements InitializingBean {

	private static Logger log = LoggerFactory.getLogger(RippexArbitrager.class);

	@Autowired
	private JmsTemplate template;
	@Autowired
	private Account rippleAccount;

	@Value("${ripplemm.arbitrager.resistances}")
	private String resistances;
	@Value("${ripplemm.arbitrager.ratios}")
	private String ratios;
	@Value("${ripplemm.arbitrager.exchanges}")
	private String exchanges;
	@Value("${ripplemm.arbitrager.amounts}")
	private String exchangesAmounts;

	// Setup
	public HashMap<String, Double> listRatios = new HashMap<>();
	public HashMap<String, Double> listResistances = new HashMap<>();
	public HashMap<String, Pair<BigDecimal, BigDecimal>> listExchanges = new HashMap<>();

	// Market data
	public HashMap<String, Object> listOfferBooks = new HashMap<>();
	public HashMap<String, JSONObject> listPathfind = new HashMap<>();
	public HashMap<String, Double> listAmounts = new HashMap<>();

	@Value("${ripplemm.arbitrager.createoffers.enabled}")
	private boolean arbitrageCreateOffersEnabled;

	@Value("${ripplemm.arbitrager.slippageMaxAmount}")
	private Double slippageMaxAmount;

	@Override
	public void afterPropertiesSet() throws Exception {
		parseExchangesFromProperties(exchanges);
		parseRatiosFromProperties(ratios);
		parseExchangeAmountsFromProperties(exchangesAmounts);
		parseResistancesFromProperties(resistances);
	}

	public void parseExchangesFromProperties(String value) {
		listExchanges = new HashMap<>();
		String[] e = value.split(",");
		for (int i = 0; i < e.length; i++) {
			listExchanges.put(e[i], null);
			if (!e[i].startsWith("#")) {
				String[] paths = e[i].split("/");
				listPathfind.put(paths[0] + "/" + paths[1], null);
				listPathfind.put(paths[2] + "/" + paths[3], null);
			}
		}
		log.info("Arbitrager Exchange List: " + listExchanges.toString());
	}

	public void parseRatiosFromProperties(String value) {
		listRatios = new HashMap<>();
		try {
			String[] r = value.split(",");
			for (int i = 0; i < r.length; i++) {
				String[] values = r[i].split(";");
				listRatios.put(values[0] + ";" + values[1], Double.valueOf(values[2]));
			}
		} catch (Exception ex) {
			log.error("Error parsing Ratios", ex);
		}
		log.info("Arbitrager Ratio List: " + listRatios.toString());
	}

	public void parseResistancesFromProperties(String value) {
		listResistances = new HashMap<>();
		try {
			String[] r = value.split(",");
			for (int i = 0; i < r.length; i++) {
				String[] values = r[i].split(";");
				listResistances.put(values[0] + ";" + values[1], Double.valueOf(values[2]));
			}
		} catch (Exception ex) {
			log.error("Error parsing Resistances", ex);
		}
		log.info("Arbitrager Resistances List: " + listResistances.toString());
	}

	public void parseExchangeAmountsFromProperties(String value) {
		try {
			String[] r = value.split(",");
			for (int i = 0; i < r.length; i++) {
				String[] values = r[i].split(";");
				listAmounts.put(values[0], Double.parseDouble(values[1]));
			}
		} catch (Exception ex) {
			log.error("Error parsing Ratios", ex);
		}
	}

	@JmsListener(destination = "pathfind_pair")
	public void processPathfind(String message) throws Exception {
		JSONObject json = new JSONObject(message);
		log.info("Pathfind : " + json.toString());

		listPathfind.put(json.getString("exchange"), json);
		arbitrageBidAskFromBookAndPathfind(listExchanges, listOfferBooks, listPathfind);
	}

	@JmsListener(destination = "bitfinex_book")
	public void onMessageBitfinexOrderBook(String message) {
		JSONObject json = new JSONObject(message);
		listOfferBooks.put("#BITFINEX", json);
	}

	@JmsListener(destination = "bitstamp_orderbook")
	public void onMessageBitstampOrderBook(String message) {
		JSONObject json = new JSONObject(message);
		listOfferBooks.put("#BITSTAMP", json);
	}

	public JSONObject findPathfindFromBaseAssetCurrency(JSONArray alts, Issue sourceIssue) throws JSONException {
		int altsLength = alts.length();
		for (int x = 0; x < altsLength; x++) {
			JSONObject j = alts.getJSONObject(x);
			JSONObject sourceAmount = j.optJSONObject("source_amount");

			if (sourceAmount != null) {
				String currency = sourceAmount.getString("currency");
				if (currency.equals(sourceIssue.currency().toString())) {
					return j;
				}
			}
		}
		return null;

	}

	public HashMap<String, JSONObject> calculateIncentives(HashMap<String, Pair<BigDecimal, BigDecimal>> exchanges) {

		HashMap<String, JSONObject> list = new HashMap<>();
		for (String keyM : exchanges.keySet()) {
			for (String keyN : exchanges.keySet()) {
				if (keyM.equals(keyN))
					continue;

				if (list.containsKey(keyN + ";" + keyM))
					continue;

				JSONObject result = new JSONObject();

				Pair<BigDecimal, BigDecimal> values_M = exchanges.get(keyM);
				Pair<BigDecimal, BigDecimal> values_N = exchanges.get(keyN);

				if (!listRatios.containsKey(keyM + ";" + keyN)) {
					log.warn("Cannot find Ratio for " + keyM + ";" + keyN);
				}

				double Ratio_MN = listRatios.get(keyM + ";" + keyN);
				if (values_M.getValue0().doubleValue() == 0 || values_M.getValue1().doubleValue() == 0) {
					return null;
				}

				if (values_N.getValue0().doubleValue() == 0 || values_N.getValue1().doubleValue() == 0) {
					return null;
				}

				if (values_M.getValue0().doubleValue() == 0 || values_M.getValue1().doubleValue() == 0) {
					return null;
				}

				if (values_N.getValue0().doubleValue() == 0 || values_N.getValue1().doubleValue() == 0) {
					return null;
				}

				BigDecimal value = values_M.getValue1().multiply(BigDecimal.valueOf(Ratio_MN));
				BigDecimal IncMN = (value.subtract(values_N.getValue0()).divide(values_M.getValue0(),
						MathContext.DECIMAL64));

				BigDecimal value1 = values_M.getValue0().multiply(BigDecimal.valueOf(Ratio_MN));
				BigDecimal IncNM = (values_N.getValue1().subtract(value1).divide(values_N.getValue1(),
						MathContext.DECIMAL64));

				result.put("Opp_MN", false);
				if (IncMN.doubleValue() > listResistances.get(keyM + ";" + keyN)) {
					log.info("Opp_MN is true " + IncMN.doubleValue() + " > " + listResistances.get(keyN + ";" + keyM));
					result.put("Opp_MN", true);
				}

				result.put("Opp_NM", false);
				if (IncNM.doubleValue() > listResistances.get(keyN + ";" + keyM)) {
					log.info("Opp_NM is true " + IncNM.doubleValue() + " > " + listResistances.get(keyN + ";" + keyM));
					result.put("Opp_NM", true);
				}

				result.put("keyM", keyM);
				result.put("keyN", keyN);
				result.put("ratio_MN", Ratio_MN);
				result.put("incMN", IncMN);
				result.put("incNM", IncNM);
				result.put("incMN", IncMN);
				result.put("incNM", IncNM);
				result.put("value_M", values_M);
				result.put("value_N", values_N);

				list.put(keyM + ";" + keyN, result);
			}
		}

		return list;
	}

	public JSONObject arbitrageBidAskFromBookAndPathfind(HashMap<String, Pair<BigDecimal, BigDecimal>> exchanges,
			HashMap<String, Object> offerBooks, HashMap<String, JSONObject> pathfinds) throws Exception {

		HashMap<String, Pair<BigDecimal, BigDecimal>> list = new HashMap<>();
		BigDecimal ask = BigDecimal.ZERO;
		BigDecimal bid = BigDecimal.ZERO;
		for (String key : exchanges.keySet()) {
			if (key.startsWith("#BITSTAMP")) {
				if (offerBooks.containsKey(key)) {
					// TODO sum orders using deepness
					JSONObject foxbitOfferBook = (JSONObject) listOfferBooks.get(key);
					ask = BigDecimal.valueOf(foxbitOfferBook.getJSONArray("asks").getJSONArray(0).getDouble(0));
					bid = BigDecimal.valueOf(foxbitOfferBook.getJSONArray("bids").getJSONArray(0).getDouble(0));
				}
			} else if (key.startsWith("#BITFINEX")) {
				if (offerBooks.containsKey(key)) {
					// TODO sum orders using deepness
					JSONObject foxbitOfferBook = (JSONObject) listOfferBooks.get(key);
					ask = BigDecimal.valueOf(foxbitOfferBook.getJSONArray("asks").getJSONObject(0).getDouble("price"));
					bid = BigDecimal.valueOf(foxbitOfferBook.getJSONArray("bids").getJSONObject(0).getDouble("price"));
				}
			} else {
				JSONObject exchange = (JSONObject) pathfinds.get(key);
				if (exchange != null) {
					ask = BigDecimal.valueOf(exchange.getDouble("ask"));
					bid = BigDecimal.valueOf(exchange.getDouble("bid"));
				}
			}

			log.info("Exchange: " + key + " ASK: " + ask + " BID: " + bid);
			list.put(key, new Pair<BigDecimal, BigDecimal>(ask, bid));
		}

		listExchanges = list;

		JSONObject jsonResult = new JSONObject();
		// exchanges
		JSONArray jsonExchanges = new JSONArray();
		for (String key : listExchanges.keySet())

		{
			JSONObject exchange = new JSONObject();
			exchange.put("key", key);
			exchange.put("ask", listExchanges.get(key).getValue0());
			exchange.put("bid", listExchanges.get(key).getValue1());
			if (!key.startsWith("#")) {
				JSONObject pathfind = (JSONObject) pathfinds.get(key);
				exchange.put("pathfind", pathfind);
			}
			jsonExchanges.put(exchange);
		}

		// incentives
		HashMap<String, JSONObject> listIncentives = calculateIncentives(list);
		JSONArray jsonIncentives = new JSONArray();
		for (String k : listIncentives.keySet())

		{
			jsonIncentives.put(listIncentives.get(k));
		}
		jsonResult.put("resistance", resistances);
		jsonResult.put("ratios", ratios);
		jsonResult.put("exchanges", exchanges);
		// jsonResult.put("deepness", String.format("%.12f", deepness));
		jsonResult.put("exchanges", jsonExchanges);
		jsonResult.put("incentives", jsonIncentives);
		jsonResult.put("timestamp", Calendar.getInstance().getTimeInMillis());

		log.info(jsonResult.toString());
		if (template != null)

		{
			template.convertAndSend(Channels.ARBITRAGER, jsonResult.toString());
		}

		checkAndCreateOffers(jsonResult);
		return jsonResult;

	}

	public void checkAndCreateOffers(JSONObject json) throws Exception {
		// TODO change it
		JSONObject jsonIncentive = json.getJSONArray("incentives").getJSONObject(0);
		boolean Opn_MN = jsonIncentive.getBoolean("Opp_MN");
		boolean Opn_NM = jsonIncentive.getBoolean("Opp_NM");

		String keyM = jsonIncentive.getString("keyM");
		String keyN = jsonIncentive.getString("keyN");

		JSONArray exchanges = json.getJSONArray("exchanges");
		JSONObject exchangeM = null;
		JSONObject exchangeN = null;
		for (int x = 0; x < exchanges.length(); x++) {
			if (keyM.equals(exchanges.getJSONObject(x).get("key"))) {
				exchangeM = exchanges.getJSONObject(x);
			} else if (keyN.equals(exchanges.getJSONObject(x).get("key"))) {
				exchangeN = exchanges.getJSONObject(x);
			}
		}
		if (Opn_MN) {
			// sell btc in M
			if (!exchangeN.getString("key").startsWith("#")) {
				createRipplePaymentSell(exchangeN);
			} else if (exchangeM.getString("key").startsWith("#")) {
				createRipplePaymentSell(exchangeM);
			}

			// buy btc in N
			if (exchangeN.getString("key").startsWith("#")) {
				Double deepness = listAmounts.get(exchangeN.getString("key"));
				Double price = exchangeN.getDouble("ask");
				Double limit_price = price;
				createBitstampOrder(deepness, price, limit_price, true);
			} else if (exchangeM.getString("key").startsWith("#")) {
				Double deepness = listAmounts.get(exchangeM.getString("key"));
				Double price = exchangeM.getDouble("ask");
				Double limit_price = price;
				createBitstampOrder(deepness, price, limit_price, true);
			}
		}

		if (Opn_NM) {
			// buy btc in M
			if (!exchangeN.getString("key").startsWith("#")) {
				createRipplePaymentBuy(exchangeN);
			} else if (exchangeM.getString("key").startsWith("#")) {
				createRipplePaymentBuy(exchangeM);
			}

			// sell btc in N
			if (exchangeN.getString("key").startsWith("#")) {
				Double deepness = listAmounts.get(exchangeN.getString("key"));
				Double price = exchangeN.getDouble("bid");
				Double limit_price = price * slippageMaxAmount;
				createBitstampOrder(deepness, price, limit_price, false);
			} else if (exchangeM.getString("key").startsWith("#")) {
				Double deepness = listAmounts.get(exchangeM.getString("key"));
				Double price = exchangeM.getDouble("bid");
				Double limit_price = price * slippageMaxAmount - 1;
				createBitstampOrder(deepness, price, limit_price, false);
			}
		}
	}

	private void createBitstampOrder(Double deepness2, Double price, Double limit_price, boolean isBuy)
			throws Exception {
		log.info("Create Bitstamp Buy : " + isBuy + " offer Amount: " + deepness2 + " Price: " + price
				+ " Limit Price: " + limit_price);
		JSONObject json = new JSONObject();
		json.put("deepness", String.format("%.12f", deepness2));
		json.put("price", String.format("%.12f", price));
		json.put("limit_price", String.format("%.12f", limit_price));
		json.put("isBuy", isBuy);
		template.convertAndSend(Channels.BITSTAMP_OFFERCREATE, json.toString());
	}

	public void createRipplePaymentSell(JSONObject exchange) {
		String pathfindKey = "pathfind2";
		JSONObject p = exchange.getJSONObject("pathfind");
		JSONObject pathfind = p.getJSONObject(pathfindKey);
		JSONObject result = pathfind.getJSONObject("result");
		JSONObject destination_amount = result.getJSONObject("destination_amount");
		JSONObject alternative = pathfind.getJSONObject("alternative");
		JSONArray paths = alternative.getJSONArray("paths_computed");

		Payment payment = new Payment();
		payment.destination(rippleAccount.id());
		payment.account(rippleAccount.id());

		payment.paths(PathSet.translate.fromJSONArray(paths));
		payment.amount(Amount.translate.fromJSONObject(destination_amount));

		JSONObject deliverMin = result.getJSONObject("destination_amount");
		Double deliverMin_value = p.getDouble("bid") * listAmounts.get(p.getString("baseIssue"));
		deliverMin.put("value", String.format("%.12f", deliverMin_value));
		payment.deliverMin(Amount.translate.fromJSONObject(deliverMin));

		payment.flags(new UInt32(131072));

		JSONObject sendMax = alternative.getJSONObject("source_amount");
		Double sendMax_value = listAmounts.get(p.getString("baseIssue")) * slippageMaxAmount;
		sendMax.put("value", String.format("%.12f", sendMax_value));
		payment.sendMax(Amount.translate.fromJSONObject(sendMax));

		log.info("Create Ripple Payment Sell " + pathfindKey + ": " + payment.prettyJSON());
		template.convertAndSend(Channels.PAYMENT_CREATE, payment.toJSON().toString());
	}

	public void createRipplePaymentBuy(JSONObject exchange) {
		String pathfindKey = "pathfind1";
		JSONObject p = exchange.getJSONObject("pathfind");
		JSONObject pathfind = p.getJSONObject(pathfindKey);
		JSONObject result = pathfind.getJSONObject("result");
		JSONObject destination_amount = result.getJSONObject("destination_amount");

		JSONObject alternative = pathfind.getJSONObject("alternative");
		JSONArray paths = alternative.getJSONArray("paths_computed");

		Amount amount = Amount.translate.fromJSONObject(alternative.getJSONObject("source_amount"));
		Amount maxAmount = amount.multiply(slippageMaxAmount);

		Payment payment = new Payment();

		payment.destination(rippleAccount.id());
		payment.account(rippleAccount.id());

		payment.paths(PathSet.translate.fromJSONArray(paths));
		payment.amount(Amount.translate.fromJSONObject(destination_amount));
		payment.sendMax(maxAmount);

		log.info("Create Ripple Payment Buy " + pathfindKey + ": " + payment.prettyJSON());
		template.convertAndSend(Channels.PAYMENT_CREATE, payment.toJSON().toString());
	}
}