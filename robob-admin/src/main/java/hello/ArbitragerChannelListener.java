package hello;

import java.util.Calendar;

import org.hornetq.utils.json.JSONException;
import org.hornetq.utils.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class ArbitragerChannelListener {

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private SimpMessagingTemplate clientTemplate;

	@JmsListener(id = "arbitrager", destination = "arbitrager")
	public void onArbitrager(String message) {
		if (clientTemplate != null) {
			clientTemplate.convertAndSend("/topic/greetings", message);
		}
	}

	@JmsListener(id = "payment_create", destination = "payment_create")
	public void onRipplePayment(String message) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("channel", "payment_create");
		json.put("content", message);
		json.put("timestamp", Calendar.getInstance().getTimeInMillis());

		if (clientTemplate != null) {
			clientTemplate.convertAndSend("/topic/payments", json.toString());
		}
	}

	@JmsListener(id = "bitstamp_offercreate", destination = "bitstamp_offercreate")
	public void onBitstampOfferCreate(String message) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("channel", "bitstamp_offercreate");
		json.put("content", message);
		json.put("timestamp", Calendar.getInstance().getTimeInMillis());

		if (clientTemplate != null) {
			clientTemplate.convertAndSend("/topic/payments", json.toString());
		}
	}

	@JmsListener(id = "pathfind_pair", destination = "pathfind_pair")
	public void onRipplePathfindPair(String json) {
		if (clientTemplate != null) {
			clientTemplate.convertAndSend("/topic/pathfinds", json);
		}
	}

	@JmsListener(id = "bitstamp_orderbook", destination = "bitstamp_orderbook")
	public void onBitstampOrderBook(String json) {
		if (clientTemplate != null) {
			clientTemplate.convertAndSend("/topic/orderbook", json);
		}
	}

}
