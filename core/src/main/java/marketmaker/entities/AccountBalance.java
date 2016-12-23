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
public class AccountBalance implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@SequenceGenerator(name="AccountBalance_seq", sequenceName="AccountBalance_seq")
	private Long id;
	private boolean no_ripple;
	
	private BigDecimal balance;
	private BigDecimal limit_balance;
	private BigDecimal quality_in;
	private BigDecimal quality_out;
	private String currency;
	private BigDecimal limit_peer;
	private String account;
	private Date createdAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean isNo_ripple() {
		return no_ripple;
	}

	public void setNo_ripple(boolean no_ripple) {
		this.no_ripple = no_ripple;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	

	public BigDecimal getLimit_balance() {
		return limit_balance;
	}

	public void setLimit_balance(BigDecimal limit_balance) {
		this.limit_balance = limit_balance;
	}

	public BigDecimal getQuality_in() {
		return quality_in;
	}

	public void setQuality_in(BigDecimal quality_in) {
		this.quality_in = quality_in;
	}

	public BigDecimal getQuality_out() {
		return quality_out;
	}

	public void setQuality_out(BigDecimal quality_out) {
		this.quality_out = quality_out;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public BigDecimal getLimit_peer() {
		return limit_peer;
	}

	public void setLimit_peer(BigDecimal limit_peer) {
		this.limit_peer = limit_peer;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}


}
