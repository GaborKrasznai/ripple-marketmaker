package marketmaker.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;

@Entity
public class Avalanche implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@SequenceGenerator(name="Avalanche_seq", sequenceName="Avalanche_seq")
	private Long id;
	private Date createdAt;
	private boolean enableOpportunityTaker = false;
	private boolean cancelAllAccountOffersOnStart = false;
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
	private int maxOrdersOpenAsks;
	private int maxOrdersOpenBids;
	private BigDecimal refCostMargin;
	@Lob
	private String listAsks;
	@Lob
	private String listBids;
	@Lob
	private String rangeAsks;
	@Lob
	private String rangeBids;
	@Lob
	private String rangeCountAsks;
	@Lob
	private String rangeCountBids;
	@Lob
	private String filteredListBids;
	@Lob
	private String filteredListAsks;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
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

	public String getListAsks() {
		return listAsks;
	}

	public void setListAsks(String listAsks) {
		this.listAsks = listAsks;
	}

	public String getListBids() {
		return listBids;
	}

	public void setListBids(String listBids) {
		this.listBids = listBids;
	}

	public String getRangeAsks() {
		return rangeAsks;
	}

	public void setRangeAsks(String rangeAsks) {
		this.rangeAsks = rangeAsks;
	}

	public String getRangeBids() {
		return rangeBids;
	}

	public void setRangeBids(String rangeBids) {
		this.rangeBids = rangeBids;
	}

	public String getRangeCountAsks() {
		return rangeCountAsks;
	}

	public void setRangeCountAsks(String rangeCountAsks) {
		this.rangeCountAsks = rangeCountAsks;
	}

	public String getRangeCountBids() {
		return rangeCountBids;
	}

	public void setRangeCountBids(String rangeCountBids) {
		this.rangeCountBids = rangeCountBids;
	}

	public String getFilteredListBids() {
		return filteredListBids;
	}

	public void setFilteredListBids(String filteredListBids) {
		this.filteredListBids = filteredListBids;
	}

	public String getFilteredListAsks() {
		return filteredListAsks;
	}

	public void setFilteredListAsks(String filteredListAsks) {
		this.filteredListAsks = filteredListAsks;
	}

	public BigDecimal getRefCostMargin() {
		return refCostMargin;
	}

	public void setRefCostMargin(BigDecimal refCostMargin) {
		this.refCostMargin = refCostMargin;
	}
	
	

}
