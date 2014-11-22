import com.ripple.client.Client;
import com.ripple.client.requests.Request;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;
import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.Issue;
import com.ripple.core.coretypes.STObject;
import com.ripple.core.types.known.sle.entries.Offer;
import org.json.JSONException;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * This code was inspired on CheckPrice class from ripple-cli lib.
 * Created by bob on 22/11/14.
 */
public class MainRippleSpreadTaker {

    // TODO change accountID
    public static final AccountID RIPPEX = AccountID.fromAddress("rfNZPxoZ5Uaamdp339U9dCLWz2T73nZJZH");
    public static final AccountID BITSTAMP = AccountID.fromAddress("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B");
    public static final Issue XRP = Issue.XRP;
    public static final Issue BITSTAMP_USD = BITSTAMP.issue("USD");
    public static final Issue BITSTAMP_BTC = BITSTAMP.issue("BTC");
    public static final Issue RIPPEX_BRL = RIPPEX.issue("BRL");


    public static void main(String[] args) throws JSONException {
        Client client = new Client(new JavaWebSocketTransportImpl());
        client.connect("wss://s1.ripple.com");

        BookEvents bookEvents = new BookEvents() {
            @Override
            public void onUpdate(OrderBookPair book) {
                if (!book.isEmpty()) {
                    System.out.printf("%s Ask: %s, Bid: %s, Spread %s%n",
                            book.currencyPair(),
                            book.ask.toText(),
                            book.bid.toText(),
                            book.spread.toText());

                    System.out.println(Arrays.toString(book.asks.toArray()));
                    System.out.println(Arrays.toString(book.bids.toArray()));

                } else {
                    System.out.printf("%s No info!%n", book.currencyPair());
                }
            }

            private void showOfferInfo(Offer offer) {
                BigDecimal payForOne = offer.askQuality();
                Amount getsOne = offer.getsOne();
                Amount paysOne = offer.paysOne();
                System.out.println("Directory quality: " + offer.directoryAskQuality().toPlainString());
                // Multiply and divide will round/scale to the required bounds
                System.out.printf("%40s == %s\n", paysOne.multiply(payForOne).toText(), getsOne.toText());
                System.out.printf("%40s == %s\n", getsOne.divide(payForOne).toText(), paysOne.toText());
            }
        };

        new OrderBookPair(client, XRP, BITSTAMP_USD, bookEvents).requestUpdate();
        new OrderBookPair(client, XRP, RIPPEX_BRL, bookEvents).requestUpdate();
    }
}

