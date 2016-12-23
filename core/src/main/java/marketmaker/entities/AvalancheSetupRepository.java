package marketmaker.entities;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface AvalancheSetupRepository extends CrudRepository<AvalancheSetup, Long> {

	public AvalancheSetup findOneByStatus(@Param("status") String status);

	public AvalancheSetup findOneByInstanceId(String instanceId);

}
