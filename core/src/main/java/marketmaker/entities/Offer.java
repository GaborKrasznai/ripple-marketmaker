package marketmaker.entities;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;

@Entity
public class Offer {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@SequenceGenerator(name = "Offer_seq", sequenceName = "Offer_seq")
	private Long id;
	private Date created_at;
	private BigDecimal owner_funds;
	private String account;
	private String previousTxnLgrSeq;
	private String ownerNode;
	private String index;
	private String previousTxnID;
	private BigDecimal quality;
	private String flags;
	private Long sequence;
	private String takerGetsCurrency;

	private BigDecimal takerGetsValue;
	private String takerGetsIssuer;
	private String takerPaysCurrency;

	private BigDecimal takerPaysValue;
	private String takerPaysIssuer;
	private String bookDirectory;
	private String ledgerEntryType;
	private String bookNode;
	private Long ledger_current_index;

	// @OneToOne
	// private OfferBook offerBook;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BigDecimal getOwner_funds() {
		return owner_funds;
	}

	public void setOwner_funds(BigDecimal owner_funds) {
		this.owner_funds = owner_funds;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getPreviousTxnLgrSeq() {
		return previousTxnLgrSeq;
	}

	public void setPreviousTxnLgrSeq(String previousTxnLgrSeq) {
		this.previousTxnLgrSeq = previousTxnLgrSeq;
	}

	public String getOwnerNode() {
		return ownerNode;
	}

	public void setOwnerNode(String ownerNode) {
		this.ownerNode = ownerNode;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getPreviousTxnID() {
		return previousTxnID;
	}

	public void setPreviousTxnID(String previousTxnID) {
		this.previousTxnID = previousTxnID;
	}

	public BigDecimal getQuality() {
		return quality;
	}

	public void setQuality(BigDecimal quality) {
		this.quality = quality;
	}

	public String getFlags() {
		return flags;
	}

	public void setFlags(String flags) {
		this.flags = flags;
	}

	public Long getSequence() {
		return sequence;
	}

	public void setSequence(Long sequence) {
		this.sequence = sequence;
	}

	public String getTakerGetsCurrency() {
		return takerGetsCurrency;
	}

	public void setTakerGetsCurrency(String takerGetsCurrency) {
		this.takerGetsCurrency = takerGetsCurrency;
	}

	public BigDecimal getTakerGetsValue() {
		return takerGetsValue;
	}

	public void setTakerGetsValue(BigDecimal takerGetsValue) {
		this.takerGetsValue = takerGetsValue;
	}

	public String getTakerGetsIssuer() {
		return takerGetsIssuer;
	}

	public void setTakerGetsIssuer(String takerGetsIssuer) {
		this.takerGetsIssuer = takerGetsIssuer;
	}

	public String getTakerPaysCurrency() {
		return takerPaysCurrency;
	}

	public void setTakerPaysCurrency(String takerPaysCurrency) {
		this.takerPaysCurrency = takerPaysCurrency;
	}

	public BigDecimal getTakerPaysValue() {
		return takerPaysValue;
	}

	public void setTakerPaysValue(BigDecimal takerPaysValue) {
		this.takerPaysValue = takerPaysValue;
	}

	public String getTakerPaysIssuer() {
		return takerPaysIssuer;
	}

	public void setTakerPaysIssuer(String takerPaysIssuer) {
		this.takerPaysIssuer = takerPaysIssuer;
	}

	public String getBookDirectory() {
		return bookDirectory;
	}

	public void setBookDirectory(String bookDirectory) {
		this.bookDirectory = bookDirectory;
	}

	public String getLedgerEntryType() {
		return ledgerEntryType;
	}

	public void setLedgerEntryType(String ledgerEntryType) {
		this.ledgerEntryType = ledgerEntryType;
	}

	public String getBookNode() {
		return bookNode;
	}

	public void setBookNode(String bookNode) {
		this.bookNode = bookNode;
	}

	public Date getCreated_at() {
		return created_at;
	}

	public void setCreated_at(Date created_at) {
		this.created_at = created_at;
	}

	public Long getLedger_current_index() {
		return ledger_current_index;
	}

	public void setLedger_current_index(Long ledger_current_index) {
		this.ledger_current_index = ledger_current_index;
	}

}
