package client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import service.ServiceLauncher;

import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.ShutdownSignalException;

import driver.InputReGen;
import messaging.MessagingConfig;
import messaging.MessagingOperationTypes;
import messaging.MessagingQueue;
import messaging.Receiver;
import messaging.Sender;

public class SynchronizedClient {

	private Receiver receiver;
	private Sender sender;
	private MessagingConfig msgconf;
	
	private Logger log = Logger.getLogger(getClass());
	
	
	public SynchronizedClient(MessagingConfig msgconf){
		this.msgconf = msgconf;
		try {
			this.sender = new Sender(msgconf, MessagingOperationTypes.SEND_REQUEST);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public String sendRequest(JSONObject object){
		String responseRoutingKey = UUID.randomUUID().toString();
		
		
		try {
			new MessagingQueue().new QueueBind(this.msgconf, this.msgconf.getResponseExchangename(),
					this.msgconf.getResponseQueuename(), responseRoutingKey);
			object.put("responseKey", responseRoutingKey);
			this.sender.sendJsonMessage(object);
			return responseRoutingKey;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ShutdownSignalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;		
		 		 
	}
	
	public JSONObject reveiveResponse(String responseRoutingKey){
			
		try {
			this.msgconf.setResponseRoutingKey(responseRoutingKey);
			this.receiver = new Receiver(this.msgconf,MessagingOperationTypes.RECEIVE_RESPONSE);
			JSONObject object = receiver.getJsonMessage();
			this.receiver.closeChannel();
			this.receiver.closeConnection();
			new MessagingQueue().new QueueUnBind(this.msgconf, this.msgconf.getResponseExchangename(),
					this.msgconf.getResponseQueuename(), responseRoutingKey);
			return object;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			log.error("Error", e);
			
			this.receiver.abortChannel();
			this.receiver.abortConnection();
			
			boolean reConnected = false;
			int retry_count = 0;
			
			while (reConnected){
				if (retry_count > this.msgconf.getThreshold()){
					ServiceLauncher.shutDown();
					
				}
				
				retry_count++;
				reConnected = false;
				
				try{
					log.info("Reconnecting to Messaging Server");
					this.receiver.createConnection();
					this.receiver.createChannel();
					reConnected = true;
				} catch (IOException e1){
					log.error("Can't connect to Messaging Server");
					reConnected = false;
					e1.printStackTrace();
				}
				
				try{
					Thread.sleep(this.msgconf.getInterval()*1000);
				}catch(InterruptedException e3){
					e3.printStackTrace();
				}
				
				if (reConnected){
					try {
						this.receiver.modifyChannel();
						log.info("Connected to Messaging Server");
						break;
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						this.receiver.closeChannel();
						this.receiver.closeConnection();
						reConnected = false;
					}
					
				}
			}
		}
			
		return null;
		
	}
	
	
	
	
	public void closeChannel(){
		this.receiver.closeChannel();
		this.sender.closeChannel();;
	}
	
	
	public void closeConnection(){
		this.receiver.closeConnection();
		this.sender.closeConnection();;
	}
	
	
	public JSONObject request(JSONObject object){
		JSONObject response;

		String responseKey = this.sendRequest(object);
		response = this.reveiveResponse(responseKey);
	
		return response;
	}
	
	
	
	
	public static void main(String[] args) throws Exception{
		if (args.length < 2){
			System.err.println("ERROR: properties file or input file not specified");
            System.err.println("Usage:  ServiceLauncher <propertiesFilePath> <inputFilePath>");
            throw new Exception("ERROR: properties file or input file not specified");
		}
		String propertyPath = args[0];
		
		String queryFile = args[1];
		
		long startTime = System.currentTimeMillis();
		MessagingConfig msgconf=new MessagingConfig(propertyPath);
		SynchronizedClient synchronizedClient=new SynchronizedClient(msgconf);
		
		String responseKey;
		JSONObject object = new JSONObject();
		JSONObject result1 = new JSONObject();
		JSONObject result2 = new JSONObject();
		JSONParser parser = new JSONParser();
		
		
		try {
			
			Object obj = parser.parse(new FileReader(queryFile));
			object = (JSONObject) obj;	
			
			responseKey = synchronizedClient.sendRequest(object);
			
			InputReGen clientFile = new InputReGen(object);
			
			object = clientFile.getReGenInput();
		
			
			String operation = (String) object.get("operation");
			
			
			
			if (operation.equals("query")){
		
				result1=synchronizedClient.reveiveResponse(responseKey);
			
				synchronizedClient.closeChannel();
				synchronizedClient.closeConnection();
				System.out.println("Result:\n"+result1);
			}else if (operation.equals("deposit")){
				
				result1=synchronizedClient.reveiveResponse(responseKey);
				System.out.println("Result:\n"+result1);
				
				if (!result1.get("Status").equals("Duplicated uniqueID")){
				
					result2=synchronizedClient.reveiveResponse(responseKey);
					System.out.println("Result:\n"+result2);
		
				}
				synchronizedClient.closeChannel();
				synchronizedClient.closeConnection();
			}
			
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

		long endTime = System.currentTimeMillis();
		System.out.println("Total Execution Time: "+(endTime-startTime));
		
		
	}
	
	
}
