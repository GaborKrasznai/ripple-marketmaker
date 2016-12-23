package marketmaker.services.ripple;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONException;
import org.json.JSONObject;
import org.ripple.bouncycastle.crypto.InvalidCipherTextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jms.annotation.JmsListener;

import com.ripple.client.Account;
import com.ripple.client.responses.Response;
import com.ripple.client.transactions.ManagedTxn;
import com.ripple.client.transactions.TransactionManager;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.serialized.enums.TransactionType;
import com.ripple.core.types.known.tx.Transaction;
import com.ripple.core.types.known.tx.result.TransactionResult;
import com.ripple.core.types.known.tx.txns.OfferCreate;

/**
 * Created by rmartins on 8/3/15.
 */
public class RippleOfferCreateListener extends BaseRippleClient implements InitializingBean {

	private static Logger log = LoggerFactory.getLogger(RippleOfferCreateListener.class);

	private String baseAsset;
	private boolean enableOpportunityTaker;
	private int maxOrdersOpenAsks;
	private int maxOrdersOpenBids;

	private JSONObject accountOffers;
	private AtomicInteger countCreateBids = new AtomicInteger();
	private AtomicInteger countCreateAsks = new AtomicInteger();
	private AtomicInteger countOffers = new AtomicInteger();
	private BigDecimal maxOpenAsks;
	private BigDecimal maxOpenBids;

	protected Account rippleAccount;
	private UInt32 sequence = UInt32.ZERO;

	public RippleOfferCreateListener() {

	}

	public void refresh() throws Exception {
		this.maxOpenAsks = avalancheSetup().getMaxOpenAsks();
		this.maxOpenBids = avalancheSetup().getMaxOpenBids();
		this.baseAsset = avalancheSetup().getBaseAsset();
		this.enableOpportunityTaker = avalancheSetup().isEnableOpportunityTaker();
		rippleAccount = account();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		refresh();

	}

	@JmsListener(destination = "account_offers")
	public void processAccountOffers(String message) throws Exception {
		refresh();
		JSONObject json = new JSONObject(message);
		this.accountOffers = json;
		JSONObject count = accountOffers.getJSONObject("count");
		this.countCreateAsks.set(count.getInt("countOpenAsks"));
		this.countCreateBids.set(count.getInt("countOpenBids"));
		this.countOffers.set(count.getInt("countOffers"));

		log.info("processAccountOffers countCreateAsks " + countCreateAsks + " countCreateBids " + countCreateBids);
	}

	@JmsListener(destination = "offer_create")
	public void processOpportunity(String message) throws Exception {
		refresh();
		JSONObject json = new JSONObject(message);
		OfferCreate offer = (OfferCreate) OfferCreate.fromJSONObject(json);
		checkMaxRequestOffer(account(), transactionManager(), offer, maxOpenAsks.intValue(), countCreateAsks.intValue(),
				maxOpenBids.intValue(), countCreateBids.intValue());
	}

	@JmsListener(destination = "instruments")
	public void processInstruments(String message) throws JSONException {

		JSONObject json = new JSONObject(message);
		updateInstruments(json);
	}

	public void checkMaxRequestOffer(Account account, TransactionManager transactionManager, OfferCreate offer,
			int maxOrdersOpenAsks, int countCreateAsks, int maxOrdersOpenBids, int countCreateBids)
			throws InvalidCipherTextException, JSONException, IOException {

		log.info("checkMaxRequestOffer maxOrdersOpenAsks: " + maxOrdersOpenAsks + " countCreateAsks: " + countCreateAsks
				+ " maxOrdersOpenBids: " + maxOrdersOpenBids + " countCreateBids: " + countCreateBids + " Offer: "
				+ offer.accountTxnID());

		int x = countTransactionManagerOfferCancel(transactionManager);
		int c = transactionManager.txnsPending() - x;
		int count = (maxOrdersOpenAsks - countCreateAsks) + (maxOrdersOpenBids - countCreateBids);
		if (c > count) {
			log.warn("transactionManager " + c + " countAsks " + countCreateAsks + " countBids " + countCreateBids
					+ " countCancel " + x);
			return;
		}

		boolean isAsk = RippleAccountOffersPublisher.isOfferAsk(offer, baseAsset);
		if (isAsk) {
			if (countCreateAsks >= maxOrdersOpenAsks) {
				log.info("Max countCreateAsks " + countCreateAsks);
				return;
			}
		} else {
			if (countCreateBids >= maxOrdersOpenBids) {
				log.info("Max countCreateBids " + countCreateBids);
				return;
			}
		}

		if (enableOpportunityTaker) {
			prepareAndQueueOfferCreate(offer, isAsk);
		}
	}

	private int countTransactionManagerOfferCancel(TransactionManager transactionManager) {
		Iterator<ManagedTxn> it = transactionManager.getPending().iterator();
		int a = 0;
		while (it.hasNext()) {
			ManagedTxn t = it.next();
			if (t.transactionType() == TransactionType.OfferCancel) {
				a++;
			}
		}
		return a;
	}

	public void prepareAndQueueOfferCreate(Transaction transaction, boolean isAsk)
			throws InvalidCipherTextException, JSONException, IOException {
		ManagedTxn txn = new ManagedTxn(transaction);
		Amount fee = client().serverInfo.transactionFee(txn.txn);
		UInt32 sequence = new UInt32(transactionManager().sequence);
		txn.prepare(account().keyPair, fee, sequence, null);
		log.info("OfferCreate Sequence: " + txn.sequence() + " " + txn.description());
		txn.once(ManagedTxn.OnSubmitSuccess.class, new ManagedTxn.OnSubmitSuccess() {
			@Override
			public void called(Response response) {
				log.info("OfferCreate isAsk: " + isAsk + " send :" + response.message + " " + response.status);
			}
		});
		txn.once(ManagedTxn.OnSubmitFailure.class, new ManagedTxn.OnSubmitFailure() {
			@Override
			public void called(Response response) {
				log.info("OfferCreate isAsk: " + isAsk + " error :" + response.message + " " + response.error_message
						+ " " + response.status);
			}
		});
		txn.once(ManagedTxn.OnTransactionValidated.class, new ManagedTxn.OnTransactionValidated() {
			@Override
			public void called(TransactionResult result) {
				log.info("OfferCreate submitted: " + result.message + " Sequence: " + result.txn.sequence());
			}
		});

		log.info("Transaction Manager " + txn.toString());
		transactionManager.queue(txn);
	}

	public int getMaxOrdersOpenAsks() {
		return maxOrdersOpenAsks;
	}

	public void setMaxOrdersOpenAsks(int maxOrdersOpenAsks) {
		this.maxOrdersOpenAsks = maxOrdersOpenAsks;
	}

	public int getMaxOrdersOpenBids() {
		return maxOrdersOpenBids;
	}

	public void setMaxOrdersOpenBids(int maxOrdersOpenBids) {
		this.maxOrdersOpenBids = maxOrdersOpenBids;
	}

	public String getBaseAsset() {
		return baseAsset;
	}

	public void setBaseAsset(String baseAsset) {
		this.baseAsset = baseAsset;
	}

	private void updateInstruments(JSONObject values) {
		if (values.has("enableOpportunityTaker"))
			this.enableOpportunityTaker = values.getBoolean("enableOpportunityTaker");
		if (values.has("maxOpenAsks"))
			this.maxOrdersOpenAsks = values.getInt("maxOpenAsks");
		if (values.has("maxOpenBids"))
			this.maxOrdersOpenBids = values.getInt("maxOpenBids");
		if (values.has("baseAsset"))
			this.baseAsset = values.getString("baseAsset");
	}

}
