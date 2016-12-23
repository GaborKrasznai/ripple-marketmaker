package marketmaker.services.exchanges.bitstamp;

import org.springframework.beans.factory.annotation.Value;

public class BitstampBalanceService {

	@Value("${ripple.arbitrager.bitstamp.balance.url}")
	private String url; 
	
}
