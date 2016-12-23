package marketmaker.application;

import java.io.IOException;
import java.util.Calendar;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.ripple.client.Client;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;

import marketmaker.services.LiveFeedPublisher;
import marketmaker.services.algorithms.RippexLiquidityMakerListener;
import marketmaker.services.ripple.BaseRippleClient;
import marketmaker.services.ripple.RippleAccountBalancePublisher;
import marketmaker.services.ripple.RippleAccountOffersPublisher;
import marketmaker.services.ripple.RippleOfferBookPublisher;
import marketmaker.services.ripple.RippleOfferCancelListener;
import marketmaker.services.ripple.RippleOfferCreateListener;
import marketmaker.services.ripple.RipplePathfindPublisher;

/**
 * Created by rmartins on 2/22/15.
 */
@EnableAutoConfiguration
@EnableScheduling
@EnableJms
@EnableConfigurationProperties
@EnableJpaRepositories(basePackages = { "marketmaker.entities" })
@EntityScan({ "marketmaker.entities" })
@ComponentScan({ "marketmaker" })
public class RippleMarketMakerApplication extends BaseRippleClient {
	private static Logger log = LoggerFactory.getLogger(RippleMarketMakerApplication.class);

	@Bean
	public RippexLiquidityMakerListener rippleLiquidityTakerListener() {
		return new RippexLiquidityMakerListener();
	}

	@Bean
	public RipplePathfindPublisher ripplePathfindPublisher0() {
		return new RipplePathfindPublisher();
	}

	@Bean
	public RippleOfferBookPublisher rippleOfferBookPublisher() {
		return new RippleOfferBookPublisher();
	}

	//
	@Bean
	public RippleAccountBalancePublisher rippleAccountBalancePublisher() {
		return new RippleAccountBalancePublisher();
	}

	@Bean
	public RippleAccountOffersPublisher rippleAccountOffersPublisher() {
		return new RippleAccountOffersPublisher();
	}

	@Bean
	public RippleOfferCancelListener rippleOfferCancelListener() {
		return new RippleOfferCancelListener();
	}

	@Bean
	public RippleOfferCreateListener rippleOfferCreateListener() {
		return new RippleOfferCreateListener();
	}

	@Bean()
	public LiveFeedPublisher liveFeedPublisher() {
		return new LiveFeedPublisher();
	}

	public static void main(String[] args) throws IOException {
		SpringApplication app = new SpringApplication(RippleMarketMakerApplication.class);
		ConfigurableApplicationContext ctx = app.run(args);
	}

	@PostConstruct
	public void init() {

	}

}
