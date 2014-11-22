import com.ripple.client.Client;
import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;
import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Issue;
import org.junit.Test;

/**
 * Created by bob on 22/11/14.
 */
public class TestRipple {

    public AccountID RIPPEX = AccountID.fromAddress("rfNZPxoZ5Uaamdp339U9dCLWz2T73nZJZH");
    public AccountID BITSTAMP = AccountID.fromAddress("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B");

    public Issue XRP = Issue.XRP;
    public Issue BITSTAMP_USD = BITSTAMP.issue("USD");
    public Issue BITSTAMP_BTC = BITSTAMP.issue("BTC");
    public Issue RIPPEX_BRL = RIPPEX.issue("BRL");

    @Test
    public void testOffersLoad() {

        Client client = new Client(new JavaWebSocketTransportImpl());
        client.connect("wss://s1.ripple.com");

        Request request = client.requestBookOffers(XRP, BITSTAMP_USD);
        request.once(Request.OnResponse.class, new Request.OnResponse() {
            @Override
            public void called(Response response) {
                System.out.println(response);
            }
        });
        request.request();

    }

}
