package marketmaker.services.ripple;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;

import com.ripple.client.Account;
import com.ripple.client.responses.Response;
import com.ripple.client.transactions.ManagedTxn;
import com.ripple.client.transactions.TransactionManager;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.serialized.enums.TransactionType;
import com.ripple.core.types.known.tx.result.TransactionResult;
import com.ripple.core.types.known.tx.txns.Payment;

/**
 * Created by rmartins on 8/3/15.
 */
public class RipplePaymentCreateListener extends BaseRippleClient implements InitializingBean {

	private static Logger log = LoggerFactory.getLogger(RipplePaymentCreateListener.class);

	@Value("${ripple.arbitrager.maxOpenOffers}")
	private int maxOpenOffers;
	private int currentOpenOffers = 0;

	private JSONObject accountOffers;

	@Autowired
	protected TransactionManager transactionManager;

	protected Account rippleAccount;

	@Value("${ripplemm.arbitrager.createoffers.enabled}")
	private boolean arbitragerCreateOffersEnabled = false;

	public RipplePaymentCreateListener() {

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		rippleAccount = account();
	}

	@JmsListener(destination = "account_offers")
	public void processAccountOffers(String message) throws JSONException {
		JSONObject json = new JSONObject(message);
		this.accountOffers = json;
		JSONObject count = accountOffers.getJSONObject("count");

		log.info("processAccountOffers Count: " + count);
	}

	@JmsListener(destination = "payment_create")
	public void processOpportunity(String message) throws JSONException {
		JSONObject json = new JSONObject(message);
		Payment payment = (Payment) Payment.fromJSONObject(json);

		checkMaxRequestPayment(rippleAccount, transactionManager, payment, maxOpenOffers, currentOpenOffers);
	}

	public void checkMaxRequestPayment(Account account, TransactionManager transactionManager, Payment payment,
			int maxOrdersOffers, int currentOpenOffers) {

		log.info("checkMaxRequestPayment maxOrdersOffers: " + maxOrdersOffers + " currentOpenOffers: "
				+ currentOpenOffers + " Payment: " + payment.accountTxnID());

		int x = countTransactionManagerPayment(transactionManager);
		int c = transactionManager.txnsPending() - x;
		if (c > currentOpenOffers) {
			log.warn("transactionManager " + c + " currentOpenOffers: " + currentOpenOffers);
			return;
		}

		// TODO check and count for ask and bid

		if (arbitragerCreateOffersEnabled) {
			prepareAndQueuePayment(payment);
		}
	}

	private int countTransactionManagerPayment(TransactionManager transactionManager) {
		Iterator<ManagedTxn> it = transactionManager.getPending().iterator();
		int a = 0;
		while (it.hasNext()) {
			ManagedTxn t = it.next();
			if (t.transactionType() == TransactionType.Payment) {
				a++;
			}
		}
		return a;
	}

	public void prepareAndQueuePayment(Payment payment) {
		ManagedTxn txn = new ManagedTxn(payment);
		Amount fee = client().serverInfo.transactionFee(txn.txn);
		UInt32 sequence = new UInt32(transactionManager.sequence);
		txn.prepare(rippleAccount.keyPair, fee, sequence, null);
		log.info("Payment Sequence: " + txn.sequence() + " Hash: " + txn.hash.toHex());
		txn.once(ManagedTxn.OnSubmitSuccess.class, new ManagedTxn.OnSubmitSuccess() {
			@Override
			public void called(Response response) {
				log.info("Payment send :" + response.message + " " + response.status);
			}
		});
		txn.once(ManagedTxn.OnSubmitFailure.class, new ManagedTxn.OnSubmitFailure() {
			@Override
			public void called(Response response) {
				log.info("Payment error :" + response.message + " " + response.error_message + " " + response.status);
			}
		});
		txn.once(ManagedTxn.OnTransactionValidated.class, new ManagedTxn.OnTransactionValidated() {
			@Override
			public void called(TransactionResult result) {
				log.info("Payment submitted: " + result.message + " Sequence: " + result.txn.sequence());
			}
		});

		transactionManager.queue(txn);
	}
}
