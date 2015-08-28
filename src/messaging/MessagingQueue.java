package messaging;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class MessagingQueue {
	
	public class QueueBind{
		public ConnectionFactory factory;
		public Connection conn;
		public Channel channel;
		public String ExchangeName;
		public String QueueName;
		public String RoutingKey;
		public QueueingConsumer consumer;
		
		
		
		public QueueBind(MessagingConfig msgconf, String exchangeName,String queueName, 
				String routingKey) throws IOException{
			
			this.factory = new ConnectionFactory();
			this.factory.setUsername(msgconf.getUsername());
			this.factory.setPassword(msgconf.getPassword());
			this.factory.setVirtualHost(msgconf.getVirtualhost());
			this.factory.setHost(msgconf.getHostname());
			this.factory.setPort(msgconf.getHostport());
			this.ExchangeName = exchangeName;
			this.QueueName = queueName;
			this.RoutingKey = routingKey;
			
			createConnection();
			createChannel();
			bind();
			closeChannel();
			closeConnection();
			
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
		
		public void bind() throws IOException {
			
			this.channel.exchangeDeclare(ExchangeName, "direct", true);
			this.channel.queueDeclare(QueueName, true, false, false, null);
			this.channel.queueBind(QueueName, ExchangeName, RoutingKey);
		
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
				try{
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
		
	}
	
	
	public class QueueUnBind{
		public ConnectionFactory factory;
		public Connection conn;
		public Channel channel;
		public String ExchangeName;
		public String QueueName;
		public String RoutingKey;
		public QueueingConsumer consumer;
		
		
		
		public QueueUnBind(MessagingConfig msgconf, String exchangeName,String queueName, 
				String routingKey) throws IOException, TimeoutException{
			
			this.factory = new ConnectionFactory();
			this.factory.setUsername(msgconf.getUsername());
			this.factory.setPassword(msgconf.getPassword());
			this.factory.setVirtualHost(msgconf.getVirtualhost());
			this.factory.setHost(msgconf.getHostname());
			this.factory.setPort(msgconf.getHostport());
			this.ExchangeName = exchangeName;
			this.QueueName = queueName;
			this.RoutingKey = routingKey;
			
			createConnection();
			createChannel();
			unbind();
			closeChannel();
			closeConnection();
			
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
		
	
		
		public void unbind() throws IOException{
			
			this.channel.exchangeDeclare(ExchangeName, "direct", true);
			this.channel.queueDeclare(QueueName, true, false, false, null); 
			this.channel.queueUnbind(QueueName, ExchangeName, RoutingKey);
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
				try{
					this.channel.close();
				}catch(Exception e){
					e.printStackTrace();}	
			
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
		
	}

}
