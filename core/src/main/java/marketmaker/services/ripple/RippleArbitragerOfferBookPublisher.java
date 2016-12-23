package marketmaker.services.ripple;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.javatuples.Pair;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.core.coretypes.Issue;

import marketmaker.entities.Channels;
import marketmaker.services.algorithms.RippleArbitrager;

/*
 * Created by rmartins on 2/18/15.
 */
public class RippleArbitragerOfferBookPublisher extends BaseRippleClient implements InitializingBean {
	private static Logger log = LoggerFactory.getLogger(RippleArbitragerOfferBookPublisher.class);

	@Autowired
	private JmsTemplate template;

	@Value("${ripplemm.arbitrager.currencies}")
	private String currencies;

	private HashMap<Pair<Issue, Issue>, double[]> listValues;

	public RippleArbitragerOfferBookPublisher() {
	}

	@Scheduled(fixedDelay = 20000)
	public void run() {
		publishAll();
	}

	public void publishAll() {
		Set<Pair<Issue, Issue>> keySet = listValues.keySet();
		for (Iterator<Pair<Issue, Issue>> iterator = keySet.iterator(); iterator.hasNext();) {
			Pair<Issue, Issue> pair = iterator.next();
			publish(pair.getValue0(), pair.getValue1());
		}
	}

	public void publish(Issue getIssue, Issue payIssue) {
		log.info("Request OfferBook getIssue: " + getIssue.toString() + " payIssue: " + payIssue.toString());
		if (getIssue == null || payIssue == null) {
			return;
		}

		Request request = client().requestBookOffers(getIssue, payIssue);
		request.once(Request.OnResponse.class, new Request.OnResponse() {
			@Override
			public void called(Response response) {
				if (response.succeeded) {
					final JSONObject json = new JSONObject();
					json.put("offers", response.result.optJSONArray("offers"));
					json.put("getIssue", getIssue);
					json.put("payIssue", payIssue);
					if (json.optJSONArray("offers").length() > 0) {
						log.info("OfferBook " + json.toString());
						template.convertAndSend(Channels.OFFERBOOK, json.toString());
					} else {
						log.info("OfferBook without offers: " + json.toString());
					}

				} else {
					log.error("Error processing OfferBook. getIssue: " + getIssue.toString() + " payIssue: "
							+ payIssue.toString() + response);
				}
			}
		});
		request.request();

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		listValues = new RippleArbitrager().permutation(currencies.split(","));
		publishAll();
	}

}
