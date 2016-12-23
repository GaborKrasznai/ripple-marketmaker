package marketmaker.services.ripple;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.core.coretypes.Issue;
import com.ripple.core.coretypes.STArray;

import marketmaker.entities.Channels;
import marketmaker.entities.OfferBook;

/*
 * Created by rmartins on 2/18/15.
 */
public class RippleOfferBookPublisher extends BaseRippleClient implements InitializingBean {
	private static Logger log = LoggerFactory.getLogger(RippleOfferBookPublisher.class);

	@Autowired
	private JmsTemplate template;

	private OfferBook offerBook;
	private Issue getIssue;
	private Issue payIssue;
	
	private Request request;
	
	public RippleOfferBookPublisher() {
	}

	// public RippleOfferBookPublisher(Issue getIssue, Issue payIssue) {
	// this.getIssue = getIssue;
	// this.payIssue = payIssue;
	// this.offerBook = new OfferBook(getIssue, payIssue);
	// }

	@Scheduled(fixedDelay = 18000, initialDelay = 0)
	public void run() {
		refreshSetup();
		publish();
	}

	public void publish() {

		if (this.getIssue == null || this.payIssue == null) {
			return;
		}

		for (int i = 0; i < 2; i++) {
			final boolean getAsks = i == 0, getBids = !getAsks;

			Issue getIssue = getAsks ? this.payIssue : this.getIssue,
					payIssue = getAsks ? this.getIssue : this.payIssue;

			Request request = client().requestBookOffers(getIssue, payIssue);
			request.once(Request.OnResponse.class, new Request.OnResponse() {
				@Override
				public void called(Response response) {
					if (response.succeeded) {
						log.info(getAsks == true ? "ASK " : "BID " + response.result.toString());
						JSONArray offersJSON = response.result.optJSONArray("offers");
						STArray offers = STArray.translate.fromJSONArray(offersJSON);
						if (getAsks) {
							offerBook.setOffersBids(offersJSON);
						} else {
							offerBook.setOffersAsks(offersJSON);
						}

						if (offerBook.retrievedBothBooks()) {
							template.convertAndSend(Channels.OFFERBOOK, offerBook.toJSONObject().toString());
						}
					} else {
						log.error("Error processing OfferBook.");
					}
				}
			});
			request.request();
		}

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		refreshSetup();
		// publish();
	}

	private void refreshSetup() {
		this.getIssue = Issue.fromString(avalancheSetup().getBaseAsset());
		this.payIssue = Issue.fromString(avalancheSetup().getCounterAsset());
		this.offerBook = new OfferBook(getIssue, payIssue);
		publish();
	}

}
