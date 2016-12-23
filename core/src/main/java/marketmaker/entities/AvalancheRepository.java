package marketmaker.entities;

import java.util.Date;
import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly=true)
public interface AvalancheRepository extends CrudRepository<Avalanche, Long> {
	
	List<Avalanche> findByCreatedAtBetween(Date time, Date time2);
	
	

}
