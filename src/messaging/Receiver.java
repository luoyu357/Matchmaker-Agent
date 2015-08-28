package messaging;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;




public class Receiver {
	
	public ConnectionFactory factory;
	public Connection conn;
	public Channel channel;
	public String ExchangeName;
	public String QueueName;
	public String RoutingKey;
	public QueueingConsumer consumer;
	
	
	
	public Receiver(MessagingConfig msgconf, MessagingOperationTypes types) throws IOException{
		this.factory = new ConnectionFactory();
		this.factory.setUsername(msgconf.getUsername());
		this.factory.setPassword(msgconf.getPassword());
		this.factory.setVirtualHost(msgconf.getVirtualhost());
		this.factory.setHost(msgconf.getHostname());
		this.factory.setPort(msgconf.getHostport());
		
		switch(types){
		case RECEIVE_REQUESTS:
			this.ExchangeName = msgconf.getRequestExchangename();
			this.QueueName = msgconf.getRequestQueuename();
			this.RoutingKey = msgconf.getRequestRoutingkey();
			break;
			
		case RECEIVE_RESPONSE:
			this.ExchangeName = msgconf.getResponseExchangename();
			this.QueueName = msgconf.getResponseQueuename();
			this.RoutingKey = msgconf.getResponseRoutingkey();
			break;
			
		default:
			try {
				throw new Exception("OperationType: "+types.toString()+" not supported.");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}
		
		createConnection();
		createChannel();
		modifyChannel();
		
	}
	
	public void createConnection() throws IOException{
		try{
			this.conn = this.factory.newConnection();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void createChannel() throws IOException{
		this.channel = this.conn.createChannel();
	}
	
	public void modifyChannel() throws IOException{
		this.channel.exchangeDeclare(ExchangeName, "direct", true);
		this.channel.queueDeclare(QueueName, true, false, false, null);
		this.channel.queueBind(QueueName, ExchangeName, RoutingKey);
	
		this.consumer = new QueueingConsumer(this.channel);
		this.channel.basicConsume(QueueName, false, this.consumer);
	
	}

	
	public void closeConnection(){
		try{
			if (this.conn.isOpen()){
				this.conn.close();
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	
	public void closeChannel(){
		if (this.channel.isOpen()){
			try {
				this.channel.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	
	public void abortChannel(){
		try{
			this.channel.abort();;
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	public void abortConnection(){
		try{
			this.conn.abort();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public String getMessage() throws ShutdownSignalException, ConsumerCancelledException, 
	InterruptedException, IOException{
		String message = null;
		QueueingConsumer.Delivery delivery = consumer.nextDelivery();
		message = new String(delivery.getBody());
		channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
		return message;
	}
	
	public JSONObject getJsonMessage() throws ShutdownSignalException, ConsumerCancelledException, 
	InterruptedException, IOException, ParseException{
		JSONObject json = null;
		JSONParser parser = new JSONParser();
		String message = null;
		QueueingConsumer.Delivery delivery = consumer.nextDelivery();
		message = new String(delivery.getBody());
		channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
		json = (JSONObject) parser.parse(message);
		return json;
	}
	
	
}
