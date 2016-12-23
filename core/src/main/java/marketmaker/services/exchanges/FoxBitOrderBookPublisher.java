package marketmaker.services.exchanges;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import marketmaker.entities.Channels;

/**
 * BRL / BTC OrderBook from FoxBit Exchange
 * 
 * @author rmartins
 */
public class FoxBitOrderBookPublisher implements InitializingBean {

	private static Logger log = LoggerFactory.getLogger(FoxBitOrderBookPublisher.class);

	@Autowired
	private JmsTemplate template;

	@Value("${ripplemm.foxbit.orderbook.url}")
	private String urlEndpoint;

	@Scheduled(fixedDelay = 6000)
	public void run() {
		try {
			URL urlConnection = new URL(urlEndpoint);
			HttpsURLConnection con = (HttpsURLConnection) urlConnection.openConnection();
			con.setRequestMethod("GET");

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
				template.convertAndSend(Channels.FOXBIT_ORDERBOOK, response.toString());
			} else {
				log.info("FoxBit returns response code != 200 " + response.toString());
			}

		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
