package util;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.rabbitmq.client.ShutdownSignalException;


public class ConnectionManager {
	
	private Connection connection = null;
	
	private static Map<String, ConnectionManager> connectionMap = new HashMap<String, ConnectionManager> (0);
	
	private ConnectionManager(String serverHost) throws IOException {
		
		ConnectionFactory cf = new ConnectionFactory();
		cf.setHost(serverHost);
		
		connection = cf.newConnection();
	}
	
	
	public static ConnectionManager getInstance(String serverHost) throws IOException {
		if (!connectionMap.containsKey(serverHost)) {
			connectionMap.put(serverHost, new ConnectionManager(serverHost));
		}
		
		return connectionMap.get(serverHost);
	}
	
	public void publish(String queue, String message) throws IOException {
		if (connection != null) {
			Channel c = connection.createChannel();
			
			c.queueDeclare(queue, false, false, false, null);
			
			
			c.basicPublish("", queue, null, message.getBytes());
			
			c.close();
		}
	}
	
	public void publishToTopic(String topicName, String message) throws IOException {
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(topicName, "topic");

        String routingKey = "*";

        channel.basicPublish(topicName, routingKey, null, message.getBytes());
        
        channel.close();
	}
	
	public void consume(String queue, Consumer consumer) throws IOException {
		Channel c = connection.createChannel();
		
		c.queueDeclare(queue, false, false, false, null);
		
		QueueingConsumer cons = new QueueingConsumer(c);
		
		boolean autoAck = true;
		
		c.basicConsume(queue, autoAck, cons);
		
		
		while (true) {
			Delivery d = null;
			try {
				d = cons.nextDelivery();
			} catch (Exception e) {
				e.printStackTrace();
				c.close();
			}
			
			String message = new String(d.getBody());
			
			if (consumer.callback(message)) {
				c.close();
				break;				
			}
		}
		
	}
	
	public void consumeTopic(String topicName, Consumer consumer) throws Exception {
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(topicName, "topic");
        String queueName = channel.queueDeclare().getQueue();

        channel.queueBind(queueName, topicName, "*");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        QueueingConsumer cons = new QueueingConsumer(channel);
        channel.basicConsume(queueName, true, cons);

        while (true) {
            QueueingConsumer.Delivery delivery = cons.nextDelivery();
            String message = new String(delivery.getBody());
            String routingKey = delivery.getEnvelope().getRoutingKey();
            
            if (consumer != null && consumer.callback(message)) {
            	channel.close();
            	break;
            }
        }
	}
	
	
//	public void findSuccessor(String nodeHashKey, String publishQueue, String consumeQueue, )
	
	public void close() throws IOException {
		if (connection != null) {
			connection.close();
		}
	}
	
	
	public void closeAllConnections() {
		for (String ip: connectionMap.keySet()) {
			try {
				connectionMap.get(ip).close();
			} catch (Exception e) {
				System.err.println("Failed to close connection to ip:" + ip);
			}
		}
	}
	
	
	public interface Consumer {
		boolean callback(String message);
	}
}
