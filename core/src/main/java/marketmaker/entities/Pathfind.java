package marketmaker.entities;

import java.io.Serializable;

import org.javatuples.Pair;
import org.json.JSONObject;

import com.ripple.core.coretypes.Issue;

/**
 * Created by rmartins on 4/30/15.
 */
public class Pathfind implements Serializable {

	private Double amount;
	private String pathFrom;
	private String pathTo;
	private Issue baseAsset;
	private Pair<Issue, Issue> issues;
	private JSONObject alternative;
	private JSONObject result;

	public Pathfind() {

	}

	public Pathfind(JSONObject json) {
		this.setPathFrom(json.getString("pathFrom"));
		this.setPathTo(json.getString("pathTo"));
		this.setBaseAsset(Issue.fromString(json.getString("baseAsset")));
		this.setAmount(json.getDouble("amount"));
		this.setResult(json);
	}

	public Pathfind(String pathFrom, String pathTo, Double amount, Issue baseAsset) {
		super();
		this.amount = amount;
		this.pathFrom = pathFrom;
		this.pathTo = pathTo;
		this.baseAsset = baseAsset;
	}

	public Pathfind(String pathFrom, String pathTo, Double amount, Issue baseAsset, Pair<Issue, Issue> pair) {
		this.amount = amount;
		this.pathFrom = pathFrom;
		this.pathTo = pathTo;
		this.baseAsset = baseAsset;
		this.issues = pair;
	}

	public JSONObject toJSONObject() {
		JSONObject json = new JSONObject();
		json.put("pathFrom", this.getPathFrom());
		json.put("pathTo", this.getPathTo());
		json.put("baseAsset", this.getBaseAsset());
		json.put("amount", this.getAmount());
		json.put("issues", this.getIssues().toArray());
		json.put("alternative", this.getAlternative());
		json.put("result", this.getResult());
		return json;
	}

	/**
	 * @return the amount
	 */
	public Double getAmount() {
		return amount;
	}

	/**
	 * @param amount
	 *            the amount to set
	 */
	public void setAmount(Double amount) {
		this.amount = amount;
	}

	/**
	 * @return the pathFrom
	 */
	public String getPathFrom() {
		return pathFrom;
	}

	/**
	 * @param pathFrom
	 *            the pathFrom to set
	 */
	public void setPathFrom(String pathFrom) {
		this.pathFrom = pathFrom;
	}

	/**
	 * @return the pathTo
	 */
	public String getPathTo() {
		return pathTo;
	}

	/**
	 * @param pathTo
	 *            the pathTo to set
	 */
	public void setPathTo(String pathTo) {
		this.pathTo = pathTo;
	}

	/**
	 * @return the baseAsset
	 */
	public Issue getBaseAsset() {
		return baseAsset;
	}

	/**
	 * @param baseAsset
	 *            the baseAsset to set
	 */
	public void setBaseAsset(Issue baseAsset) {
		this.baseAsset = baseAsset;
	}

	/**
	 * @return the issues
	 */
	public Pair<Issue, Issue> getIssues() {
		return issues;
	}

	/**
	 * @param issues
	 *            the issues to set
	 */
	public void setIssues(Pair<Issue, Issue> issues) {
		this.issues = issues;
	}

	/**
	 * @return the result
	 */
	public JSONObject getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(JSONObject result) {
		this.result = result;
	}

	/**
	 * @return the alternative
	 */
	public JSONObject getAlternative() {
		return alternative;
	}

	/**
	 * @param alternative
	 *            the alternative to set
	 */
	public void setAlternative(JSONObject alternative) {
		this.alternative = alternative;
	}

}
