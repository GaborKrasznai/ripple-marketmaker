package marketmaker.entities;

import javax.jms.Destination;

/**
 * Created by rmartins on 2/22/15.
 */
public class Channels {
	public static String AVALANCHE = "avalanche";
	public static String INSTRUMENTS = "instruments";
	public static String ARBITRAGER = "arbitrager";
	public static String PAYMENT_CREATE = "payment_create";
	public static String PATH_FIND_PAIR = "pathfind_pair";
	public static String FOXBIT_BALANCE = "foxbit_balance";
	public static String FOXBIT_ORDERBOOK = "foxbit_orderbook";
	public static String B2U_BALANCE = "b2u_balance";
	public static String ACCOUNT_OFFERS = "account_offers";
	public static String OFFER_CANCEL = "offer_cancel";
	public static String OFFER_CREATE = "offer_create";
	public static String ACCOUNT_BALANCE = "account_balance";
	public static String OFFERBOOK = "offerbook";
	public static String PATH_FIND = "pathfind";

	public static String BITSTAMP_ORDERBOOK = "bitstamp_orderbook";
	public static String BITSTAMP_BALANCE = "bitstamp_balance";
	public static String BITSTAMP_OFFERCREATE = "bitstamp_offercreate";
	public static String BITSTAMP_OFFERCANCEL = "bitstamp_offercancel";
	public static String BITSTAMP_OFFERSTATUS = "bitstamp_orderstatus";
	public static String BITSTAMP_OPENORDERS = "bitstamp_openorders";
	
	public static String BITFINEX_BOOK = "bitfinex_book";
	public static String BITFINEX_OFFERS = "bitfinex_offers";
	public static String BITFINEX_OFFERSTATUS = "bitfinex_offerstatus";
	public static String BITFINEX_OFFERCREATE = "bitfinex_offercreate";
	public static String BITFINEX_OFFERCANCEL = "bitfinex_offercancel";
	
}
