import com.ripple.client.Client;
import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.Issue;
import com.ripple.core.coretypes.STArray;
import com.ripple.core.types.known.sle.entries.Offer;
import org.json.JSONArray;

import java.math.BigDecimal;

/**
 * Created by bob on 22/11/14.
 */
public class OrderBookPair {


    private final Client client;
    private final Issue first;
    private final Issue second;
    private final BookEvents callback;
    public STArray asks, bids;
    public Amount ask, bid, spread;

    public OrderBookPair(Client client, Issue first, Issue second, BookEvents callback) {
        this.client = client;
        this.first = first;
        this.second = second;
        this.callback = callback;
    }

    private void calculateStats() {
        Offer firstAsk = (Offer) asks.get(0);
        Offer firstBid = (Offer) bids.get(0);

        BigDecimal askQuality = firstAsk.askQuality();
        BigDecimal bidQuality = firstBid.bidQuality();

        Amount secondOne = firstAsk.paysOne();

        ask = secondOne.multiply(askQuality);
        bid = secondOne.multiply(bidQuality);

        spread = ask.subtract(bid).abs();
    }

    public void requestUpdate() {
        for (int i = 0; i < 2; i++) {
            final boolean getAsks = i == 0,
                    getBids = !getAsks;

            Issue getIssue = getAsks ? first : second,
                    payIssue = getAsks ? second : first;

            Request request = client.requestBookOffers(getIssue, payIssue);
            request.once(Request.OnResponse.class, new Request.OnResponse() {
                @Override
                public void called(Response response) {
                    if (response.succeeded) {
                        JSONArray offersJSON = response.result.optJSONArray("offers");
                        System.out.println(offersJSON);
                        STArray offers = STArray.translate.fromJSONArray(offersJSON);
                        if (getBids) bids = offers;
                        else asks = offers;

                        if (retrievedBothBooks()) {
                            if (!isEmpty()) {
                                calculateStats();
                            }
                            callback.onUpdate(OrderBookPair.this);
                        }
                    } else {
                        System.out.println("There was an error: " + response.message);
                    }
                }
            });
            request.request();
        }

    }

    public String currencyPair() {
        return String.format("%s/%s", first.currency(), second.currency());
    }

    public boolean retrievedBothBooks() {
        return asks != null && bids != null;
    }

    public boolean isEmpty() {
        return !retrievedBothBooks() || asks.isEmpty() || bids.isEmpty();
    }
}