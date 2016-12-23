package marketmaker.services.ripple;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.ripple.bouncycastle.crypto.InvalidCipherTextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;

import com.ripple.client.Account;
import com.ripple.client.Client;
import com.ripple.client.responses.Response;
import com.ripple.client.transactions.ManagedTxn;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.fields.Field;
import com.ripple.core.types.known.tx.Transaction;
import com.ripple.core.types.known.tx.result.TransactionResult;
import com.ripple.core.types.known.tx.txns.OfferCancel;

/**
 * Created by rmartins on 8/4/15.
 */
public class RippleOfferCancelListener extends BaseRippleClient implements InitializingBean {

	private static Logger log = LoggerFactory.getLogger(RippleOfferCancelListener.class);

	protected Account rippleAccount;

	public RippleOfferCancelListener() {

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		rippleAccount = account();
	}

	@JmsListener(destination = "offer_cancel")
	public void processOfferCancel(String message) throws JSONException, InvalidCipherTextException, IOException {
		JSONObject json = new JSONObject(message);
		OfferCancel offerCancel = new OfferCancel();
		offerCancel.account(rippleAccount.id());
		UInt32 seq = new UInt32(json.optString("seq"));
		offerCancel.put(Field.OfferSequence, seq);
		prepareAndQueueOfferCancel(offerCancel);
	}

	public void prepareAndQueueOfferCancel(Transaction transaction)
			throws InvalidCipherTextException, JSONException, IOException {
		ManagedTxn txn = new ManagedTxn(transaction);
		Amount fee = client.serverInfo.transactionFee(txn.txn);
		UInt32 sequence = new UInt32(transaction.get(Field.OfferSequence).toBytes());
		sequence = new UInt32(java.lang.Math.negateExact(sequence.intValue()));
		txn.prepare(rippleAccount.keyPair, fee, sequence, null);

		txn.once(ManagedTxn.OnSubmitSuccess.class, new ManagedTxn.OnSubmitSuccess() {
			@Override
			public void called(Response response) {
				log.debug("OfferCancel submitted: " + response.message);
				// if (isAsk) {
				// countCreateAsks.incrementAndGet();
				// } else {
				// countCreateBids.incrementAndGet();
				// }
			}
		});
		txn.once(ManagedTxn.OnSubmitFailure.class, new ManagedTxn.OnSubmitFailure() {
			@Override
			public void called(Response response) {
				log.error("OfferCancel error: " + response.error_message + " " + response.message + " "
						+ response.status);
			}
		});
		txn.once(ManagedTxn.OnTransactionValidated.class, new ManagedTxn.OnTransactionValidated() {
			@Override
			public void called(TransactionResult result) {
				log.info("OfferCancel submitted: " + result.message + " Sequence: " + result.txn.sequence());
			}
		});

		transactionManager().queue(txn);
	}

	@JmsListener(destination = "instruments")
	public void processInstruments(String message) throws JSONException {
		JSONObject json = new JSONObject(message);
		updateInstruments(json);
	}

	private void updateInstruments(JSONObject values) {
	}

}
