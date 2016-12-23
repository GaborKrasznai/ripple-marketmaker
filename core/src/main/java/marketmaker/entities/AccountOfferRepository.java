package marketmaker.entities;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = false)
public interface AccountOfferRepository extends CrudRepository<AccountOffer, Long> {

	@Query("select max(a.ledgerCurrentIndex) from AccountOffer a")
	Long maxLedgerCurrentIndex();

	List<AccountOffer> findAllByLedgerCurrentIndex(Long maxLedgerCurrentIndex);

	@Modifying
	@Query("update AccountOffer a set a.status = ?1 where a.status = 'active'")

	void updateStatus(String status);

	List<AccountOffer> findAllByStatus(String status);

}
