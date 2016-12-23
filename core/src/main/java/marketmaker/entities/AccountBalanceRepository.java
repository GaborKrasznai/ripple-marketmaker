package marketmaker.entities;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface AccountBalanceRepository extends CrudRepository<AccountBalance, Long> {

	List<AccountBalance> findByCreatedAtBetween(Date date, Date date2);

}
