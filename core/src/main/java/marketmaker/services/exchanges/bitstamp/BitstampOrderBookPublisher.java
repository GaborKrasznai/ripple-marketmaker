package marketmaker.services.exchanges.bitstamp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import marketmaker.entities.Channels;

public class BitstampOrderBookPublisher {

	@Value("${ripple.arbitrager.bitstamp.orderbook.url}")
	private String url;

	@Autowired
	private JmsTemplate template;

	@Scheduled(fixedDelay = 12000)
	public void run() {
		try {
			createAndExecuteRequest();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String createAndExecuteRequest() throws Exception {
		URL urlConnection = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) urlConnection.openConnection();
		con.setDoOutput(true);
		con.setRequestMethod("POST");

		Date d = Calendar.getInstance().getTime();
		String n = String.valueOf((d.getTime() + Calendar.getInstance().get(Calendar.MILLISECOND) / 1000000) * 1000000);

		int responseCode = con.getResponseCode();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		JSONObject json = new JSONObject(response.toString());

		if (template != null) {
			template.convertAndSend(Channels.BITSTAMP_ORDERBOOK, json.toString());
		}

		return json.toString();
	}

}
