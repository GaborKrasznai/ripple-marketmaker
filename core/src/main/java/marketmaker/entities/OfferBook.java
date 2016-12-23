package marketmaker.entities;

import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ripple.core.coretypes.Issue;

/**
 * Created by rmartins on 2/25/15.
 */
public class OfferBook implements Serializable {

    private Issue getIssue;
    private Issue payIssue;
    private JSONArray offersAsks;
    private JSONArray offersBids;

    public OfferBook(JSONObject jsonObject) throws JSONException {
        this.getIssue = parseIssueFromBookOffer(jsonObject.getJSONObject("getIssue"));
        this.payIssue = parseIssueFromBookOffer(jsonObject.getJSONObject("payIssue"));
    }

    private Issue parseIssueFromBookOffer(JSONObject jsonObject) throws JSONException {

        String currency = jsonObject.getString("currency");
        if (!Issue.XRP.toString().equals(currency)) {
            return Issue.fromString(currency.concat("/").concat(jsonObject.getString("issuer")));
        }

        return Issue.fromString(currency);

    }

    public OfferBook(Issue getIssue, Issue payIssue) {
        this.getIssue = getIssue;
        this.payIssue = payIssue;
    }

    public Issue getGetIssue() {
        return getIssue;
    }

    public void setGetIssue(Issue getIssue) {
        this.getIssue = getIssue;
    }

    public Issue getPayIssue() {
        return payIssue;
    }

    public void setPayIssue(Issue payIssue) {
        this.payIssue = payIssue;
    }

    public JSONArray getOffersAsks() {
        return offersAsks;
    }

    public void setOffersAsks(JSONArray offersAsks) {
        this.offersAsks = offersAsks;
    }

    public JSONArray getOffersBids() {
        return offersBids;
    }

    public void setOffersBids(JSONArray offersBids) {
        this.offersBids = offersBids;
    }

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("getIssue", getIssue.toJSON());
            jsonObject.put("payIssue", payIssue.toJSON());
            jsonObject.put("offersAsks", offersAsks);
            jsonObject.put("offersBids", offersBids);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public boolean retrievedBothBooks() {
        return offersAsks != null && offersBids != null;
    }

    @Override
    public String toString() {
        JSONObject json = toJSONObject();
        return json.toString();

    }
}
