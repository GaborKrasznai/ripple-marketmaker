package marketmaker.services.exchanges;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Hex;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import marketmaker.entities.Channels;

public class FoxBitExchangePublisher implements InitializingBean {

	private static Logger log = LoggerFactory.getLogger(FoxBitExchangePublisher.class);

	@Autowired
	private JmsTemplate template;

	@Value("${ripplemm.foxbit.apiKey}")
	private String apiKey;
	@Value("${ripplemm.foxbit.apiSecret}")
	private String apiSecret;
	@Value("${ripplemm.foxbit.url}")
	private String url; // https://api.testnet.blinktrade.com/tapi/v1/message

	private static Mac sha256_HMAC;

	@Scheduled(fixedDelay = 60000)
	public void run() throws GeneralSecurityException {
		createAndExecuteRequest();
	}

	// msg = {
	// "MsgType": "U2", # Balance Request
	// "BalanceReqID": 1 # An ID assigned by you. It can be any number. The
	// response message associated with this request will contain the same ID.
	// }

	// msg = {
	// "MsgType": "U4",
	// "OrdersReqID": 1,
	// "Page": 0,
	// "PageSize": 100,
	// "Filter":["has_leaves_qty eq 1"] # Set it to "has_leaves_qty eq 1" to get
	// open orders, "has_cum_qty eq 1" to get executed orders, "has_cxl_qty eq
	// 1" to get cancelled orders
	// }
	//
	public void createAndExecuteRequest() throws GeneralSecurityException {
		try {
			URL urlConnection = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) urlConnection.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");

			Date d = Calendar.getInstance().getTime();
			String n = String
					.valueOf((d.getTime() + Calendar.getInstance().get(Calendar.MILLISECOND) / 1000000) * 1000000);

			JSONObject json = new JSONObject();
			json.put("MsgType", "U2");
			json.put("BalanceReqID", 1);

			// JSONObject json1 = new JSONObject();
			// json1.put("MsgType", "U4");
			// json1.put("OrdersReqID", 1);
			// json1.put("Page", 0);
			// json1.put("PageSize", 100);
			// json1.put("Filter", "[\"has_leaves_qty eq 1\"]");

			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("APIKey", "E4sDz8urG04zI1Ej7bRf95PjC3Q0ZLRlJIKN4vx4cDc");
			con.setRequestProperty("Nonce", n);
			String signature = generateHmacSHA256Signature(n, apiSecret);
			con.setRequestProperty("Signature", signature);
			con.getOutputStream().write(json.toString().getBytes("UTF-8"));

			int responseCode = con.getResponseCode();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				log.info(inputLine);
				response.append(inputLine);
			}
			in.close();

			if (responseCode == 200) {
				log.info(response.toString());
				template.convertAndSend(Channels.FOXBIT_BALANCE, response.toString());
			} else {
				log.info("FoxBit returns response code != 200 " + response.toString());
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public static String generateHmacSHA256Signature(String data, String keyString)
			throws GeneralSecurityException, IllegalStateException, UnsupportedEncodingException {
		SecretKeySpec key = new SecretKeySpec((keyString).getBytes("UTF-8"), "HmacSHA256");
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(key);

		byte[] bytes = mac.doFinal(data.getBytes("ASCII"));

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

	@Override
	public void afterPropertiesSet() throws Exception {

	}

	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException {
		String apiSecret = "efzC6famchKDLujsBDoqLa9pti9DV3utfDZv9xDD1Q8";
		sha256_HMAC = Mac.getInstance("HmacSHA256");
		SecretKeySpec secret_key = new SecretKeySpec(apiSecret.getBytes(), "HmacSHA256");
		sha256_HMAC.init(secret_key);

		Integer n = Calendar.getInstance().get(Calendar.MILLISECOND) * 1000000;
		byte[] sig = sha256_HMAC.doFinal(String.valueOf(n).getBytes());
		System.out.println(Hex.encodeHexString(sig));

	}

}
