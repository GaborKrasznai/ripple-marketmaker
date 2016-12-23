package marketmaker.application;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jms.annotation.EnableJms;

import marketmaker.services.ripple.BaseRippleClient;

/**
 * Created by rmartins on 2/22/15.
 */
@SpringBootApplication
@EnableJms
@EnableConfigurationProperties
@EnableJpaRepositories(basePackages = { "marketmaker.entities" })
@EntityScan({ "marketmaker.entities" })
@ComponentScan({ "marketmaker" })
// @PropertySource("config/webapp.properties")
public class WebAppApplication extends BaseRippleClient {

	private static Logger log = LoggerFactory.getLogger(WebAppApplication.class);

	@Bean
	public RippleMessageStoreListener rippleMessageStoreListener() {
		return new RippleMessageStoreListener();
	}

	public static void main(String[] args) throws IOException {
		SpringApplication app = new SpringApplication(WebAppApplication.class);
		app.setWebEnvironment(true);
		ConfigurableApplicationContext ctx = app.run(args);
	}

	@PostConstruct
	public void init() {

	}

}
