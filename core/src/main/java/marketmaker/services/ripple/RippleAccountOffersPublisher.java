package marketmaker.services.ripple;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import com.ripple.client.Account;
import com.ripple.client.Client;
import com.ripple.client.enums.Command;
import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.core.coretypes.Issue;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.fields.Field;
import com.ripple.core.types.known.tx.txns.OfferCancel;
import com.ripple.core.types.known.tx.txns.OfferCreate;

import marketmaker.entities.Channels;

/**
 * Created by rmartins on 5/21/15.
 */
public class RippleAccountOffersPublisher extends BaseRippleClient implements InitializingBean {
	private static Logger log = LoggerFactory.getLogger(RippleAccountOffersPublisher.class);

	@Autowired
	private JmsTemplate template;
	private Request request;

	private Account rippleAccount;
	
	public RippleAccountOffersPublisher() {
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		rippleAccount = account();
		log.info("RippleAccountOffersPublisher " + rippleAccount.id());
		// Request request = subscribeAccountOffers(rippleAccount);
		// request.request();
	}

	@Scheduled(fixedDelay = 6000, initialDelay = 0)
	public void run() throws JSONException {
		request = subscribeAccountOffers(rippleAccount);
		request.request();
	}

	public Request subscribeAccountOffers(Account account) throws JSONException {

		Request request = client.newRequest(Command.account_offers);
		request.json().put("account", account.id());
		request.json().put("ledger_index", "current");

		request.on(Request.OnResponse.class, new Request.OnResponse() {
			@Override
			public void called(Response response) {
				// log.info("OnResponse " + response.message.toString());
				try {
					processMessage(response);
				} catch (JSONException e) {
					e.printStackTrace();
					log.error(e.getMessage());
				}
			}
		});
		return request;
	}

	private void processMessage(Response response) throws JSONException {
		JSONObject offers = response.message;

		if (offers.optJSONObject("result") != null) {
			JSONObject count = countOffers(offers.getJSONObject("result").getJSONArray("offers"));
			offers.put("count", count);

			template.convertAndSend(Channels.ACCOUNT_OFFERS, offers.toString());

			boolean cancelAllAccountOffersOnStart = avalancheSetup().isCancelAllAccountOffersOnStart();
			if (cancelAllAccountOffersOnStart) {
				log.info("cancelAllAccountOffers" + cancelAllAccountOffersOnStart);
				// TODO change it
				JSONArray listOffers = offers.getJSONObject("result").getJSONArray("offers");
				for (int i = 0; i < listOffers.length(); i++) {
					template.convertAndSend(Channels.OFFER_CANCEL, listOffers.get(i).toString());
				}
			}

			cancelAllAccountOffersOnStart = false;
		} else {
			log.info("Message without result: " + offers.toString());
		}
	}

	public static boolean isOfferAsk(OfferCreate offer, String baseAsset) {
		String base = Issue.fromString(baseAsset).currency().toString();

		if (baseAsset.equals(offer.takerGets().currency().toString())) {
			return true;
		}
		return false;

	}

	public static boolean isOfferAsk(JSONObject offer, String baseAsset) {
		String base = Issue.fromString(baseAsset).currency().toString();

		JSONObject takerGets = offer.optJSONObject("taker_gets");
		String currency;
		if (takerGets == null) {
			currency = Issue.XRP.currency().toString();
		} else {
			currency = takerGets.getString("currency");
		}

		if (currency.equals(base)) {
			return true;
		}

		return false;
	}

	private JSONObject countOffers(JSONArray offers) throws JSONException {
		int offerBids = 0;
		int offerAsks = 0;
		try {
			JSONObject result = new JSONObject();

			String baseAsset = avalancheSetup().getBaseAsset();
			String counterAsset = avalancheSetup().getCounterAsset();

			String base = Issue.fromString(baseAsset).currency().toString();
			String counter = Issue.fromString(counterAsset).currency().toString();

			int offerLenght = offers.length();
			for (int x = 0; x < offerLenght; x++) {
				JSONObject offer = offers.getJSONObject(x);
				JSONObject takerGets = offer.optJSONObject("taker_gets");
				String currency;
				if (takerGets == null) {
					currency = Issue.XRP.currency().toString();
				} else {
					currency = takerGets.getString("currency");
				}

				if (currency.equals(base)) {
					// comprando baseAsset
					offerAsks++;
				} else if (currency.equals(counter)) {
					// vendendo counterAsset
					offerBids++;
				}
			}

			result.put("countOffers", offers.length());
			result.put("countOpenBids", offerBids);
			result.put("countOpenAsks", offerAsks);

			return result;

		} catch (JSONException jex) {
			log.error(jex.getMessage());
			jex.printStackTrace();
			throw jex;
		}

	}

	private void cancelAllOffers(JSONObject data) throws JSONException {
		JSONArray offers = data.optJSONArray("offers");
		if (offers == null) {
			log.info("There is no offer for account" + data.toString(4));
			return;
		}

		int offersLength = offers.length();
		for (int i = 0; i < offersLength; i++) {
			JSONObject offer = offers.getJSONObject(i);
			OfferCancel offerCancel = new OfferCancel();
			offerCancel.account(rippleAccount.id());
			offerCancel.put(Field.OfferSequence, new UInt32(offer.optString("seq")));
			log.info("Cancell All Offers " + offerCancel.toJSON());
			template.convertAndSend(Channels.OFFER_CANCEL, offerCancel.toJSON().toString());
		}

	}

	@JmsListener(destination = "instruments")
	public void processInstruments(String message) throws JSONException {
		JSONObject json = new JSONObject(message);
		updateInstruments(json);
	}

	private void updateInstruments(JSONObject values) {
		// if (values.has("ripplemm.cancelAllAccountOffersOnStart"))
		// this.cancelAllAccountOffersOnStart =
		// values.getBoolean("ripplemm.cancelAllAccountOffersOnStart");
	}

}
