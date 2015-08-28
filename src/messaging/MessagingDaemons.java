package messaging;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

public class MessagingDaemons {
	
	private Thread[] messagingDaemons;
	private int numberOfMessagingDaemon;
	
	private Logger log;
	
	
	public MessagingDaemons(String propertiesPath, MessagingDaemonsConfig msgdmconf,
			MessagingConfig msgconf, AbstractEnvironment absenv) throws IOException, TimeoutException, ClassNotFoundException{
		
		this.numberOfMessagingDaemon = msgdmconf.getNumberOfMessagingDaemon();
		this.messagingDaemons = new Thread[numberOfMessagingDaemon];
		
		SynchronizedReceiverRunnable srr;
		try {
			srr = new SynchronizedReceiverRunnable(propertiesPath, msgconf, absenv);
			for (int i = 0; i < this.numberOfMessagingDaemon; i++){
				this.messagingDaemons[i] = new Thread(srr);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	
	
	public void start() throws IllegalMonitorStateException{
		log = Logger.getLogger(MessagingDaemons.class);
		
		for (int i = 0; i < this.numberOfMessagingDaemon; i++){
			this.messagingDaemons[i].start();
			log.info("Messaging Daemon [" + i +"] Started");
		}
	}

}
