package messaging;

import java.util.Properties;

import engine.PropertiesReader;

public class MessagingConfig {
	private Properties properties;
	private String username;
	private String password;
	private String hostname;
	private int hostport;
	private String virtualhost;
	private String exchangename;
	private String queuename;
	private String routingkey;
	private int threshold;
	private int interval;
	private int daemon;
	
	
	public MessagingConfig(String propertiesPath){
		 properties = new PropertiesReader(propertiesPath).getProperties();
		 username = properties.getProperty("messaging.username");
		 password = properties.getProperty("messaging.password");
		 hostname = properties.getProperty("messaging.hostname");
		 hostport = Integer.parseInt(properties.getProperty("messaging.hostport"));
		 virtualhost = properties.getProperty("messaging.virtualhost");
		 exchangename = properties.getProperty("messaging.exchangename");		
		 queuename = properties.getProperty("messaging.queuename");
		 routingkey = properties.getProperty("messaging.routingkey");		
		 threshold = Integer.parseInt(properties.getProperty("messaging.retry.threshold"));
		 interval = Integer.parseInt(properties.getProperty("messaging.retry.interval"));
		 daemon = Integer.parseInt(properties.getProperty("messaging.daemon"));
	}
	
	
	
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public int getHostport() {
		return hostport;
	}
	public void setHostport(int hostport) {
		this.hostport = hostport;
	}
	public String getVirtualhost() {
		return virtualhost;
	}
	public void setVirtualhost(String virtualhost) {
		this.virtualhost = virtualhost;
	}
	public String getExchangename() {
		return exchangename;
	}
	public void setExchangename(String exchangename) {
		this.exchangename = exchangename;
	}
	public String getQueuename() {
		return queuename;
	}
	public void setQueuename(String queuename) {
		this.queuename = queuename;
	}
	public String getRoutingkey() {
		return routingkey;
	}
	public void setRoutingkey(String routinkey) {
		this.routingkey = routinkey;
	}
	public int getThreshold() {
		return threshold;
	}
	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}
	public int getInterval() {
		return interval;
	}
	public void setInterval(int interval) {
		this.interval = interval;
	}
	
	
	
	public String getRequestExchangename(){
		return this.exchangename+"_request";
	}
	
	
	
	public String getRequestQueuename(){
		return this.queuename+"_request";
	}
	public String getRequestRoutingkey(){
		return this.routingkey+"_request";
	}
	public String getResponseQueuename(){
		return this.queuename+"_response";
	}
	
	
	
	
	
	
	public String getResponseExchangename(){
		return this.exchangename+"_response";
	}
	
	
	public String getResponseRoutingkey(){
		return this.routingkey+"_response";
	}

	public void setResponseRoutingKey(String responseRoutingKey){
		this.routingkey = responseRoutingKey;
	}




	public int getDaemon() {
		return daemon;
	}

	public void setDaemon(int daemon) {
		this.daemon = daemon;
	}



}
