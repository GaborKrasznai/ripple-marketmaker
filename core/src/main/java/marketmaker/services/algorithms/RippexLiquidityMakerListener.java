package marketmaker.services.algorithms;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.javatuples.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ripple.bouncycastle.crypto.InvalidCipherTextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;

import com.ripple.client.Account;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.Issue;
import com.ripple.core.types.known.sle.entries.Offer;
import com.ripple.core.types.known.tx.txns.OfferCreate;

import bsh.Interpreter;
import marketmaker.entities.Channels;
import marketmaker.services.ripple.BaseRippleClient;
import marketmaker.services.ripple.RippleAccountOffersPublisher;

/**
 * Created by rmartins on 5/28/15.
 */
public class RippexLiquidityMakerListener extends BaseRippleClient implements InitializingBean {

	private static Logger log = LoggerFactory.getLogger(RippexLiquidityMakerListener.class);

	@Autowired
	private JmsTemplate template;
	
	private boolean enableOpportunityTaker = false;
	private boolean cancelAllAccountOffersOnStart = false;
	private String scriptsDirectory = "scripts/";
	private String pathFrom;
	private String pathTo;
	private String baseAsset;
	private String counterAsset;
	private String refAsset;
	private BigDecimal refCost;
	private BigDecimal baseAmount;
	private BigDecimal marginAsk;
	private BigDecimal marginBid;
	private BigDecimal degreeAsk;
	private BigDecimal degreeBid;
	private BigDecimal maxOpenAsks;
	private BigDecimal maxOpenBids;
	private BigDecimal baseExpo;
	private BigDecimal counterExpo;
	private BigDecimal slippage;
	private BigDecimal refCostMargin;

	private AtomicInteger countCreateBids = new AtomicInteger();
	private AtomicInteger countCreateAsks = new AtomicInteger();
	private AtomicInteger countOffers = new AtomicInteger();

	private JSONObject pathfind;
	private JSONObject offerBook;
	private JSONObject accountBalance;
	private JSONObject instruments;
	private JSONObject accountOffers;

	private boolean isRunning = false;

	private void refreshSetup() {
		try {
			this.enableOpportunityTaker = avalancheSetup().isEnableOpportunityTaker();
			this.cancelAllAccountOffersOnStart = avalancheSetup().isCancelAllAccountOffersOnStart();
			this.pathFrom = avalancheSetup().getPathFrom();
			this.pathTo = avalancheSetup().getPathTo();
			this.baseAsset = avalancheSetup().getBaseAsset();
			this.counterAsset = avalancheSetup().getCounterAsset();
			this.refAsset = avalancheSetup().getRefAsset();
			this.refCost = avalancheSetup().getRefCost();
			this.baseAmount = avalancheSetup().getBaseAmount();
			this.marginAsk = avalancheSetup().getMarginAsk();
			this.marginBid = avalancheSetup().getMarginBid();
			this.degreeAsk = avalancheSetup().getDegreeAsk();
			this.degreeBid = avalancheSetup().getDegreeBid();
			this.maxOpenAsks = avalancheSetup().getMaxOpenAsks();
			this.maxOpenBids = avalancheSetup().getMaxOpenBids();
			this.baseExpo = avalancheSetup().getBaseExpo();
			this.counterExpo = avalancheSetup().getCounterExpo();
			this.slippage = avalancheSetup().getSlippage();
			this.refCostMargin = avalancheSetup().getRefCostMargin();
			// this.liveFeedEndpoint = json.getString("liveFeedEndpoint");
			// this.liveFeedCurrencyPair =
			// json.getString("liveFeedCurrencyPair");
			// this.liveFeedEnabled = json.getBoolean("liveFeedEnabled");
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}

	}

	@JmsListener(destination = "instruments")
	public void processInstruments(String message) {
		JSONObject json = new JSONObject(message);
		log.info("Instruments: " + json.toString());
		if (json.has("refCost")) {
			this.refCost = new BigDecimal(json.getString("refCost"), MathContext.DECIMAL64);
			log.info("Update refCost " + this.refCost);
			createContextAndExecute();
		}
	}

	@JmsListener(destination = "pathfind")
	public void processPathfind(String message) throws JSONException {
		JSONObject json = new JSONObject(message);
		this.pathfind = json;
		createContextAndExecute();
	}

	@JmsListener(destination = "account_balance")
	public void processAccountBalance(String message) throws JSONException {
		JSONObject json = new JSONObject(message);
		this.accountBalance = json;
		createContextAndExecute();
	}

	@JmsListener(destination = "account_offers")
	public void processAccountOffers(String message) throws JSONException {
		JSONObject json = new JSONObject(message);
		this.accountOffers = json;
		JSONObject count = accountOffers.getJSONObject("count");
		this.countCreateAsks.set(count.getInt("countOpenAsks"));
		this.countCreateBids.set(count.getInt("countOpenBids"));
		this.countOffers.set(count.getInt("countOffers"));
		log.info("processAccountOffers countCreateAsks " + countCreateAsks + " countCreateBids " + countCreateBids);
		createContextAndExecute();
	}

	@JmsListener(destination = "offerbook")
	public void processAccountOfferBook(String message) throws JSONException {
		JSONObject json = new JSONObject(message);
		this.offerBook = json;
		createContextAndExecute();
	}

	public static Pair<Double, Double> calculateRange(double amount, double slippage, int index) {
		// calculate range RangeAskN = [VAskN*(1-Slippage*N),
		// VAskN*(1+Slippage*N)]
		double min = amount * (1 - (slippage * index));
		double max = amount * (1 + (slippage * index));
		Pair<Double, Double> range = new Pair<>(min, max);
		return range;
	}

	private boolean validateContextVariables() {

		if (this.pathfind != null && this.accountBalance != null && this.accountOffers != null && this.offerBook != null
				&& this.offerBook.optJSONArray("offersAsks") != null
				&& this.offerBook.optJSONArray("offersBids") != null
				&& this.offerBook.optJSONArray("offersAsks").length() > 0
				&& this.offerBook.optJSONArray("offersBids").length() > 0) {

			if (!this.pathfind.getBoolean("full_reply")) {
				return false;
			}

			return true;
		}

		return false;
	}

	private void createContextAndExecute() {
		refreshSetup();
		if (!validateContextVariables()) {
			return;
		}

		if (isRunning) {
			return;
		}
		isRunning = true;

		log.info("Ripple Liquidity Maker Start ...");
		try {
			// MARKET DATA CALCULATIONS
			double baseBalance = findAccountBalanceFromCurrency(accountBalance, Issue.fromString(baseAsset))
					.doubleValue();
			double counterBalance = findAccountBalanceFromCurrency(accountBalance, Issue.fromString(counterAsset))
					.doubleValue();
			double baseAmountCost = findPathfindFromRefAsset(pathfind, Issue.fromString(refAsset),
					Issue.fromString(baseAsset)).doubleValue();
			double baseRefCost = baseAmountCost / baseAmount.doubleValue();
			double baseCost = baseRefCost * (refCost.multiply(refCostMargin)).doubleValue();
			double takerGetsAsk = baseBalance * baseExpo.doubleValue();
			double takerGetsBid = counterBalance * counterExpo.doubleValue();
			Offer offerBid = (Offer) Offer.fromJSONObject(offerBook.optJSONArray("offersBids").getJSONObject(0));
			Offer offerAsk = (Offer) Offer.fromJSONObject(offerBook.optJSONArray("offersAsks").getJSONObject(0));
			double bestAsk = offerAsk.askQuality().doubleValue();
			double bestBid = offerBid.bidQuality().doubleValue();
			double spreadAsk = baseCost - (bestAsk / baseCost);
			double spreadBid = baseCost - (bestBid / baseCost);

			log.info("accountBalance: " + accountBalance.toString());
			log.info("accountOffers: " + accountOffers.toString());
			log.info("offerBook: " + offerBook.toString());

			Interpreter interpreter = new Interpreter();
			interpreter.set("baseBalance", baseBalance);
			interpreter.set("counterBalance", counterBalance);
			interpreter.set("pathfind", this.pathfind);
			interpreter.set("instruments", this.instruments);
			interpreter.set("accountBalance", this.accountBalance);
			interpreter.set("accountOffers", this.accountOffers);
			interpreter.set("offerBook", this.offerBook);
			interpreter.set("log", log);
			interpreter.set("baseCost", baseCost);
			interpreter.set("pathFrom", pathFrom);
			interpreter.set("pathTo", pathTo);
			interpreter.set("baseAsset", baseAsset);
			interpreter.set("counterAsset", counterAsset);
			interpreter.set("refAsset", refAsset);
			interpreter.set("refCost", refCost);
			interpreter.set("baseAmount", baseAmount);
			interpreter.set("marginAsk", marginAsk);
			interpreter.set("marginBid", marginBid);
			interpreter.set("degreeAsk", degreeAsk);
			interpreter.set("degreeBid", degreeBid);
			interpreter.set("maxOpenAsks", maxOpenAsks);
			interpreter.set("maxOpenBids", maxOpenBids);
			interpreter.set("baseExpo", baseExpo);
			interpreter.set("counterExpo", counterExpo);
			interpreter.set("countOffers", countOffers);
			interpreter.set("baseAmountCost", baseAmountCost);
			interpreter.set("baseRefCost", baseRefCost);
			interpreter.set("slippage", slippage);
			// interpreter.set("account", account());
			// interpreter.set("transactionManager", transactionManager);

			FileReader reader = new FileReader(scriptsDirectory.concat("RippexLiquidityMaker.bsh"));
			interpreter.eval(reader);

			List listBids = (List) interpreter.get("listBids");
			List listAsks = (List) interpreter.get("listAsks");
			List rangeBids = (List) interpreter.get("rangeBids");
			List rangeAsks = (List) interpreter.get("rangeAsks");

			calculateOpportunitiesAvalanche(baseCost, baseBalance, baseAmountCost, counterBalance, bestAsk, bestBid,
					takerGetsAsk, takerGetsBid, baseRefCost, spreadAsk, spreadBid, listAsks, listBids, rangeAsks,
					rangeBids);

			this.pathfind = null;
			this.accountBalance = null;
			this.offerBook = null;

			log.info("Ripple Liquidity Maker End ...");
			isRunning = false;

		} catch (Exception e) {
			e.printStackTrace();
			isRunning = false;
			log.error(e.getMessage(), e);
		}
	}

	public LinkedList<OfferCreate> calculateOpportunitiesAvalanche(double baseCost, double baseBalance,
			double baseAmountCost, double counterBalance, double bestAsk, double bestBid, double takerGetsAsk,
			double takerGetsBid, double baseRefCost, double spreadAsk, double spreadBid, List<Double> listAsks,
			List<Double> listBids, List<Pair<Double, Double>> rangeAsks, List<Pair<Double, Double>> rangeBids)
			throws JSONException, InvalidCipherTextException, IOException {

		List<Double> beforeListAsks = listAsks;
		List<Double> beforeListBids = listBids;
		log.info("beforeListAsks " + beforeListAsks);
		log.info("beforeListBids " + beforeListBids);
		log.info("rangeAsks " + rangeAsks);
		log.info("rangeBids " + rangeBids);

		JSONArray offers = accountOffers.getJSONObject("result").getJSONArray("offers");

		// Get rangeCount
		Pair<int[], int[]> rangeCount = cancelAvalancheOffers(account(), offers, baseBalance, baseAsset,
				baseAmountCost, counterBalance, rangeAsks, counterBalance, rangeBids);

		int[] rangeCountAsks = rangeCount.getValue0();
		int[] rangeCounBids = rangeCount.getValue1();

		// filtrar os ranges com as ordens abertas no offerbook
		// o que sobrou do cancelAvalanche
		List<Double> listAsksResult = new ArrayList<Double>();
		List<Double> listBidsResult = new ArrayList<Double>();

		for (int i = 0; i < offers.length(); i++) {
			Pair<Double, Double> takerValues = getOfferTakerValuesFromJSONObject(offers.getJSONObject(i));
			double takerPays = takerValues.getValue0();
			double takerGets = takerValues.getValue1();

			if (RippleAccountOffersPublisher.isOfferAsk(offers.getJSONObject(i), baseAsset)) {
				double myOfferPriceAsk = takerPays / takerGets;
				int idx = getOfferPrinceRange(myOfferPriceAsk, rangeAsks);
				if (idx != -1 && rangeCountAsks[idx] < 1) {
					listAsksResult.add((Double) listAsks.get(idx));
				}
			} else {
				double myOfferPriceBid = takerGets / takerPays;
				int idx = getOfferPrinceRange(myOfferPriceBid, rangeBids);
				if (idx != -1 && rangeCounBids[idx] < 1) {
					listBidsResult.add((Double) listBids.get(idx));
				}
			}
		}

		if (listAsksResult.size() > 0) {
			listAsks = listAsksResult;
		}

		if (listBidsResult.size() > 0) {
			listBids = listBidsResult;
		}

		log.info("calculateOpportunitiesAvalanche -> baseBalance " + baseBalance + " counterBalance " + counterBalance
				+ " baseAmountCost " + baseAmountCost + " baseRefCost " + baseRefCost + " baseCost " + baseCost
				+ " takerGetsAsk " + takerGetsAsk + " takerGetsBid  " + takerGetsBid + " bestAsk " + bestAsk
				+ " bestBid " + bestBid + " spreadAsk " + spreadAsk + " spreadBid " + spreadBid + " rangeCountAsks "
				+ Arrays.toString(rangeCountAsks) + " rangeCounBids " + Arrays.toString(rangeCounBids));

		log.info("Filtered listAsks " + listAsks);
		log.info("Filtered listBids " + listBids);

		JSONObject avalanche = createAvalancheMessage(beforeListAsks, beforeListBids, listAsks, listBids, rangeAsks,
				rangeBids, rangeCountAsks, rangeCounBids);
		template.convertAndSend(Channels.AVALANCHE, avalanche.toString());

		int size = Math.max(listAsks.size(), listBids.size());
		int sizeListAsks = listAsks.size();
		int sizeListBids = listBids.size();
		LinkedList<OfferCreate> listOffers = new LinkedList<>();
		for (int i = 0; i < size; i++) {
			if (i < sizeListAsks) {
				OfferCreate offerCreateAsk = createOfferAsk(takerGetsAsk, listAsks.get(i), rangeAsks, rangeCountAsks);
				if (offerCreateAsk != null) {
					listOffers.add(offerCreateAsk);
					if (enableOpportunityTaker) {
						template.convertAndSend(Channels.OFFER_CREATE, offerCreateAsk.toJSON().toString());
					}
				}
			}
			if (i < sizeListBids) {
				OfferCreate offerCreateBid = createOfferBid(takerGetsBid, listBids.get(i), rangeBids, rangeCounBids);
				if (offerCreateBid != null) {
					listOffers.add(offerCreateBid);
					if (enableOpportunityTaker) {
						template.convertAndSend(Channels.OFFER_CREATE, offerCreateBid.toJSON().toString());
					}
				}
			}
		}

		return listOffers;
	}

	public OfferCreate createOfferBid(double takerGetsBid, double bid, List<Pair<Double, Double>> rangeBids,
			int[] rangeCounBids) throws InvalidCipherTextException, JSONException, IOException {
		OfferCreate offer = null;
		double takerPaysBid = takerGetsBid / bid;
		double myOfferPriceBid = takerGetsBid / takerPaysBid;
		int idx = getOfferPrinceRange(myOfferPriceBid, rangeBids);
		if (idx != -1 && rangeCounBids[idx] < 1) {
			log.info("CreateOffer Bid " + myOfferPriceBid + " in Range " + rangeBids.get(idx));
			offer = createOffer(account(), Issue.fromString(counterAsset), Issue.fromString(baseAsset),
					new BigDecimal(takerGetsBid, new MathContext(5)), new BigDecimal(takerPaysBid, new MathContext(5)),
					false);
			// opportunityBids.put(offer.toJSON());
		} else {
			log.info("Opportunity not in range: myOfferPriceBid " + myOfferPriceBid + " bid " + bid + " takerPaysBid "
					+ takerPaysBid + " takerGetsBid " + takerGetsBid + " Idx " + idx + " rangeBids " + rangeBids);
		}

		return offer;
	}

	public OfferCreate createOfferAsk(double takerGetsAsk, double ask, List rangeAsks, int[] rangeCountAsks) throws InvalidCipherTextException, JSONException, IOException {
		OfferCreate offer = null;
		double takerPaysAsk = takerGetsAsk * ask;
		double myOfferPriceAsk = takerPaysAsk / takerGetsAsk;
		int idx = getOfferPrinceRange(myOfferPriceAsk, rangeAsks);
		if (idx != -1 && rangeCountAsks[idx] < 1) {
			log.info("CreateOffer Ask " + myOfferPriceAsk + " in Range " + rangeAsks.get(idx));
			offer = createOffer(account(), Issue.fromString(baseAsset), Issue.fromString(counterAsset),
					new BigDecimal(takerGetsAsk, new MathContext(5)), new BigDecimal(takerPaysAsk, new MathContext(5)),
					true);
			// opportunityAsks.put(offer.toJSON());
		} else {
			log.info("Opportunity not in range: myOfferPriceAsk " + myOfferPriceAsk + " ask " + ask + " takerPaysAsk "
					+ takerPaysAsk + " takerGetsAsk " + takerGetsAsk + " Idx " + idx + " rangeAsks " + rangeAsks);
		}

		return offer;
	}

	public static Pair<Double, Double> getOfferTakerValuesFromJSONObject(JSONObject o) throws JSONException {

		double takerPays;
		if (o.optJSONObject("taker_pays") == null || !o.optJSONObject("taker_pays").has("value")) {
			takerPays = o.getDouble("taker_pays") / 1000000;
		} else {
			takerPays = o.getJSONObject("taker_pays").getDouble("value");
		}

		double takerGets;
		if (o.optJSONObject("taker_gets") == null || !o.optJSONObject("taker_gets").has("value")) {
			takerGets = o.getDouble("taker_gets") / 1000000;
		} else {
			takerGets = o.getJSONObject("taker_gets").getDouble("value");
		}

		Pair<Double, Double> takerValues = new Pair(takerPays, takerGets);
		return takerValues;
	}

	public Pair<int[], int[]> cancelAvalancheOffers(Account account, JSONArray offers, double baseBalance,
			String baseAsset, double baseExpo, double counterExpo, List<Pair<Double, Double>> rangeAsks,
			double counterBalance, List<Pair<Double, Double>> rangeBids) throws JSONException {

		// validate for create or cancel offer this is Avalanche
		// https://bitbucket.org/elavbr/rippex-marketmaker/issues/33/offer-create-cancel-20-avalanche
		int[] rangeAsksCount = new int[rangeAsks.size()];
		int[] rangeBidsCount = new int[rangeBids.size()];

		for (int x = 0; x < offers.length(); x++) {
			double myAskOfferPrice;
			double myBidOfferPrice;
			Pair<Double, Double> takerValues = getOfferTakerValuesFromJSONObject(offers.getJSONObject(x));
			double takerPays = takerValues.getValue0();
			double takerGets = takerValues.getValue1();

			if (RippleAccountOffersPublisher.isOfferAsk(offers.getJSONObject(x), baseAsset)) {
				myAskOfferPrice = takerPays / takerGets;
				double value = takerGets / (baseBalance * baseExpo);
				int idx = getOfferPrinceRange(myAskOfferPrice, rangeAsks);
				if (idx == -1 || (value >= 1.1 && value <= 0.9)) {
					log.info("CancelOffer Ask Not in Range or (value >= 1.1 && value <= 0.9)  Offer: "
							+ offers.getJSONObject(x).getInt("seq"));
					template.convertAndSend(Channels.OFFER_CANCEL, offers.getJSONObject(x).toString());
				} else {
					if (rangeAsksCount[idx] >= 1) {
						log.info("Range Asks Count: " + rangeAsksCount[idx]);
						log.info("CancelOffer Ask already in Range " + rangeAsks.get(idx) + " Offer: "
								+ offers.getJSONObject(x).getInt("seq"));
						template.convertAndSend(Channels.OFFER_CANCEL, offers.getJSONObject(x).toString());
					}
					rangeAsksCount[idx]++;

				}
			} else {
				myBidOfferPrice = takerGets / takerPays;
				double value = takerPays / (counterBalance * counterExpo);
				int idx = getOfferPrinceRange(myBidOfferPrice, rangeBids);
				if (idx == -1 || (value >= 1.1 && value <= 0.9)) {
					log.info("CancelOffer Bid Not in Range (value >= 1.1 && value <= 0.9) Offer: "
							+ offers.getJSONObject(x).getInt("seq"));
					template.convertAndSend(Channels.OFFER_CANCEL, offers.getJSONObject(x).toString());
				} else {
					if (rangeBidsCount[idx] >= 1) {
						log.info("Range Bids Count: " + rangeBidsCount[idx]);

						log.info("CancelOffer Bid already in Range " + rangeAsks.get(idx) + " Offer: "
								+ offers.getJSONObject(x).getInt("seq"));
						template.convertAndSend(Channels.OFFER_CANCEL, offers.getJSONObject(x).toString());
					}
					rangeBidsCount[idx]++;

				}
			}
		}

		return new Pair<>(rangeAsksCount, rangeBidsCount);
	}

	public int getOfferPrinceRange(double myOfferPrice, List<Pair<Double, Double>> range) {
		for (int i = 0; i < range.size(); i++) {
			if (isValueInRange(myOfferPrice, range.get(i))) {
				return i;
			}
		}
		return -1;
	}

	public boolean isOfferPriceInRange(double myAskOfferPrice, List<Pair<Double, Double>> range) {
		for (int i = 0; i < range.size(); i++) {
			if (isValueInRange(myAskOfferPrice, range.get(i))) {
				return true;
			}
		}
		return false;
	}

	public boolean isValueInRange(double myOfferPrice, Pair<Double, Double> range) {
		if (range == null) {
			return false;
		}

		if (myOfferPrice >= range.getValue0() && myOfferPrice <= range.getValue1()) {
			return true;
		}
		return false;
	}

	public OfferCreate createOffer(Account account, Issue baseAsset, Issue counterAsset, BigDecimal takerGets,
			BigDecimal takerPays, boolean isAsk) {

		Amount amountGets;
		if (baseAsset.currency().isNative()) {
			amountGets = new Amount(Amount.roundValue(takerGets, true), Issue.XRP.currency(), Issue.XRP.issuer(), true,
					false);
		} else {
			amountGets = new Amount(takerGets.abs(new MathContext(7)), baseAsset.currency(), baseAsset.issuer());
		}

		Amount amountPays;
		if (counterAsset.currency().isNative()) {
			amountPays = new Amount(Amount.roundValue(takerPays, true), Issue.XRP.currency(), Issue.XRP.issuer(), true,
					false);
		} else {
			amountPays = new Amount(takerPays.abs(new MathContext(7)), counterAsset.currency(), counterAsset.issuer());
		}

		OfferCreate offer = new OfferCreate();
		offer.account(account.id());
		offer.takerGets(amountGets);
		offer.takerPays(amountPays);

		return offer;
	}

	public BigDecimal findAccountBalanceFromCurrency(JSONObject json, Issue issue)
			throws JSONException, InvalidCipherTextException, IOException {

		String baseCurrency = issue.currency().toString();
		if (baseCurrency.equals(Issue.XRP.toString())) {
			return new BigDecimal(account().getAccountRoot().getBalance().floatValue());
		}

		JSONArray lines = json.getJSONObject("result").getJSONArray("lines");

		int linesLength = lines.length();
		for (int x = 0; x < linesLength; x++) {
			JSONObject line = lines.getJSONObject(x);
			String currency = line.getString("currency");
			String issuerAccount = line.getString("account");
			if (currency.equals(baseCurrency) && issuerAccount.equals(issue.issuer().address)) {
				return new BigDecimal(line.getDouble("balance"));
			}
		}

		log.info("There is no balance for " + issue.toString());
		return BigDecimal.ZERO;
	}

	public BigDecimal findPathfindFromRefAsset(JSONObject json, Issue refIssue, Issue baseIssue) throws JSONException {

		JSONArray alts = json.getJSONArray("alternatives");

		int altsLength = alts.length();
		for (int x = 0; x < altsLength; x++) {
			JSONObject j = alts.getJSONObject(x);
			JSONObject sourceAmount = j.optJSONObject("source_amount");
			if (sourceAmount == null) {
				return new BigDecimal(j.getString("source_amount"));
			}

			String currency = sourceAmount.getString("currency");
			String issuer = sourceAmount.getString("issuer");
			if (currency.equals(refIssue.currency().toString()) && issuer.equals(refIssue.issuer().address)) {
				if (Issue.XRP.currency().toString().equals(baseIssue.currency().toString())) {
					// TODO checkout doc
					// https://ripple.com/build/rippled-apis/#specifying-currency-amounts
					return new BigDecimal(sourceAmount.getString("value")).multiply(BigDecimal.valueOf(1000000));
				} else {
					return new BigDecimal(sourceAmount.getString("value"));
				}
			}
		}

		return BigDecimal.ZERO;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		refreshSetup();
	}

	private JSONObject createAvalancheMessage(List<Double> beforeListAsks, List<Double> beforeListBids,
			List<Double> listAsks, List<Double> listBids, List<Pair<Double, Double>> rangeAsks,
			List<Pair<Double, Double>> rangeBids, int[] rangeCountAsks, int[] rangeCounBids) {
		JSONObject json = new JSONObject();
		json.put("createdAt", Calendar.getInstance().getTime());
		json.put("enableOpportunityTaker", enableOpportunityTaker);
		json.put("cancelAllAccountOffersOnStart", cancelAllAccountOffersOnStart);
		json.put("pathFrom", pathFrom);
		json.put("pathTo", pathTo);
		json.put("baseAsset", baseAsset);
		json.put("counterAsset", counterAsset);
		json.put("refAsset", refAsset);
		json.put("refCost", refCost);
		json.put("baseAmount", baseAmount);
		json.put("marginAsk", marginAsk);
		json.put("marginBid", marginBid);
		json.put("degreeAsk", degreeAsk);
		json.put("degreeBid", degreeBid);
		json.put("maxOpenAsks", maxOpenAsks);
		json.put("maxOpenBids", maxOpenBids);
		json.put("baseExpo", baseExpo);
		json.put("counterExpo", counterExpo);
		json.put("slippage", slippage);
		json.put("maxOpenAsks", maxOpenAsks);
		json.put("maxOpenBids", maxOpenBids);
		json.put("listAsks", beforeListAsks);
		json.put("listBids", beforeListBids);
		json.put("rangeAsks", rangeAsks);
		json.put("rangeBids", rangeBids);
		json.put("rangeCountAsks", rangeCountAsks);
		json.put("rangeCountBids", rangeCounBids);
		json.put("filteredListBids", listBids);
		json.put("filteredListAsks", listAsks);
		return json;
	}

}
