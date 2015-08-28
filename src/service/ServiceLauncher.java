package service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import messaging.AbstractEnvironment;
import messaging.MessagingConfig;
import messaging.MessagingDaemons;
import messaging.MessagingDaemonsConfig;

import org.apache.log4j.Logger;


public class ServiceLauncher {
	
	private static String propertiesPath;
	
	private static boolean onOf = false;
	
	private static final Logger log = Logger.getLogger(ServiceLauncher.class);
	private static AbstractEnvironment absenv = null;
	
	public static void start(String propertyPath) {
		if (!onOf){
			propertiesPath = propertyPath;
				
			if (log.isDebugEnabled()){
				log.debug("Matchmaker Agent started");
			}
		
			onOf = true;
		}
	}
	
	
	
	public static void shutDown(){
		if (onOf){
			
			if (log.isDebugEnabled()){
				log.debug("Matchmaker Agent stopped");
			}
			
			onOf = false;
		}
	}
	
	
	
	public static boolean startMessageReceiverDaemon() throws ClassNotFoundException, IOException, TimeoutException{
	
		MessagingConfig msgconf;
		
		MessagingDaemons msgdm;
		
		
		msgconf = new MessagingConfig(propertiesPath);
		
		MessagingDaemonsConfig msgdmconf = new MessagingDaemonsConfig(msgconf.getDaemon());
		
		msgdm = new MessagingDaemons(propertiesPath, msgdmconf, msgconf, absenv);
		
		msgdm.start();
		
		
		return true;
	}
	
	
	
	
	
	
	public static void main(String[] args){
		try{
			
			if (args.length < 1){
				System.err.println("ERROR: properties file not specified");
                System.err.println("Usage:  ServiceLauncher <propertiesFilePath>");
                throw new Exception("ERROR: properties file not specified");
			}
			
			String propertyPath = args[0];
			ServiceLauncher.start(propertyPath);
			
			if (!ServiceLauncher.startMessageReceiverDaemon()){
				System.out.println("shutdown");
				shutDown();
			}
			else {
				System.out.println("[Agent Server Working]");
			}
			
		} catch (Throwable e){
			log.fatal("Unable to launch service", e);			
			shutDown();
		}
	}

}
