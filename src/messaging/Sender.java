package messaging;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.json.simple.JSONObject;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class Sender {
	
	
	public ConnectionFactory factory;
	public Connection conn;
	public Channel channel;
	public String ExchangeName;
	public String QueueName;
	public String RoutingKey;
	public QueueingConsumer consumer;
	
	
	public Sender(MessagingConfig msgconf, MessagingOperationTypes types) throws IOException{
		this.factory = new ConnectionFactory();
		this.factory.setUsername(msgconf.getUsername());
		this.factory.setPassword(msgconf.getPassword());
		this.factory.setVirtualHost(msgconf.getVirtualhost());
		this.factory.setHost(msgconf.getHostname());
		this.factory.setPort(msgconf.getHostport());
		
		
		switch(types){
		case SEND_REQUEST:
			this.ExchangeName = msgconf.getRequestExchangename();
			this.QueueName = msgconf.getRequestQueuename();
			this.RoutingKey = msgconf.getRequestRoutingkey();
			break;
			
		case SEND_RESPONSE:
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
	
	public void closeConnectionAndChannel(){
		try{
			if (this.conn.isOpen()){
				this.conn.close();
			}
		}catch(IOException e){
			e.printStackTrace();
		}
		
		if (this.channel.isOpen()){
			try {
				this.channel.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
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
	
	
	
	public void sendMessage(String message) throws IOException{
		byte[] messageByte = message.getBytes();
		this.channel.basicPublish(ExchangeName, RoutingKey, null, messageByte);
	}
	
	public void sendJsonMessage(JSONObject object) throws IOException{
		byte[] messageByte = object.toJSONString().getBytes();
		this.channel.queueDeclare(QueueName, true, false, false, null);
		this.channel.basicPublish(this.ExchangeName, this.RoutingKey, null, messageByte);
	}

}
