package messaging;

public class MessagingDaemonsConfig {
	private int numberOfMessagingDaemon;
	
	public MessagingDaemonsConfig(int numberOfMessagingDaemon){
		this.numberOfMessagingDaemon = numberOfMessagingDaemon;
	}
	
	public void setNumberOfMessagingDaemon(int numberOfMessagingDaemon){
		this.numberOfMessagingDaemon = numberOfMessagingDaemon;
	}
	
	public int getNumberOfMessagingDaemon(){
		return this.numberOfMessagingDaemon;
	}
	
}
