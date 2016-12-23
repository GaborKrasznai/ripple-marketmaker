package marketmaker.services.ripple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

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

import com.ripple.client.Client;
import com.ripple.client.enums.Command;
import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.core.coretypes.Issue;

import marketmaker.entities.Channels;
import marketmaker.entities.Pathfind;

public class RippleArbitragerPathfindPublisher implements InitializingBean {

	private static Logger log = LoggerFactory.getLogger(RippleArbitragerPathfindPublisher.class);

	@Autowired
	private JmsTemplate template;

	private List<Pair<Issue, Issue>> listExchanges;

	@Value("${ripplemm.arbitrager.exchanges}")
	private String exchanges;
	@Value("${ripplemm.ripple.account}")
	private String rippleAccount;
	@Value("${ripplemm.arbitrager.amounts}")
	private String exchangesAmounts;
	private HashMap<String, Double> listAmounts = new HashMap<>();

	private HashMap<String, Pathfind> pathfinds = new HashMap<>();

	@JmsListener(destination = "pathfind")
	public void onPathfind(String message) {
		JSONObject json = new JSONObject(message);
		// log.info("Pathfind: " + json.getString("baseAsset"));

		Pathfind p = new Pathfind(json);
		pathfinds.put(p.getBaseAsset().toString(), p);

		if (pathfinds.containsKey(listExchanges.get(0).getValue0().toString())
				&& pathfinds.containsKey(listExchanges.get(0).getValue1().toString())) {
			Pathfind pathfind1 = pathfinds.get(listExchanges.get(0).getValue0().toString());
			Pathfind pathfind2 = pathfinds.get(listExchanges.get(0).getValue1().toString());
			pathfind1.setIssues(listExchanges.get(0));
			pathfind2.setIssues(listExchanges.get(0));

			if (pathfind1 != null && pathfind2 != null) {

				if (pathfind1.getResult().getJSONObject("result").optBoolean("full_reply") == true
						&& pathfind2.getResult().getJSONObject("result").optBoolean("full_reply") == true) {

					// find alternative
					JSONObject alternative1 = findPathfindFromBaseAssetCurrency(
							pathfind1.getResult().getJSONObject("result").getJSONArray("alternatives"),
							pathfind2.getBaseAsset());
					pathfind1.setAlternative(alternative1);
					JSONObject alternative2 = findPathfindFromBaseAssetCurrency(
							pathfind2.getResult().getJSONObject("result").getJSONArray("alternatives"),
							pathfind1.getBaseAsset());
					pathfind2.setAlternative(alternative2);
					publish(pathfind1, pathfind2);
				}
			}
		}
	}

	public void publish(Pathfind pathfind1, Pathfind pathfind2) {
		if (pathfind1.getAlternative() == null || pathfind2.getAlternative() == null) {
			return;
		}

		// TODO check for XRP source_amount
		Double sourceAmountPathfind1 = pathfind1.getAlternative().getJSONObject("source_amount").getDouble("value");
		Double ask = sourceAmountPathfind1 / pathfind1.getAmount();
		Double sourceAmountPathfind2 = pathfind2.getAlternative().getJSONObject("source_amount").getDouble("value");
		Double bid = pathfind2.getAmount() / sourceAmountPathfind2;

		JSONObject jsonResult = new JSONObject();
		jsonResult.put("exchange", pathfind1.getBaseAsset() + "/" + pathfind2.getBaseAsset());
		jsonResult.put("baseIssue", pathfind1.getBaseAsset());
		jsonResult.put("counterIssue", pathfind2.getBaseAsset());
		jsonResult.put("amount1", pathfind1.getAmount());
		jsonResult.put("amount2", pathfind2.getAmount());
		jsonResult.put("pathfind1", pathfind1.toJSONObject());
		jsonResult.put("pathfind2", pathfind2.toJSONObject());
		jsonResult.put("bid", bid);
		jsonResult.put("ask", ask);
		log.info(jsonResult.toString());
		template.convertAndSend(Channels.PATH_FIND_PAIR, jsonResult.toString());
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

	// TODO move it to utils
	public static List<Pair<Issue, Issue>> parseExchanges(String exchanges) {
		String[] e = exchanges.split(",");
		List<Pair<Issue, Issue>> listPair = new ArrayList<>();
		for (int i = 0; i < e.length; i++) {
			if (!e[i].startsWith("#")) {
				String[] paths = e[i].split("/");
				Issue issue1 = Issue.fromString(paths[0] + "/" + paths[1]);
				Issue issue2 = Issue.fromString(paths[2] + "/" + paths[3]);
				Pair<Issue, Issue> pair = new Pair<Issue, Issue>(issue1, issue2);
				listPair.add(pair);
			}
		}
		return listPair;
	}

	public static Pair<Pathfind, Pathfind> parsePathfinds(String exchanges, Double deepness, String rippleAccount,
			String rippleAccount2) {
		List<Pair<Issue, Issue>> listExchanges = RippleArbitragerPathfindPublisher.parseExchanges(exchanges);
		Pathfind pathfind1 = createPathfind(deepness, rippleAccount, listExchanges.get(0).getValue1(), listExchanges);
		Pathfind pathfind2 = createPathfind(deepness, rippleAccount, listExchanges.get(0).getValue0(), listExchanges);
		return new Pair<Pathfind, Pathfind>(pathfind1, pathfind2);
	}

	private static Pathfind createPathfind(Double deepness, String rippleAccount, Issue baseAsset,
			List<Pair<Issue, Issue>> listExchanges) {
		Pathfind pathfind = new Pathfind();
		pathfind.setPathFrom(rippleAccount);
		pathfind.setPathTo(rippleAccount);
		pathfind.setIssues(listExchanges.get(0));
		pathfind.setAmount(deepness);
		pathfind.setBaseAsset(listExchanges.get(0).getValue0());

		return pathfind;
	}

	public static HashMap<String, Double> parseExchangeAmountsFromProperties(String value) {
		HashMap<String, Double> result = new HashMap<String, Double>();
		try {
			String[] r = value.split(",");
			for (int i = 0; i < r.length; i++) {
				String[] values = r[i].split(";");
				result.put(values[0], Double.parseDouble(values[1]));
			}
		} catch (Exception ex) {
			log.error("Error parsing Ratios", ex);
		}
		return result;
	}

	public Request subscribePathfind(Client client, Pathfind pathfind) {
		Request request = client.newRequest(Command.path_find);
		try {
			request.json().put("id", new Random().nextInt());
			request.json().put("subcommand", "create");
			request.json().put("source_account", pathfind.getPathFrom());
			request.json().put("destination_account", pathfind.getPathTo());

			if (!Issue.XRP.currency().toString().equals(pathfind.getBaseAsset().currency().toString())) {
				JSONObject path_find = new JSONObject();
				path_find.put("value", String.format("%.12f", pathfind.getAmount()));
				path_find.put("currency", pathfind.getBaseAsset().currency().toString());
				path_find.put("issuer", pathfind.getBaseAsset().issuer().address);
				request.json().put("destination_amount", path_find);
			} else {
				request.json().put("destination_amount", String.format("%.12f", pathfind.getAmount()));
			}

			// request.json().put("send_max", String.format("%.12f", deepness));

			request.on(Request.OnError.class, new Request.OnError() {
				@Override
				public void called(Response args) {
					// TODO Auto-generated method stub

				}
			});

			request.on(Request.OnResponse.class, new Request.OnResponse() {
				@Override
				public void called(Response response) {
					// System.out.println("Response : " + response.toString());
				}
			});
			request.request();

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return request;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		listExchanges = RippleArbitragerPathfindPublisher.parseExchanges(exchanges);
	}

}