package marketmaker.services.exchanges.bitstamp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;

import marketmaker.utils.Utils;

public class BitstampOfferCreateService {

	private static Logger log = LoggerFactory.getLogger(BitstampOfferCreateService.class);

	@Value("${ripple.arbitrager.bitstamp.sell.url}")
	private String sellUrl;
	@Value("${ripple.arbitrager.bitstamp.buy.url}")
	private String buyUrl;

	@Value("${ripple.arbitrager.bitstamp.customerid}")
	private String customerId;

	@Value("${ripplemm.arbitrager.createoffers.enabled}")
	private boolean arbitrageCreateOffersEnabled = false;

	@Value("${ripple.arbitrager.bitstamp.key}")
	private String key;
	@Value("${ripple.arbitrager.bitstamp.secret}")
	private String apiSecret;

	@JmsListener(destination = "bitstamp_offercreate")
	public void message(String message) {
		JSONObject json = new JSONObject(message);
		Double deepness = json.getDouble("deepness");
		Double price = json.getDouble("price");
		Double limit_price = json.getDouble("limit_price");
		boolean isBuy = json.getBoolean("isBuy");

		try {
			createAndExecuteOrderRequest(deepness, price, limit_price, isBuy);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public String createAndExecuteOrderRequest(Double amount, Double price, Double limit_price, boolean isBuy)
			throws Exception {

		String url = buyUrl;
		if (!isBuy) {
			url = sellUrl;
		}

		URL urlConnection = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) urlConnection.openConnection();
		con.setDoOutput(true);
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		con.setRequestProperty("charset", "utf-8");

		Date d = Calendar.getInstance().getTime();
		String n = String.valueOf((d.getTime() + Calendar.getInstance().get(Calendar.MILLISECOND) / 1000000) * 1000000);

		// con.setRequestProperty("Content-Type",
		// MediaType.APPLICATION_FORM_URLENCODED);

		String signature = generateHmacSHA256Signature(n.concat(customerId).concat(key), apiSecret);

		HashMap<String, String> parameters = new HashMap<>();
		parameters.put("signature", signature.toUpperCase());
		parameters.put("nonce", n);
		parameters.put("amount", amount.toString());
		parameters.put("price", String.format("%.2f", price));
		parameters.put("limit_price", String.format("%.2f", limit_price));
		parameters.put("key", key);

		log.info("Bitstamp " + parameters.toString());
		if (arbitrageCreateOffersEnabled) {
			String postParameters = Utils.createQueryStringForParameters(parameters);
			// send the POST out
			PrintWriter out = new PrintWriter(con.getOutputStream());
			out.print(postParameters);
			out.close();

			int responseCode = con.getResponseCode();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			log.info(response.toString());
			return response.toString();
		} else {
			return "Not executed.";
		}
	}

	public static String generateHmacSHA256Signature(String data, String keyString)
			throws GeneralSecurityException, IllegalStateException, UnsupportedEncodingException {
		SecretKeySpec key = new SecretKeySpec((keyString).getBytes("UTF-8"), "HmacSHA256");
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(key);

		byte[] bytes = mac.doFinal(data.getBytes("UTF-8"));

		StringBuffer hash = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				hash.append('0');
			}
			hash.append(hex);
		}
		return hash.toString();
	}

}
