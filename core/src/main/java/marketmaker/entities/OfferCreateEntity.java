package marketmaker.entities;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
public class OfferCreateEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@SequenceGenerator(name="offerCreateEntity_seq", sequenceName="offerCreateEntity_seq")
	private Long id;
	private Date createdAt;
	private String expiration;
	private String offerSequence;
	private BigDecimal takerPaysValue;
	private String takerPaysCurrency;
	private String takerPaysIssuer;
	private BigDecimal takerGetsValue;
	private String takerGetsCurrency;
	private String takerGetsIssuer;

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

	public String getExpiration() {
		return expiration;
	}

	public void setExpiration(String expiration) {
		this.expiration = expiration;
	}

	public String getOfferSequence() {
		return offerSequence;
	}

	public void setOfferSequence(String offerSequence) {
		this.offerSequence = offerSequence;
	}

	public BigDecimal getTakerPaysValue() {
		return takerPaysValue;
	}

	public void setTakerPaysValue(BigDecimal takerPaysValue) {
		this.takerPaysValue = takerPaysValue;
	}

	public String getTakerPaysCurrency() {
		return takerPaysCurrency;
	}

	public void setTakerPaysCurrency(String takerPaysCurrency) {
		this.takerPaysCurrency = takerPaysCurrency;
	}

	public String getTakerPaysIssuer() {
		return takerPaysIssuer;
	}

	public void setTakerPaysIssuer(String takerPaysIssuer) {
		this.takerPaysIssuer = takerPaysIssuer;
	}

	public BigDecimal getTakerGetsValue() {
		return takerGetsValue;
	}

	public void setTakerGetsValue(BigDecimal takerGetsValue) {
		this.takerGetsValue = takerGetsValue;
	}

	public String getTakerGetsCurrency() {
		return takerGetsCurrency;
	}

	public void setTakerGetsCurrency(String takerGetsCurrency) {
		this.takerGetsCurrency = takerGetsCurrency;
	}

	public String getTakerGetsIssuer() {
		return takerGetsIssuer;
	}

	public void setTakerGetsIssuer(String takerGetsIssuer) {
		this.takerGetsIssuer = takerGetsIssuer;
	}

}
