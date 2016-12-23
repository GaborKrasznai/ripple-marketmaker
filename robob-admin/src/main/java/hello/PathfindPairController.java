package hello;

import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PathfindPairController {

	private static Logger log = LoggerFactory.getLogger(PathfindPairController.class);

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private SimpMessagingTemplate clientTemplate;

//	@MessageMapping("/pathfind")
//	@SendTo("/topic/pathfinds")
//	public void pathfinds(String message) throws Exception {
//		log.info(message);
//	}
//
//	@JmsListener(destination = "pathfind_pair")
//	public void onPathfindPair(String message) {
//		if (clientTemplate != null) {
//			clientTemplate.convertAndSend("/topic/pathfinds", message);
//		}
//	}

}
