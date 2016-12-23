package marketmaker.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.ripple.client.Account;
import com.ripple.client.Client;
import com.ripple.client.transactions.TransactionManager;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;
import com.ripple.core.coretypes.Issue;

import marketmaker.entities.Pathfind;
import marketmaker.services.algorithms.RippexArbitrager;
import marketmaker.services.exchanges.bitfinex.BitfinexBookPublisher;
import marketmaker.services.exchanges.bitfinex.BitfinexOfferCreatePublisher;
import marketmaker.services.exchanges.bitstamp.BitstampOfferCreateService;
import marketmaker.services.exchanges.bitstamp.BitstampOrderBookPublisher;
import marketmaker.services.ripple.RippleArbitragerPathfindPublisher;
import marketmaker.services.ripple.RippleMessageLoggerListener;
import marketmaker.services.ripple.RippleMultiPathfindPublisher;
import marketmaker.services.ripple.RipplePaymentCreateListener;

/**
 * Created by rmartins on 29/06/15.
 */
@EnableAutoConfiguration
@EnableScheduling
@EnableJms
@EnableConfigurationProperties
@PropertySource("config/application.properties")
@ComponentScan
public class ArbitragerApplication implements BeanFactoryAware {
	private static Logger log = LoggerFactory.getLogger(ArbitragerApplication.class);

	@Value("${ripplemm.rippled}")
	private String rippled;
	@Value("${ripplemm.account}")
	protected String account;
	@Value("${ripplemm.ripple.account}")
	protected String rippleAccount;

	@Value("${ripplemm.arbitrager.exchanges}")
	private String exchanges;
	@Value("${ripplemm.arbitrager.amounts}")
	private String exchangesAmounts;

	@Bean
	@Scope()
	Client client() {
		JavaWebSocketTransportImpl javaWebSocketTransportImpl = new JavaWebSocketTransportImpl();
		Client client = new Client(javaWebSocketTransportImpl);
		
		client.on(Client.OnDisconnected.class, new Client.OnDisconnected() {
			@Override
			public void called(Client arg0) {
				connectClient(client);
				
			}
		});
		
		connectClient(client);

		return client;
	}

	public void connectClient(Client client) {
		client.connect(rippled, new Client.OnConnected() {
			@Override
			public void called(Client client) {
			}
		});
	}

	@Bean
	public Account rippleAccount() {
		return client().accountFromSeed(account);
	}

	@Bean(name = "rippleTransactionManager")
	@Scope(value = "singleton")
	TransactionManager transactionManager(Client client) {
		if (account == null || account.length() == 0) {
			return null;
		}

		TransactionManager transactionManager = new TransactionManager(client, rippleAccount().getAccountRoot(),
				rippleAccount().id(), rippleAccount().keyPair);
		return transactionManager;
	}

	@Bean
	public BitstampOrderBookPublisher bitstampOrderBookPublisher() {
		return new BitstampOrderBookPublisher();
	}

	@Bean
	public BitstampOfferCreateService bitstampOfferCreateService() {
		return new BitstampOfferCreateService();
	}

	@Bean
	public BitfinexBookPublisher bitfinexBookPublisher() {
		return new BitfinexBookPublisher();
	}

	@Bean
	public BitfinexOfferCreatePublisher bitfinexOfferCreatePublisher() {
		return new BitfinexOfferCreatePublisher();
	}

	@Bean
	public RipplePaymentCreateListener paymentCreateListener() {
		return new RipplePaymentCreateListener();
	}

	@Bean
	public RippleMessageLoggerListener rippleMessageLoggerListener() {
		return new RippleMessageLoggerListener();
	}

	// @Bean
	// public RippleMessageStoreListener rippleMessageStoreListener() {
	// return new RippleMessageStoreListener();
	// }

	@Bean
	public RippleMultiPathfindPublisher rippleMultiPathfindPublisher() {

		List<Pair<Issue, Issue>> listExchanges = RippleArbitragerPathfindPublisher.parseExchanges(exchanges);
		// TODO check #
		HashMap<String, Double> listAmounts = RippleArbitragerPathfindPublisher
				.parseExchangeAmountsFromProperties(exchangesAmounts);
		Pathfind pathfind1 = new Pathfind(rippleAccount, rippleAccount,
				listAmounts.get(listExchanges.get(0).getValue0().toString()), listExchanges.get(0).getValue0(),
				listExchanges.get(0));
		Pathfind pathfind2 = new Pathfind(rippleAccount, rippleAccount,
				listAmounts.get(listExchanges.get(0).getValue1().toString()), listExchanges.get(0).getValue1(),
				listExchanges.get(0));

		List<Pathfind> list = new ArrayList<>();
		list.add(pathfind1);
		list.add(pathfind2);

		return new RippleMultiPathfindPublisher(list);
	}

	@Bean
	public RippleArbitragerPathfindPublisher rippleArbitragerPathfindPublisher() {
		return new RippleArbitragerPathfindPublisher();
	}

	@Bean
	public RippexArbitrager rippexArbitrager() {
		return new RippexArbitrager();
	}

	public static void main(String[] args) {
		SpringApplication.run(ArbitragerApplication.class, args);
	}

	private ConfigurableBeanFactory beanFactory;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = (ConfigurableBeanFactory) beanFactory;
	}

}
