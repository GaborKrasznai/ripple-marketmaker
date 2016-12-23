package marketmaker.services.ripple;

import java.io.IOException;

import org.json.JSONException;
import org.ripple.bouncycastle.crypto.InvalidCipherTextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.ripple.client.Account;
import com.ripple.client.Client;
import com.ripple.client.transactions.TransactionManager;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;

import marketmaker.entities.AvalancheSetup;
import marketmaker.services.AvalancheService;

public abstract class BaseRippleClient {

	private static Logger log = LoggerFactory.getLogger(BaseRippleClient.class);
	@Value("${ripplemm.instanceId}")
	protected String instanceId;
	@Value("${ripplemm.rippled}")
	protected String rippled;
	@Autowired
	private AvalancheService avalancheService;

	protected Client client;
	protected TransactionManager transactionManager;

	protected AvalancheSetup avalancheSetup() {
		AvalancheSetup avalancheSetup = avalancheService.findByInstanceId(instanceId);
		if (avalancheSetup == null) {
			throw new RuntimeException("Avalanche Setup not found for instanceId : " + instanceId);
		}
		return avalancheSetup;
	}

	protected Client client() {
		if (client == null) {
			client = new Client(new JavaWebSocketTransportImpl());
			client.connect(rippled, new Client.OnConnected() {
				@Override
				public void called(Client client) {
					log.info("Connected to Ripple Network!");
				}
			});
		}
		return client;
	}

	protected Account account() throws IOException, InvalidCipherTextException, JSONException {
		final Account rippleAccount = client().accountFromSeed(avalancheSetup().getRippleAccount());
		return rippleAccount;
	}

	protected TransactionManager transactionManager() throws InvalidCipherTextException, JSONException, IOException {
		TransactionManager transactionManager = new TransactionManager(client(), account().getAccountRoot(),
				account().id(), account().keyPair);
		return transactionManager;
	}

}
