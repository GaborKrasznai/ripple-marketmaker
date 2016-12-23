package marketmaker.application;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.annotation.EnableScheduling;

import marketmaker.services.ripple.BaseRippleClient;
import marketmaker.services.ripple.RippleOfferCancelListener;

/**
 * Created by rmartins on 8/3/15.
 */
@EnableAutoConfiguration
@EnableScheduling
@EnableJms
@EnableConfigurationProperties
@EnableJpaRepositories(basePackages = { "marketmaker.entities" })
@EntityScan({ "marketmaker.entities" })
@ComponentScan({ "marketmaker" })
public class RippleOfferCancelApplication extends BaseRippleClient {

	@Bean
	public RippleOfferCancelListener rippleOfferCancelListener() {
		return new RippleOfferCancelListener();
	}

	public static void main(String[] args) throws IOException {
		SpringApplication.run(RippleOfferCancelApplication.class, args);
	}

}
