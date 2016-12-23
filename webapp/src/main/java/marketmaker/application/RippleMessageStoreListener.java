package marketmaker.application;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;

import com.ripple.core.types.known.tx.txns.OfferCreate;

import marketmaker.entities.AccountBalance;
import marketmaker.entities.AccountBalanceRepository;
import marketmaker.entities.AccountOffer;
import marketmaker.entities.AccountOfferRepository;
import marketmaker.entities.Avalanche;
import marketmaker.entities.AvalancheRepository;
import marketmaker.entities.Offer;
import marketmaker.entities.OfferCreateEntity;
import marketmaker.entities.OfferCreateEntityRepository;
import marketmaker.entities.OfferRepository;

/**
 * Created by bob on 09/01/15.
 */
public class RippleMessageStoreListener {

	private static Logger log = LoggerFactory.getLogger(RippleMessageStoreListener.class);
	private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

	@Autowired
	private AccountOfferRepository accountOfferRepository;
	@Autowired
	private AccountBalanceRepository accountBalanceRepository;
	// @Autowired
	// private OfferBookRepository offerBookRepository;
	@Autowired
	private OfferRepository offerRepository;
	@Autowired
	private AvalancheRepository avalancheRepository;
	@Autowired
	private OfferCreateEntityRepository offerCreateEntityRepository;

	public RippleMessageStoreListener() {
	}

	@JmsListener(destination = "offer_create")
	public void onTransaction(String message) {
		try {
			JSONObject json = new JSONObject(message);
			OfferCreate offer = (OfferCreate) OfferCreate.fromJSONObject(json);
			OfferCreateEntity e = new OfferCreateEntity();
			e.setCreatedAt(Calendar.getInstance().getTime());
			if (offer.expiration() != null)
				e.setExpiration(offer.expiration().toString());
			if (offer.sequence() != null)
				e.setOfferSequence(offer.sequence().toString());
			if (offer.takerGets() != null) {
				e.setTakerGetsCurrency(offer.takerGets().currencyString());

				e.setTakerGetsIssuer(offer.takerGets().issuerString());
				e.setTakerGetsValue(offer.takerGets().value());
			}
			if (offer.takerPays() != null) {
				e.setTakerPaysCurrency(offer.takerPays().currencyString());
				e.setTakerPaysIssuer(offer.takerPays().issuerString());
				e.setTakerPaysValue(offer.takerPays().value());
			}
			offerCreateEntityRepository.save(e);

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	@JmsListener(destination = "avalanche")
	public void onAvalanche(String message) {
		try {
			JSONObject json = new JSONObject(message);
			Avalanche avalanche = new Avalanche();
			avalanche.setCreatedAt(Calendar.getInstance().getTime());
			avalanche.setBaseAmount(new BigDecimal(json.getDouble("baseAmount")));
			avalanche.setBaseAsset(json.getString("baseAsset"));
			avalanche.setBaseExpo(new BigDecimal(json.getDouble("baseExpo")));
			avalanche.setCancelAllAccountOffersOnStart(json.getBoolean("cancelAllAccountOffersOnStart"));
			avalanche.setCounterAsset(json.getString("counterAsset"));
			avalanche.setCounterExpo(new BigDecimal(json.getDouble("counterExpo")));
			avalanche.setDegreeAsk(new BigDecimal(json.getDouble("degreeAsk")));
			avalanche.setDegreeBid(new BigDecimal(json.getDouble("degreeBid")));
			avalanche.setEnableOpportunityTaker(json.getBoolean("enableOpportunityTaker"));
			avalanche.setFilteredListAsks(json.get("filteredListAsks").toString());
			avalanche.setFilteredListBids(json.get("filteredListBids").toString());
			avalanche.setListAsks(json.get("listAsks").toString());
			avalanche.setListBids(json.get("listBids").toString());
			avalanche.setMarginAsk(new BigDecimal(json.getDouble("marginAsk")));
			avalanche.setMarginBid(new BigDecimal(json.getDouble("marginBid")));
			avalanche.setMaxOpenAsks(new BigDecimal(json.getDouble("maxOpenAsks")));
			avalanche.setMaxOpenBids(new BigDecimal(json.getDouble("maxOpenBids")));
			avalanche.setPathFrom(json.getString("pathFrom"));
			avalanche.setPathTo(json.getString("pathTo"));
			avalanche.setRangeAsks(json.get("rangeAsks").toString());
			avalanche.setRangeBids(json.get("rangeBids").toString());
			avalanche.setRangeCountBids(json.get("rangeCountBids").toString());
			avalanche.setRangeCountAsks(json.get("rangeCountAsks").toString());
			avalanche.setRefAsset(json.getString("refAsset"));
			avalanche.setRefCost(new BigDecimal(json.getDouble("refCost")));
			avalanche.setSlippage(new BigDecimal(json.getDouble("slippage")));

			avalancheRepository.save(avalanche);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	@JmsListener(destination = "offerbook")
	public void onOfferBook(String message) {
//		try {
//			JSONObject o = new JSONObject(message);
//			OfferBook ob = new OfferBook();
//			ob.setCreated_at(Calendar.getInstance().getTime());
//			JSONObject payIssue = o.getJSONObject("payIssue");
//			JSONObject getIssue = o.getJSONObject("getIssue");
//			ob.setPayIssueCurrency(payIssue.optString("currency"));
//			ob.setPayIssueIssuer(payIssue.optString("issuer"));
//			ob.setGetIssueCurrency(getIssue.optString("currency"));
//			ob.setGetIssueIssuer(getIssue.optString("issuer"));
//			Set<Offer> offersAsks = new HashSet<>();
//			Set<Offer> offersBids = new HashSet<>();
//			JSONArray listAsks = o.getJSONArray("offersAsks");
//			JSONArray listBids = o.getJSONArray("offersBids");
//			addOffers(offersAsks, listAsks);
//			addOffers(offersBids, listBids);
//
//			ob.setOffersAsks(offersAsks);
//			ob.setOffersBids(offersBids);
//
//			offerBookRepository.save(ob);
//		} catch (Exception ex) {
//			log.error(ex.getMessage(), ex);
//		}
	}

	private void addOffers(Set<Offer> offers, JSONArray list) {
		Date now = Calendar.getInstance().getTime();
		for (int i = 0; i < list.length(); i++) {
			JSONObject ask = list.getJSONObject(i);
			Offer offer = new Offer();
			offer.setCreated_at(now);
			offer.setBookDirectory(ask.optString("bookDirectory"));
			offer.setAccount(ask.optString("account"));
			offer.setBookNode(ask.optString("bookNode"));
			offer.setFlags(ask.optString("flags"));
			offer.setIndex(ask.optString("index"));
			offer.setLedgerEntryType(ask.optString("ledgerEntryType"));
			// offer.setOwner_funds(new
			// BigDecimal(ask.getDouble("owner_funds")));
			offer.setOwnerNode(ask.optString("OwnerNode"));
			offer.setPreviousTxnID(ask.optString("PreviousTxnID"));
			offer.setPreviousTxnLgrSeq(ask.optString("PreviousTxnLgrSeq"));
			offer.setQuality(new BigDecimal(ask.getDouble("quality")));
			if (ask.optJSONObject("TakerPays") != null) {
				JSONObject takerPays = ask.optJSONObject("TakerPays");
				offer.setTakerPaysValue(new BigDecimal(takerPays.getDouble("value")));
				offer.setTakerPaysIssuer(takerPays.optString("issuer"));
				offer.setTakerPaysCurrency(takerPays.optString("currency"));
			} else {
				offer.setTakerPaysValue(new BigDecimal(ask.getDouble("TakerPays")));
			}
			if (ask.optJSONObject("TakerGets") != null) {
				JSONObject takerGets = ask.optJSONObject("TakerGets");
				offer.setTakerGetsValue(new BigDecimal(takerGets.getDouble("value")));
				offer.setTakerGetsIssuer(takerGets.optString("issuer"));
				offer.setTakerGetsCurrency(takerGets.optString("currency"));
			} else {
				offer.setTakerPaysValue(new BigDecimal(ask.getDouble("TakerGets")));
			}

			offerRepository.save(offer);
			offers.add(offer);
		}
	}

	@JmsListener(destination = "account_balance")
	public void onAccountBalance(String message) {
		try {
			JSONObject jsonObject = new JSONObject(message);
			JSONObject result = jsonObject.getJSONObject("result");
			JSONArray lines = result.getJSONArray("lines");
			Date now = Calendar.getInstance().getTime();
			for (int i = 0; i < lines.length(); i++) {
				JSONObject line = lines.getJSONObject(i);
				AccountBalance ab = new AccountBalance();
				ab.setAccount(line.optString("account"));
				ab.setBalance(new BigDecimal(line.getDouble("balance")));
				ab.setCreatedAt(now);
				ab.setCurrency(line.optString(("currency")));
				ab.setLimit_balance(new BigDecimal(line.getDouble("limit")));
				ab.setLimit_peer(new BigDecimal(line.getDouble("limit_peer")));
				ab.setQuality_in(new BigDecimal(line.getDouble("quality_in")));
				ab.setQuality_out(new BigDecimal(line.getDouble("quality_out")));
				accountBalanceRepository.save(ab);

			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	@JmsListener(destination = "account_offers")
	public void onAccountOffers(String message) {

		try {
			accountOfferRepository.updateStatus("inactive");
			JSONObject jsonObject = new JSONObject(message);
			JSONObject result = jsonObject.getJSONObject("result");
			JSONArray offers = result.getJSONArray("offers");
			Date now = Calendar.getInstance().getTime();
			for (int i = 0; i < offers.length(); i++) {
				JSONObject o = offers.getJSONObject(i);
				AccountOffer a = new AccountOffer();
				a.setStatus("active");
				a.setCreatedAt(now);
				a.setFlags(o.optString("flags"));
				a.setQuality(o.optString("quality"));
				a.setSeq(o.optString("seq"));
				a.setLedgerCurrentIndex(result.getLong("ledger_current_index"));

				if (o.optJSONObject("taker_gets") != null) {
					JSONObject takerGets = o.optJSONObject("taker_gets");
					a.setTakerGetsCurrency(takerGets.optString("currency"));
					a.setTakerGetsIssuer(takerGets.optString("issuer"));
					a.setTakerGetsValue(new BigDecimal(takerGets.getDouble("value")));
				} else {
					a.setTakerGetsValue(new BigDecimal(o.getDouble("taker_gets")));
				}

				if (o.optJSONObject("taker_pays") != null) {
					JSONObject takerPays = o.optJSONObject("taker_pays");
					a.setTakerPaysCurrency(takerPays.optString("currency"));
					a.setTakerPaysIssuer(takerPays.optString("issuer"));
					a.setTakerPaysValue(new BigDecimal(takerPays.getDouble("value")));
				} else {
					a.setTakerPaysValue(new BigDecimal(o.getDouble("taker_pays")));
				}

				accountOfferRepository.save(a);
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}
