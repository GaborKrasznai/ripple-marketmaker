package marketmaker.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import marketmaker.entities.AvalancheSetup;
import marketmaker.entities.AvalancheSetupRepository;

@Service
public class AvalancheService {

	@Autowired
	private AvalancheSetupRepository avalancheSetupRepository;

	public AvalancheSetup findByInstanceId(String instanceId) {
		AvalancheSetup a = avalancheSetupRepository.findOneByInstanceId(instanceId);
		return a;     
	}

	public AvalancheSetup findById(Long id) {
		AvalancheSetup a = avalancheSetupRepository.findOne(id);
		return a;
	}

	public AvalancheSetup save(AvalancheSetup avalancheSetup) {
		AvalancheSetup save = avalancheSetupRepository.save(avalancheSetup);
		return save;
	}

	public void delete(Long id) {
		avalancheSetupRepository.delete(id);
	}

	public void delete(String instanceId) {
		AvalancheSetup findOneByInstanceId = avalancheSetupRepository.findOneByInstanceId(instanceId);
		avalancheSetupRepository.delete(findOneByInstanceId.getId());
	}
}
