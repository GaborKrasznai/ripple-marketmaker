package marketmaker.entities;

import java.util.Date;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;

@Entity
public class OfferBookEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@SequenceGenerator(name="OfferBook_seq", sequenceName="OfferBook_seq")
	public Long id;
	public Date created_at;
	public String payIssueCurrency;
	public String payIssueIssuer;
	public String getIssueCurrency;
	public String getIssueIssuer;

	@OneToMany
	public Set<Offer> offersAsks;
	@OneToMany
	public Set<Offer> offersBids;

	public String getPayIssueCurrency() {
		return payIssueCurrency;
	}

	public void setPayIssueCurrency(String payIssueCurrency) {
		this.payIssueCurrency = payIssueCurrency;
	}

	public String getPayIssueIssuer() {
		return payIssueIssuer;
	}

	public void setPayIssueIssuer(String payIssueIssuer) {
		this.payIssueIssuer = payIssueIssuer;
	}

	public String getGetIssueCurrency() {
		return getIssueCurrency;
	}

	public void setGetIssueCurrency(String getIssueCurrency) {
		this.getIssueCurrency = getIssueCurrency;
	}

	public String getGetIssueIssuer() {
		return getIssueIssuer;
	}

	public void setGetIssueIssuer(String getIssueIssuer) {
		this.getIssueIssuer = getIssueIssuer;
	}

	public Set<Offer> getOffersAsks() {
		return offersAsks;
	}

	public void setOffersAsks(Set<Offer> offersAsks) {
		this.offersAsks = offersAsks;
	}

	public Set<Offer> getOffersBids() {
		return offersBids;
	}

	public void setOffersBids(Set<Offer> offersBids) {
		this.offersBids = offersBids;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getCreated_at() {
		return created_at;
	}

	public void setCreated_at(Date created_at) {
		this.created_at = created_at;
	}

}
