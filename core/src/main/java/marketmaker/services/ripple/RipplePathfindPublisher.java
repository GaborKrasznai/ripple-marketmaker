package marketmaker.services.ripple;

import java.math.BigDecimal;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import com.ripple.client.enums.Command;
import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.core.coretypes.Issue;

import marketmaker.entities.Channels;

/**
 * Created by rmartins on 4/19/15.
 */
public class RipplePathfindPublisher extends BaseRippleClient implements InitializingBean {
	private static Logger log = LoggerFactory.getLogger(RipplePathfindPublisher.class);

	@Autowired
	private JmsTemplate template;

	private BigDecimal amount;
	private String pathFrom;
	private String pathTo;
	private Issue baseAsset;

	private Request request;

	public RipplePathfindPublisher() {

	}

	private void refreshSetup() {
		this.amount = avalancheSetup().getBaseAmount();
		this.pathTo = avalancheSetup().getPathTo();
		this.pathFrom = avalancheSetup().getPathFrom();
		this.baseAsset = Issue.fromString(avalancheSetup().getBaseAsset());
	}

	@Scheduled(fixedDelay = 18000)
	public void run() {
		refreshSetup();
		if (this.pathFrom == null || this.amount == null || this.pathTo == null) {
			return;
		}

		request = createPathfindStatusRequest();
		request.request();
	}

	private Request createPathfindStatusRequest() {
		return createPathfindStatusRequest(new Request.OnResponse() {
			@Override
			public void called(Response response) {
				JSONObject result = response.message.optJSONObject("result");
				if (result == null) {
					log.info(response.message.toString() + " " + response.error_message);
				}
				try {
					processMessage(result);
				} catch (JSONException e) {
					log.error(e.getMessage());
				}
			}
		});
	}

	public Request createPathfindStatusRequest(Request.OnResponse onResponse) {
		Request request = client().newRequest(Command.path_find);
		try {
			request.json().put("subcommand", "status");
		} catch (JSONException e) {
			e.printStackTrace();
			log.error("createPathfindStatusRequest exception " + e.getMessage());
		}

		request.on(Request.OnResponse.class, onResponse);
		return request;
	}

	private void processMessage(JSONObject result) throws JSONException {
		if (result != null) {
			log.info("full_reply false" + result.toString());
			if (result.getBoolean("full_reply")) {
				template.convertAndSend(Channels.PATH_FIND, result.toString());
				subscribePathfind(pathFrom, pathTo, baseAsset, amount);
				log.info(result.toString());
				template.convertAndSend(Channels.PATH_FIND, result.toString());
			}
		}
	}

	public Request subscribePathfind(String pathFrom, String pathTo, Issue issue, BigDecimal amount) {
		client().disconnect();
		Request request = client().newRequest(Command.path_find);
		try {
			request.json().put("id", new Random().nextInt());
			request.json().put("subcommand", "create");
			request.json().put("source_account", pathTo);
			request.json().put("destination_account", pathFrom);

			if (!Issue.XRP.currency().toString().equals(issue.currency().toString())) {
				JSONObject path_find = new JSONObject();
				path_find.put("value", amount);
				path_find.put("currency", issue.currency().toString());
				path_find.put("issuer", issue.issuer().address);
				request.json().put("destination_amount", path_find);
			} else {
				request.json().put("destination_amount", amount);
			}

			request.on(Request.OnResponse.class, new Request.OnResponse() {
				@Override
				public void called(Response response) {
					log.info("OnResponse " + response.message.toString());
				}
			});
			log.info("Pathfind Request: " + request.json().toString());
			request.request();

		} catch (JSONException e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}

		return request;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		refreshSetup();
		subscribePathfind(pathFrom, pathTo, baseAsset, amount);
		if (this.pathFrom != null || this.pathTo != null || this.amount != null || this.pathTo != null) {
			subscribePathfind(pathFrom, pathTo, baseAsset, amount);
		}
	}
}