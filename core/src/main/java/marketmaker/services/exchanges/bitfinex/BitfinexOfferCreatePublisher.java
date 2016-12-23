package marketmaker.services.exchanges.bitfinex;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Calendar;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;

public class BitfinexOfferCreatePublisher {

	private static Logger log = LoggerFactory.getLogger(BitfinexOfferCreatePublisher.class);

	@Value("${ripple.arbitrager.bitfinex.bitfinex_offercreate}")
	public String bitfinexOffercreateUrl;

	@Value("${ripplemm.arbitrager.createoffers.enabled}")
	private boolean arbitrageCreateOffersEnabled = false;

	@Value("${ripple.arbitrager.bitfinex.apikey}")
	private String apikey;
	@Value("${ripple.arbitrager.bitfinex.apikeysecret}")
	private String apiSecret;

	public BitfinexOfferCreatePublisher() {

	}

	public BitfinexOfferCreatePublisher(String bitfinexOffercreateUrl2, String apiSecret, String apiKey,
			boolean enableOfferCreate) {
		this.bitfinexOffercreateUrl = bitfinexOffercreateUrl2;
		this.apiSecret = apiSecret;
		this.apikey = apiKey;
		this.arbitrageCreateOffersEnabled = enableOfferCreate;
	}

	@JmsListener(destination = "bitfinex_offercreate")
	public void onMessage(String message) {
		JSONObject json = new JSONObject(message);
		// Channels.BITFINEX_OFFERCREATE
	}

	public String createAndExecuteOrderRequest(Double amount, Double price, Double limit_price, boolean isBuy) {

		URL urlConnection;
		try {
			urlConnection = new URL(bitfinexOffercreateUrl);

			HttpsURLConnection con = (HttpsURLConnection) urlConnection.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
//			con.setRequestProperty("charset", "utf-8");

			Long d = Calendar.getInstance().getTime().getTime();
			JSONObject json = createJSONObject(amount, price, isBuy, d);

			String payload = Base64.getEncoder().encodeToString(json.toString().getBytes());
			con.setRequestProperty("X-BFX-APIKEY", apikey);
			con.setRequestProperty("X-BFX-PAYLOAD", payload);
			String signature = generateHmacSHA384Signature(payload, apiSecret.getBytes());
			log.info("Bitfinex " + json.toString() + " Payload " + payload + " Signature: " + signature);

			con.setRequestProperty("X-BFX-SIGNATURE", signature);

			if (arbitrageCreateOffersEnabled) {
				// PrintWriter out = new PrintWriter(con.getOutputStream());
				// out.print(payload);
				// out.close();

				int responseCode = con.getResponseCode();
				log.info("Bitfinex ResponseCode: " + responseCode + " Message: " + con.getResponseMessage());

				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				return response.toString();
			} else {
				return "Not executed.";
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public JSONObject createJSONObject(Double amount, Double price, boolean isBuy, Long d) {
		JSONObject json = new JSONObject();
		json.put("request", "/v1/order/new");
		json.put("nonce", String.valueOf(d));
		json.put("symbol", "BTCUSD");
		json.put("amount", String.format("%.2f", amount));
		json.put("price", String.format("%.2f", price));
		json.put("exchange", "bitfinex");
		if (isBuy) {
			json.put("side", "buy");
		} else {
			json.put("side", "sell");
		}
		json.put("type", "exchange limit");
		return json;
	}

	public static String generateHmacSHA384Signature(String data, byte[] keyString)
			throws GeneralSecurityException, IllegalStateException, UnsupportedEncodingException {

		final SecretKey secretKey = new SecretKeySpec(keyString, "HmacSHA384");
		Mac mac = Mac.getInstance("HmacSHA384");
		mac.init(secretKey);
		mac.update(data.getBytes());

		byte[] result = mac.doFinal();
		return String.format("%096x", new BigInteger(1, result));

	}
}
