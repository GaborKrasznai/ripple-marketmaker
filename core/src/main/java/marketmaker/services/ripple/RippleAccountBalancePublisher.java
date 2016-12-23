package marketmaker.services.ripple;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import com.ripple.client.Account;
import com.ripple.client.Client;
import com.ripple.client.enums.Command;
import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.core.coretypes.AccountID;

import marketmaker.entities.Channels;

/**
 * Created by rmartins on 5/28/15.
 */
public class RippleAccountBalancePublisher extends BaseRippleClient implements InitializingBean {
	private static Logger log = LoggerFactory.getLogger(RippleAccountBalancePublisher.class);

	@Autowired
	private JmsTemplate template;

	private Account rippleAccount;
	private Request request;
	
	public RippleAccountBalancePublisher() {
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		rippleAccount = account();
		log.info("RippleAccountBalancePublisher " + rippleAccount.id());
//		Request request = subscribeAccountBalance(rippleAccount.id());
//		request.request();
	}

	@Scheduled(fixedDelay = 12000, initialDelay = 0)
	public void run() throws JSONException {
		request = subscribeAccountBalance(rippleAccount.id());
		request.request();
	}

	public Request subscribeAccountBalance(AccountID accountId) throws JSONException {

		Request request = client.newRequest(Command.account_lines);
		request.json().put("account", accountId);
		request.json().put("ledger_index", "current");

		request.on(Request.OnResponse.class, new Request.OnResponse() {
			@Override
			public void called(Response response) {
				JSONObject balance = response.message;
				log.info("Account Balance : " + balance.toString());
				template.convertAndSend(Channels.ACCOUNT_BALANCE, response.message.toString());
			}
		});

		return request;
	}

}
