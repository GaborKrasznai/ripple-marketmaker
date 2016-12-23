package marketmaker.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
public class AccountOffer implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@SequenceGenerator(name="AccountOffer_seq", sequenceName="AccountOffer_seq")
	private Long id;
	private Date createdAt;
	private String flags;
	private String takerGetsCurrency;

	private BigDecimal takerGetsValue;
	private String takerGetsIssuer;
	private String takerPaysCurrency;

	private BigDecimal takerPaysValue;
	private String takerPaysIssuer;
	private String seq;
	private String quality;
	private Long ledgerCurrentIndex;
	private String status;

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

	public String getFlags() {
		return flags;
	}

	public void setFlags(String flags) {
		this.flags = flags;
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

	public String getSeq() {
		return seq;
	}

	public void setSeq(String seq) {
		this.seq = seq;
	}

	public String getQuality() {
		return quality;
	}

	public void setQuality(String quality) {
		this.quality = quality;
	}

	public Long getLedgerCurrentIndex() {
		return ledgerCurrentIndex;
	}

	public void setLedgerCurrentIndex(Long ledgerCurrentIndex) {
		this.ledgerCurrentIndex = ledgerCurrentIndex;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
