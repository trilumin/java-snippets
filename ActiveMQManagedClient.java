

public class ActiveMQManagedClient implements Managed, HealthyMQClient{
    
    private static final Logger log = LoggerFactory.getLogger(CookieServiceMQClient.class);
    private static final String HEALTHCHECK_MSG = "healthcheck-message";
    private static final String HEALTHCHECK_QUEUE_NAME = "HEALTHCHECK.QUEUE";
    private static final String ID_QUEUE_KEY = "cookie-id";
    private static final String ID_QUEUE_KEY = "dsp-id";
    private final ActiveMQManagedClientConfiguration config;
    private volatile boolean isOnline = false;
    private PooledConnectionFactory pooledConnectionFactory;
    private Connection connection;
    private Session session;
    private MessageProducer producer;
    private Destination optOutQueue;
    private Destination mapCookieQueue;
    private Destination  healthCheckQueue;
    
    
    
    public ActiveMQManagedClient(ActiveMQManagedClientConfiguration activeMQManagedClientConfiguration) {
   	 this.config = activeMQManagedClientConfiguration;
    }
    
    
    public void queueOptOutCookie(String cookieId) {
   	 try {
   		 // Create message
   	 	MapMessage mapMessage = session.createMapMessage();
   	 	mapMessage.setString(ID_QUEUE_KEY, cookieId);
   	 	// Send message
   	 	producer.send(optOutQueue, mapMessage, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, 3600000);
   	 } catch (JMSException e) {
   	 	log.error ("JMSException: " + e.getMessage());
   	 } catch (Exception e) {
   		 log.error("Exception: " + e.getMessage());
   	 }     
    }
    
    
    public void queueMappedCookie(String cookieId, String dspId) {
   	 try {
   		 MapMessage message = session.createMapMessage();
   		 message.setString(ID_QUEUE_KEY, dspId);
   		 message.setString(ID_QUEUE_KEY, cookieId);
   		 producer.send(mapCookieQueue, message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, 3600000);
   	 } catch (JMSException e) {
   	 	log.error ("JMSException: " + e.getMessage());
   	 } catch (Exception e) {
   		 log.error("Exception: " + e.getMessage());
   	 }     
    }
    
    public void run()    {
   	 try{
   		 pooledConnectionFactory = new PooledConnectionFactory(config.getBrokerURL());
   		 connection = pooledConnectionFactory.createConnection();
   		 connection.start();
   		 session = connection.createSession(false, DUPS_OK_ACKNOWLEDGE);
   		 producer = session.createProducer(null);
   		 //setup queue destinations
   		 optOutQueue = session.createQueue(config.getOptoutQueueName());
   		 mapCookieQueue = session.createQueue(config.getMapperQueueName());
   		 // Setup Healthcheck destination and consumer
   		 healthCheckQueue = session.createQueue(HEALTHCHECK_QUEUE_NAME);
   		 isOnline = true;
   	 }catch(Exception e) {
   		 log.error(e.getMessage());
   	 }finally {
   		 if(connection != null) {
   			 try{
   				 connection.close();
   			 }catch(Exception e){
   				 log.error(e.getMessage());
   			 }
   		 }
   		 
   		 if(session != null) {
   			 try{
   				 session.close();
   			 }catch(Exception e) {
   				 log.error(e.getMessage());
   			 }
   		 }
   	 }
    }
    
    
    @Override
    public boolean isHealthy() {
   	 if(isOnline == false) {
   		 run();
   	 }
   	 
   	 MessageConsumer consumer = null;
   	 try{
   		 producer.send(healthCheckQueue, session.createTextMessage(HEALTHCHECK_MSG),
   				 DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, 3600000);
   		 consumer = session.createConsumer(healthCheckQueue);
   		 
   		 TextMessage result = (TextMessage) consumer.receive();
   		 result.acknowledge();
   		 if(result.getText().equals(HEALTHCHECK_MSG)){
   			 return true;
   		 }
   		 
   	 }catch(Exception e) {
   		 log.error(e.getMessage());
   	 }finally{
   		 if(consumer != null) {
   			 try{
   				 consumer.close();
   			 }catch(Exception e) {
   				 log.error(e.getMessage());
   			 }
   		 }
   	 }
   	 return false;
    }
    
    
    
    @Override
    public void start() throws Exception {
   	 run();
   	 log.info("starting " + this.getClass().getName());
    }

    
    @Override
    public void stop() throws Exception {
   	 try{
   		 if(connection != null){
   			 connection.close();
   		 }
   		 if(session != null) {
   			 session.close();
   		 }
   	 }catch(Exception e) {
   		 log.error(e.getMessage());
   	 }finally{
   		 log.info("closed all connections");
   	 }
   	 
   	 log.info("stopping: " + this.getClass().getName());
    }
    
    
    

}


