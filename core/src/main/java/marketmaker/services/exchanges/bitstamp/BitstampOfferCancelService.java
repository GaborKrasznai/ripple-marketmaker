package marketmaker.services.exchanges.bitstamp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;

public class BitstampOfferCancelService {

	@Value("${ripple.arbitrager.bitstamp.cancelorder.url}")
	private String url;
	
	@JmsListener(destination = "bitstamp_offercancel")
	private void onMessage(String message) {
		
	}
}
