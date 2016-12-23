package hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class GreetingController {

	private static Logger log = LoggerFactory.getLogger(GreetingController.class);

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private SimpMessagingTemplate clientTemplate;

	@MessageMapping("/hello")
	@SendTo("/topic/greetings")
	public void greeting(String message) throws Exception {
		log.info(message);
	}

}
