package marketmaker.services.exchanges.bitfinex;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import marketmaker.entities.Channels;

public class BitfinexBookPublisher {

	private static Logger log = LoggerFactory.getLogger(BitfinexBookPublisher.class);

	@Value("${ripple.arbitrager.bitfinex.bitfinex_book}")
	public String bitfinexBookUrl;

	@Autowired
	private JmsTemplate template;

	public BitfinexBookPublisher() {

	}

	public BitfinexBookPublisher(String bitfinexBookUrl) {
		this.bitfinexBookUrl = bitfinexBookUrl;
	}

	@Scheduled(fixedDelay = 12000)
	public void run() {
		JSONObject json = createAndExecuteRequest();
		if (template != null) {
			template.convertAndSend(Channels.BITFINEX_BOOK, json.toString());
		}
	}

	public JSONObject createAndExecuteRequest() {
		URL urlConnection;
		try {
			urlConnection = new URL(bitfinexBookUrl);

			HttpsURLConnection con = (HttpsURLConnection) urlConnection.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("GET");
			con.addRequestProperty("Content-Type", "application/json");

			int responseCode = con.getResponseCode();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			JSONObject jsonObject = new JSONObject(response.toString());
			return jsonObject;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return null;

	}

}
