package marketmaker.entities;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import org.springframework.beans.factory.annotation.Value;

@Entity
public class AvalancheSetup {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@SequenceGenerator(name = "AvalancheSetup_seq", sequenceName = "AvalancheSetup_seq")
	private Long id;

	private String instanceId;
	private Date createdAt;
	private String status;
	private String rippleAccount;
	private boolean enableOpportunityTaker = false;
	private boolean cancelAllAccountOffersOnStart = false;
	private String scriptsDirectory = "scripts/";
	private String pathFrom;
	private String pathTo;
	private String baseAsset;
	private String counterAsset;
	private String refAsset;
	private BigDecimal refCost;
	private BigDecimal baseAmount;
	private BigDecimal marginAsk;
	private BigDecimal marginBid;
	private BigDecimal degreeAsk;
	private BigDecimal degreeBid;
	private BigDecimal maxOpenAsks;
	private BigDecimal maxOpenBids;
	private BigDecimal baseExpo;
	private BigDecimal counterExpo;
	private BigDecimal slippage;
	private BigDecimal refCostMargin;
	private String liveFeedEndpoint;
	private String liveFeedCurrencyPair;
	private boolean liveFeedEnabled;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean isEnableOpportunityTaker() {
		return enableOpportunityTaker;
	}

	public void setEnableOpportunityTaker(boolean enableOpportunityTaker) {
		this.enableOpportunityTaker = enableOpportunityTaker;
	}

	public boolean isCancelAllAccountOffersOnStart() {
		return cancelAllAccountOffersOnStart;
	}

	public void setCancelAllAccountOffersOnStart(boolean cancelAllAccountOffersOnStart) {
		this.cancelAllAccountOffersOnStart = cancelAllAccountOffersOnStart;
	}

	public String getScriptsDirectory() {
		return scriptsDirectory;
	}

	public void setScriptsDirectory(String scriptsDirectory) {
		this.scriptsDirectory = scriptsDirectory;
	}

	public String getPathFrom() {
		return pathFrom;
	}

	public void setPathFrom(String pathFrom) {
		this.pathFrom = pathFrom;
	}

	public String getPathTo() {
		return pathTo;
	}

	public void setPathTo(String pathTo) {
		this.pathTo = pathTo;
	}

	public String getBaseAsset() {
		return baseAsset;
	}

	public void setBaseAsset(String baseAsset) {
		this.baseAsset = baseAsset;
	}

	public String getCounterAsset() {
		return counterAsset;
	}

	public void setCounterAsset(String counterAsset) {
		this.counterAsset = counterAsset;
	}

	public String getRefAsset() {
		return refAsset;
	}

	public void setRefAsset(String refAsset) {
		this.refAsset = refAsset;
	}

	public BigDecimal getRefCost() {
		return refCost;
	}

	public void setRefCost(BigDecimal refCost) {
		this.refCost = refCost;
	}

	public BigDecimal getBaseAmount() {
		return baseAmount;
	}

	public void setBaseAmount(BigDecimal baseAmount) {
		this.baseAmount = baseAmount;
	}

	public BigDecimal getMarginAsk() {
		return marginAsk;
	}

	public void setMarginAsk(BigDecimal marginAsk) {
		this.marginAsk = marginAsk;
	}

	public BigDecimal getMarginBid() {
		return marginBid;
	}

	public void setMarginBid(BigDecimal marginBid) {
		this.marginBid = marginBid;
	}

	public BigDecimal getDegreeAsk() {
		return degreeAsk;
	}

	public void setDegreeAsk(BigDecimal degreeAsk) {
		this.degreeAsk = degreeAsk;
	}

	public BigDecimal getDegreeBid() {
		return degreeBid;
	}

	public void setDegreeBid(BigDecimal degreeBid) {
		this.degreeBid = degreeBid;
	}

	public BigDecimal getMaxOpenAsks() {
		return maxOpenAsks;
	}

	public void setMaxOpenAsks(BigDecimal maxOpenAsks) {
		this.maxOpenAsks = maxOpenAsks;
	}

	public BigDecimal getMaxOpenBids() {
		return maxOpenBids;
	}

	public void setMaxOpenBids(BigDecimal maxOpenBids) {
		this.maxOpenBids = maxOpenBids;
	}

	public BigDecimal getBaseExpo() {
		return baseExpo;
	}

	public void setBaseExpo(BigDecimal baseExpo) {
		this.baseExpo = baseExpo;
	}

	public BigDecimal getCounterExpo() {
		return counterExpo;
	}

	public void setCounterExpo(BigDecimal counterExpo) {
		this.counterExpo = counterExpo;
	}

	public BigDecimal getSlippage() {
		return slippage;
	}

	public void setSlippage(BigDecimal slippage) {
		this.slippage = slippage;
	}

	public BigDecimal getRefCostMargin() {
		return refCostMargin;
	}

	public void setRefCostMargin(BigDecimal refCostMargin) {
		this.refCostMargin = refCostMargin;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getLiveFeedEndpoint() {
		return liveFeedEndpoint;
	}

	public void setLiveFeedEndpoint(String liveFeedEndpoint) {
		this.liveFeedEndpoint = liveFeedEndpoint;
	}

	public String getLiveFeedCurrencyPair() {
		return liveFeedCurrencyPair;
	}

	public void setLiveFeedCurrencyPair(String liveFeedCurrencyPair) {
		this.liveFeedCurrencyPair = liveFeedCurrencyPair;
	}

	public boolean isLiveFeedEnabled() {
		return liveFeedEnabled;
	}

	public void setLiveFeedEnabled(boolean liveFeedEnabled) {
		this.liveFeedEnabled = liveFeedEnabled;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getRippleAccount() {
		return rippleAccount;
	}

	public void setRippleAccount(String rippleAccount) {
		this.rippleAccount = rippleAccount;
	}

}
