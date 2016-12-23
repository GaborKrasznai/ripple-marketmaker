package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
// @EnableJms
public class Application {

	@Bean
	public ArbitragerChannelListener channelListener() {
		return new ArbitragerChannelListener();
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
