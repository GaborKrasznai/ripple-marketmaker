package marketmaker.entities;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rmartins on 2/25/15.
 */
public class OfferBookPair implements Serializable {
    private OfferBook offerBookFirst;
    private OfferBook offerBookSecond;

    public OfferBookPair(JSONObject jsonObject) throws JSONException {
        this.offerBookFirst = new OfferBook(new JSONObject(jsonObject.getString("offerBookFirst")));
        this.offerBookSecond = new OfferBook(new JSONObject(jsonObject.getString("offerBookSecond")));
    }

    public OfferBookPair(OfferBook offerBookFirst, OfferBook offerBookSecond) {
        this.offerBookFirst = offerBookFirst;
        this.offerBookSecond = offerBookSecond;
    }

    public OfferBookPair() {

    }

    public OfferBook getOfferBookFirst() {
        return offerBookFirst;
    }

    public void setOfferBookFirst(OfferBook offerBookFirst) {
        this.offerBookFirst = offerBookFirst;
    }

    public OfferBook getOfferBookSecond() {
        return offerBookSecond;
    }

    public void setOfferBookSecond(OfferBook offerBookSecond) {
        this.offerBookSecond = offerBookSecond;
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("offerBookFirst", offerBookFirst.toString());
            jsonObject.put("offerBookSecond", offerBookSecond.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
}
