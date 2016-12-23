package marketmaker.services.ripple;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import com.ripple.client.Client;
import com.ripple.client.enums.Command;
import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;
import com.ripple.core.coretypes.Issue;

import marketmaker.entities.Channels;
import marketmaker.entities.Pathfind;

/**
 * Created by rmartins on 4/19/15.
 */
public class RippleMultiPathfindPublisher implements InitializingBean {
	private static Logger log = LoggerFactory.getLogger(RippleMultiPathfindPublisher.class);

	@Autowired
	private JmsTemplate template;

	@Value("${ripplemm.rippled}")
	private String rippled;

	private HashMap<Pathfind, Client> listClient = new HashMap<>();
	private HashMap<Pathfind, Request> listRequest = new HashMap<>();
	private Request requestStatus;
	private List<Pathfind> listPathfinds;

	public RippleMultiPathfindPublisher() {

	}

	public RippleMultiPathfindPublisher(List<Pathfind> pathfinds) {
		this.listPathfinds = pathfinds;
	}

	private Client createClientRequest() {
		Client client = new Client(new JavaWebSocketTransportImpl());
		client.connect(rippled, new Client.OnConnected() {
			@Override
			public void called(Client client) {
				log.info("Connected to Ripple Network!");
			}
		});

		return client;
	}

	public void subscribePathfind(Pathfind pathfind) {
		Client clientRequest = createClientRequest();
		listClient.put(pathfind, clientRequest);
		Request subscribePathfindRequest = subscribePathfind(clientRequest, pathfind.getPathFrom(),
				pathfind.getPathTo(), pathfind.getBaseAsset(), pathfind.getAmount());
		listRequest.put(pathfind, subscribePathfindRequest);
		requestStatus = createPathfindStatusRequest(clientRequest, pathfind);
		requestStatus.request();
	}

	@Scheduled(fixedDelay = 1800)
	public void run() {
		for (Pathfind pathfind : listClient.keySet()) {
			requestStatus = createPathfindStatusRequest(listClient.get(pathfind), pathfind);
			requestStatus.request();
		}
	}

	public Request createPathfindStatusRequest(Client client, Pathfind pathfind) {
		return createPathfindStatusRequest(client, pathfind, new Request.OnResponse() {
			@Override
			public void called(Response response) {
				JSONObject result = response.message.optJSONObject("result");
				try {
					processMessage(result, pathfind);
				} catch (JSONException e) {
					log.error(e.getMessage());
				}
			}
		});
	}

	public Request createPathfindStatusRequest(Client client, Pathfind pathfind, Request.OnResponse onResponse) {
		Request request = client.newRequest(Command.path_find);
		try {
			request.json().put("subcommand", "status");
		} catch (JSONException e) {
			e.printStackTrace();
			log.error("createPathfindStatusRequest exception " + e.getMessage());
		}

		request.on(Request.OnResponse.class, onResponse);
		return request;
	}

	private void processMessage(JSONObject result, Pathfind pathfind) throws JSONException {
		if (result != null) {
			pathfind.setResult(result);
			template.convertAndSend(Channels.PATH_FIND, pathfind.toJSONObject().toString());
			// TODO ripple_pathfind

		}
	}

	public Request subscribePathfind(Client client, String pathFrom, String pathTo, Issue issue, Double amount) {
		Request request = client.newRequest(Command.path_find);
		try {
			request.json().put("id", new Random().nextInt());
			request.json().put("subcommand", "create");
			request.json().put("source_account", pathTo);
			request.json().put("destination_account", pathFrom);

			if (!Issue.XRP.currency().toString().equals(issue.currency().toString())) {
				JSONObject path_find = new JSONObject();
				path_find.put("value", String.valueOf(amount));
				path_find.put("currency", issue.currency().toString());
				path_find.put("issuer", issue.issuer().address);
				request.json().put("destination_amount", path_find);
			} else {
				request.json().put("destination_amount", amount);
			}

			request.on(Request.OnResponse.class, new Request.OnResponse() {
				@Override
				public void called(Response response) {
					log.info("Pathfind Request PathFrom: " + pathFrom + " PathTo: " + pathTo + " Issue: "
							+ issue.toString() + " Amount: " + amount + " Response: " + response.message.toString());
				}
			});
			log.info("Pathfind Request PathFrom: " + pathFrom + " PathTo: " + pathTo + " Issue: " + issue.toString()
					+ " Amount: " + amount + " Request: " + request.json().toString());
			request.request();

		} catch (JSONException e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}

		return request;
	}

	public void processPathfinds(List<Pathfind> pathfinds) {
		this.listPathfinds = pathfinds;
		for (Pathfind pathfind : pathfinds) {
			subscribePathfind(pathfind);
		}
	}

	@Override
	public void afterPropertiesSet() {
		if (this.listPathfinds != null) {
			processPathfinds(this.listPathfinds);
		}
	}
}