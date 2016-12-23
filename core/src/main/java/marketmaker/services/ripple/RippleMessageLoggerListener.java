package marketmaker.services.ripple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;

/**
 * Created by rmartins on 6/4/15.
 */
public class RippleMessageLoggerListener {
	private static Logger log = LoggerFactory.getLogger(RippleMessageLoggerListener.class);

	@JmsListener(destination = "account_offers")
	public void onMessage(String message) {
		log.info(message);
	}
}
