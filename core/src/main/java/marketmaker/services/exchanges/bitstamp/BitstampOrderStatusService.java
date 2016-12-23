package marketmaker.services.exchanges.bitstamp;

import org.springframework.beans.factory.annotation.Value;

public class BitstampOrderStatusService {


	@Value("${ripple.arbitrager.bitstamp.orderstatus.url}")
	private String url;
	
}
