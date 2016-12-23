package marketmaker.services.exchanges.bitstamp;

import org.springframework.beans.factory.annotation.Value;

public class BitstampOpenOrdersService {
	
	@Value("${ripple.arbitrager.bitstamp.openorders.url}")
	private String url;

}
