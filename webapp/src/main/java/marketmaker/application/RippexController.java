package marketmaker.application;

import java.util.Calendar;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import marketmaker.entities.AccountBalance;
import marketmaker.entities.AccountBalanceRepository;
import marketmaker.entities.AccountOffer;
import marketmaker.entities.AccountOfferRepository;
import marketmaker.entities.Avalanche;
import marketmaker.entities.AvalancheRepository;
import marketmaker.entities.AvalancheSetup;
import marketmaker.entities.AvalancheSetupRepository;
import marketmaker.entities.Channels;
import marketmaker.entities.InstrumentsList;
import marketmaker.services.AvalancheService;

@RestController
@RequestMapping(value = "/rippex")
public class RippexController {

	@Autowired
	private Environment environment;
	@Autowired
	private JmsTemplate template;
	@Autowired
	private AccountOfferRepository accountOfferRepository;
	@Autowired
	private AccountBalanceRepository accountBalanceRepository;
	@Autowired
	private AvalancheRepository avalancheRepository;
	@Autowired
	private AvalancheSetupRepository avalancheSetupRepository;
	@Autowired
	private AvalancheService avalanceService;

	@RequestMapping(value = "/accountoffers", method = RequestMethod.POST)
	@ResponseBody
	public String accountOffers(@RequestBody String body) throws Exception {
		List<AccountOffer> list = accountOfferRepository.findAllByStatus("active");
		ObjectMapper mapper = new ObjectMapper(new JsonFactory());
		String result = mapper.writeValueAsString(list);

		return result;
	}

	@RequestMapping(value = "/accountbalance", method = RequestMethod.POST)
	@ResponseBody
	public String accountBalance(@RequestBody String body) throws Exception {
		Calendar now = Calendar.getInstance();
		Calendar start = Calendar.getInstance();
		start.add(Calendar.DAY_OF_MONTH, -5);
		List<AccountBalance> list = accountBalanceRepository.findByCreatedAtBetween(start.getTime(), now.getTime());
		ObjectMapper mapper = new ObjectMapper(new JsonFactory());
		String result = mapper.writeValueAsString(list);
		return result;
	}

	@RequestMapping(value = "/avalanche", method = RequestMethod.POST)
	@ResponseBody
	public String avalanche(@RequestBody String body) throws Exception {
		Calendar now = Calendar.getInstance();
		Calendar start = Calendar.getInstance();
		start.add(Calendar.DAY_OF_MONTH, -5);
		List<Avalanche> list = avalancheRepository.findByCreatedAtBetween(start.getTime(), now.getTime());
		ObjectMapper mapper = new ObjectMapper(new JsonFactory());
		String result = mapper.writeValueAsString(list);
		return result;
	}

	@RequestMapping(value = "/setup/{instanceId}", method = RequestMethod.POST)
	@ResponseBody
	public String setup(@PathVariable("instanceId") String instanceId, @RequestBody String body) throws Exception {
		try {
			JSONObject json = new JSONObject(body);
			AvalancheSetup newValues = InstrumentsList.toAvalancheSetup(json);

			AvalancheSetup findByInstanceId = avalanceService.findByInstanceId(instanceId);
			if (findByInstanceId != null) {
				newValues.setId(findByInstanceId.getId());
			} else {
				newValues.setId(null);
			}

			newValues.setInstanceId(instanceId);
			newValues.setCreatedAt(Calendar.getInstance().getTime());
			newValues.setStatus("active");

			AvalancheSetup save = avalancheSetupRepository.save(newValues);
			ObjectMapper mapper = new ObjectMapper(new JsonFactory());
			String message = mapper.writeValueAsString(save);
			// template.convertAndSend(Channels.INSTRUMENTS, message);
			return message;

		} catch (Exception ex) {
			return "";
		}

	}

	@RequestMapping(value = "/setup/{instanceId}", method = RequestMethod.GET)
	@ResponseBody
	public String getSetup(@PathVariable("instanceId") String instanceId) throws Exception {

		AvalancheSetup findByInstanceId = avalanceService.findByInstanceId(instanceId);

		ObjectMapper mapper = new ObjectMapper(new JsonFactory());
		return mapper.writeValueAsString(findByInstanceId);
	}

}
