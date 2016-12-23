package marketmaker.ui;

/**
 * Created by rmartins on 5/13/15.
 */
//@SpringBootApplication
//@EnableScheduling
//@EnableConfigurationProperties
//@PropertySource("config/application.properties")
public class RippleDashboardMarketMakerApplication {

//    private static Logger log = LoggerFactory.getLogger(RippleDashboardMarketMakerApplication.class);
//
//    @Autowired
//    Environment env;
//
//    @Value("${ripplemm.rippled}")
//    private String rippled;
//
//    @Bean
//    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
//    Client client() {
//        Client client = new Client(new JavaWebSocketTransportImpl());
//        client.connect(rippled);
//        return client;
//    }
//
//    @Bean
//    JedisConnectionFactory jedisConnectionFactory() {
//        return new JedisConnectionFactory();
//    }
//
//    @Bean
//    RedisTemplate<String, Object> redisTemplate() {
//        final RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
//        template.setConnectionFactory(jedisConnectionFactory());
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setHashValueSerializer(new GenericToStringSerializer<Object>(Object.class));
//        template.setValueSerializer(new GenericToStringSerializer<Object>(Object.class));
//        return template;
//    }
//
//    @Bean
//    RedisMessageListenerContainer redisContainer() {
//        final RedisMessageListenerContainer container = new RedisMessageListenerContainer();
//
//        container.setConnectionFactory(jedisConnectionFactory());
//        return container;
//    }
    
//    public static void main(String[] args) {
//        SpringApplication.run(RippleMarketMakerApplication.class, args);
//    }
}
