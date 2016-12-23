package marketmaker.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import marketmaker.entities.AvalancheSetup;

public class LiveFeedPublisher implements InitializingBean {

	private static Logger logger = org.slf4j.LoggerFactory.getLogger(LiveFeedPublisher.class);

	// @Value("${ripplemm.liveFeedEndpoint}")
	private String endpoint;
	// @Value("${ripplemm.liveFeedEnabled}")
	private boolean liveFeedEnabled = false;
	// @Value("${ripplemm.liveFeedCurrencyPair}")
	private String liveFeedCurrencyPair;

	@Value("${ripplemm.instanceId}")
	private String instanceId;
	@Autowired
	private AvalancheService avalancheService;

	@Autowired
	public JmsTemplate template;

	private String rate;

	@Scheduled(cron = "0 0 2 * * *")
	public void run() {

		if (liveFeedEnabled) {
			String key = "refCost";
			JSONObject message = new JSONObject();
			String value = createAndExecuteRequest();
			if (value != null) {
				message.put(key, value);
			}

			if (liveFeedEnabled) {
				template.convertAndSend(marketmaker.entities.Channels.INSTRUMENTS, message.toString());
			}

		}
	}

	public String createAndExecuteRequest() {

		URL urlConnection;
		try {
			urlConnection = new URL(endpoint);

			HttpURLConnection con = (HttpURLConnection) urlConnection.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/json");
			int responseCode = con.getResponseCode();

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			String content = response.toString();
			JSONObject json = new JSONObject(content);

			if (!json.has("rates")) {
				return null;
			}

			JSONObject rates = json.getJSONObject("rates");
			Double value = rates.getDouble(liveFeedCurrencyPair);

			return String.valueOf(value);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		AvalancheSetup setup = avalancheService.findByInstanceId(instanceId);
		if (setup != null) {
			liveFeedCurrencyPair = setup.getLiveFeedCurrencyPair();
			liveFeedEnabled = setup.isLiveFeedEnabled();
			endpoint = setup.getLiveFeedEndpoint();
			run();
		}
	}

	public static void main(String args[]) {

	}

}
