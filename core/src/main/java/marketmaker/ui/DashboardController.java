package marketmaker.ui;

import org.springframework.stereotype.Controller;

/**
 * Created by rmartins on 5/13/15.
 */
@Controller
public class DashboardController {

//    private static Logger log = LoggerFactory.getLogger(DashboardController.class);
//
//    @Autowired
//    RedisMessageListenerContainer redisContainer;
//
////    @Autowired
////    private SimpMessagingTemplate template;
//
//
//    @MessageMapping("/messages")
//    @SendTo("/topic/messages")
//    public void messages() {
//
//        MessageListener messageListener = new MessageListener() {
//            @Override
//            public void onMessage(Message message, byte[] bytes) {
//                String body = new String(message.getBody(), StandardCharsets.UTF_8);
//                String channel = new String(bytes, StandardCharsets.UTF_8);
//                JSONObject json = new JSONObject();
//                try {
//                    json.put("channel", channel);
//                    json.put("body", body);
////                    template.convertAndSend("/topic/messages", json.toString(4));
//                } catch (JSONException e) {
//                    log.error(e.getMessage());
//                }
//            }
//        };
//        redisContainer.addMessageListener(messageListener, Channels.MARKET_DATA);
//    }
//
//
//    @MessageMapping("/pathfind")
//    @SendTo("/topic/pathfind")
//    public void pathfind() {
//
//        redisContainer.addMessageListener(new MessageListener() {
//            @Override
//            public void onMessage(Message message, byte[] bytes) {
//                String value = new String(message.getBody());
////                template.convertAndSend("/topic/pathfind", value);
//            }
//        }, Channels.PATH_FIND);
//
//    }
//
//
//    @MessageMapping("/opportunity")
//    @SendTo("/topic/opportunity")
//    public void opportunity() {
//        redisContainer.addMessageListener(new MessageListener() {
//            @Override
//            public void onMessage(Message message, byte[] bytes) {
//                String value = new String(message.getBody());
////                template.convertAndSend("/topic/opportunity", value);
//            }
//        }, Channels.OPPORTUNITY);
//    }

}
