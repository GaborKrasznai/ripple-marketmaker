package marketmaker.services.exchanges;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Base64;
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

public class B2UExchangeBalancePublisher implements InitializingBean {

	private static Logger log = LoggerFactory.getLogger(B2UExchangeBalancePublisher.class);

	@Autowired
	private JmsTemplate template;

	@Value("${ripplemm.b2u.apikey}")
	private String b2uApiKey;
	@Value("${ripplemm.b2u.apisecret}")
	private String b2uSecret;
	@Value("${ripplemm.b2u.url}")
	private String b2uUrl;

	public B2UExchangeBalancePublisher() {

	}

	@Scheduled(fixedDelay = 20000, initialDelay = 0)
	public void run() {
		try {
			createAndExecuteRequest();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void createAndExecuteRequest() throws NoSuchAlgorithmException, InvalidKeyException {
		try {
			URL url = new URL(b2uUrl.concat("/CREATEORDER.ASPX"));
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");

			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secret_key = new SecretKeySpec(b2uSecret.getBytes("UTF-8"), "HmacSHA256");
			sha256_HMAC.init(secret_key);

			String nonce = String.valueOf(Calendar.getInstance().getTimeInMillis());
			JSONObject json = new JSONObject();
			json.put("key", b2uApiKey);
			json.put("nonce", nonce);
			json.put("signature", Base64.encodeBase64String(sha256_HMAC.doFinal(nonce.concat(b2uApiKey).getBytes("UTF-8"))).toUpperCase());
			json.put("asset", "DOG");
			json.put("action", "buy");
			json.put("price", "0,00095");
			json.put("amount", "1");

			log.info(json.toString(4));
			con.getOutputStream().write(json.toString(4).getBytes("UTF-8"));

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
				template.convertAndSend(Channels.B2U_BALANCE, response.toString());
			} else {
				log.info("B2U returns response code != 200 " + response.toString());
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		createAndExecuteRequest();
	}

}
