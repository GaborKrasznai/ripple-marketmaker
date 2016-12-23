package marketmaker.entities;

import java.math.BigDecimal;
import java.util.HashMap;

import org.json.JSONObject;

public class InstrumentsList {

	private static HashMap<String, String> values = new HashMap<>();

	public static HashMap<String, String> getValues() {
		return values;
	}

	public static void setKey(String key, String value) {
		values.put(key, value);
	}

	public static String getKey(String key) {
		return values.get(key);
	}

	static {
		values.put("cancelAllAccountOffersOnStart", null);
		values.put("enableOpportunityTaker", null);
		values.put("scriptsDirectory", null);
		values.put("pathFrom", null);
		values.put("pathTo", null);
		values.put("baseAsset", null);
		values.put("counterAsset", null);
		values.put("refAsset", null);
		values.put("refCost", null);
		values.put("baseAmount", null);
		values.put("marginAsk", null);
		values.put("marginBid", null);
		values.put("degreeAsk", null);
		values.put("degreeBid", null);
		values.put("maxOpenAsks", null);
		values.put("maxOpenBids", null);
		values.put("baseExpo", null);
		values.put("counterExpo", null);
		values.put("slippage", null);
		values.put("refCostMargin", null);
		values.put("liveFeedEndpoint", null);
		values.put("liveFeedCurrencyPair", null);
		values.put("liveFeedEnabled", null);
		values.put("rippleAccount", null);

	}

	public static AvalancheSetup toAvalancheSetup(JSONObject json) {
		AvalancheSetup setup = new AvalancheSetup();

		if (json.has("id"))
			setup.setId(json.getLong("id"));

		if (json.has("enableOpportunityTaker"))
			setup.setEnableOpportunityTaker(json.getBoolean("enableOpportunityTaker"));
		if (json.has("cancelAllAccountOffersOnStart"))
			setup.setCancelAllAccountOffersOnStart(json.getBoolean("cancelAllAccountOffersOnStart"));
		if (json.has("pathFrom"))
			setup.setPathFrom(json.getString("pathFrom"));
		if (json.has("pathTo"))
			setup.setPathTo(json.getString("pathTo"));
		if (json.has("baseAsset"))
			setup.setBaseAsset(json.getString("baseAsset"));
		if (json.has("counterAsset"))
			setup.setCounterAsset(json.getString("counterAsset"));
		if (json.has("refAsset"))
			setup.setRefAsset(json.getString("refAsset"));
		if (json.has("refCost"))
			setup.setRefCost(new BigDecimal(json.getDouble("refCost")));
		if (json.has("baseAmount"))
			setup.setBaseAmount(new BigDecimal(json.getDouble("baseAmount")));
		if (json.has("marginAsk"))
			setup.setMarginAsk(new BigDecimal(json.getDouble("marginAsk")));
		if (json.has("marginBid"))
			setup.setMarginBid(new BigDecimal(json.getDouble("degreeAsk")));
		if (json.has("degreeAsk"))
			setup.setDegreeAsk(new BigDecimal(json.getDouble("degreeAsk")));
		if (json.has("degreeBid"))
			setup.setDegreeBid(new BigDecimal(json.getDouble("degreeBid")));
		if (json.has("maxOpenAsks"))
			setup.setMaxOpenAsks(new BigDecimal(json.getDouble("maxOpenAsks")));
		if (json.has("maxOpenBids"))
			setup.setMaxOpenBids(new BigDecimal(json.getDouble("maxOpenBids")));
		if (json.has("baseExpo"))
			setup.setBaseExpo(new BigDecimal(json.getDouble("baseExpo")));
		if (json.has("counterExpo"))
			setup.setCounterExpo(new BigDecimal(json.getDouble("counterExpo")));
		if (json.has("slippage"))
			setup.setSlippage(new BigDecimal(json.getDouble("slippage")));
		if (json.has("refCostMargin"))
			setup.setRefCostMargin(new BigDecimal(json.getDouble("refCostMargin")));
		if (json.has("liveFeedEndpoint"))
			setup.setLiveFeedEndpoint(json.getString("liveFeedEndpoint"));
		if (json.has("liveFeedCurrencyPair"))
			setup.setLiveFeedCurrencyPair(json.getString("liveFeedCurrencyPair"));
		if (json.has("liveFeedEnabled"))
			setup.setLiveFeedEnabled(json.getBoolean("liveFeedEnabled"));
		if (json.has("rippleAccount"))
			setup.setRippleAccount(json.getString("rippleAccount"));

		return setup;

	}

}
