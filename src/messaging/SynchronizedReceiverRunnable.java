package messaging;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import service.ServiceLauncher;

import com.jcraft.jsch.JSchException;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.ShutdownSignalException;

import driver.Deposit;
import driver.FileManager;
import driver.InputReGen;
import driver.ProtocolInterchange;
import driver.Query;
import engine.IdSystem;
import engine.PropertiesReader;
import engine.SftpDeposit;

public class SynchronizedReceiverRunnable implements Runnable{
	
	private MessagingConfig msgconf;
	private AbstractEnvironment absenv;
	private Receiver receiver;
	private Logger log;
	private int retry_interval;
	private int retry_threshold;
	private  String propertiesPath;
		
	public SynchronizedReceiverRunnable(String propertiesPath, MessagingConfig msgconf, AbstractEnvironment absenv) throws IOException,
	ClassNotFoundException{
		this.msgconf = msgconf;
		this.absenv = absenv;
		this.receiver = new Receiver(msgconf,MessagingOperationTypes.RECEIVE_REQUESTS);
		this.retry_interval = msgconf.getInterval();
		this.retry_threshold = msgconf.getThreshold();
		this.propertiesPath = propertiesPath;
	}

	
	
	
	public void run() throws IllegalMonitorStateException{
		boolean runInfinite = true;
		JSONObject requestMessage;
		PropertiesReader property = new PropertiesReader(this.propertiesPath);

		while(runInfinite){
			try {
				log.info("[Agent server: Listering Queries from Messaging System]");
			
				requestMessage = this.receiver.getJsonMessage();
		
				log.info("[Agent server: One Json Query received]\n"+requestMessage.toJSONString());
				
				if (!requestMessage.isEmpty()){
					
					InputReGen reGenInput = new InputReGen(requestMessage);
					requestMessage = reGenInput.getReGenInput();
					

					String responseKey = (String) requestMessage.get("responseKey");	
					msgconf.setResponseRoutingKey(responseKey);
					
					
					String type = (String) requestMessage.get("operation");
					
					log.info("[Agent server: Request]" + type);
					log.info("[Agent server: Message Response Routing Key]"+responseKey);
					
					try{
						
						IdSystem idSystem = new IdSystem(propertiesPath);
						idSystem.connectServer();
						
						if (type.equals("query")){
							
							Query query = new Query(requestMessage);
							JSONObject newobject = query.createOutput(query.getResponseKey(), query.getUniqueId(), 
									query.getOperation());
																
							String uniqueId = (String) newobject.get("uniqueID");
							
							if (uniqueId != null){
								String result = idSystem.query(uniqueId);
								
								log.info("[Agent server: Query a unique ID In database]" + result);
								
								idSystem.disconnetServer();
								
								newobject.put("Status", result);	
								String folder = (String) requestMessage.get("ROID");
								newobject.put("ROID", folder);
								
								try {
									Sender sender = new Sender(msgconf,MessagingOperationTypes.SEND_RESPONSE);
									sender = new Sender(msgconf,MessagingOperationTypes.SEND_RESPONSE);
									sender.sendJsonMessage(newobject);
									log.info("[Agent Server: Send Query Response to Client]");
									sender.closeConnectionAndChannel();
								} catch (ShutdownSignalException e){
									log.info("[Agent Server: Messaging Response Error]" + e.toString());
								} catch (IOException e){
									log.info("[Agent Server: Messaging Response Error]" + e.toString());
								} 
							} else {
								
								idSystem.disconnetServer();
								
								log.info("[Agent server: Query fails because of an unknown ID]");
								
								newobject.put("Status", "Query fails: Cannot find this Object.");
								
								String folder = (String) requestMessage.get("ROID");
								newobject.put("ROID", folder);
								
								try {
									Sender sender = new Sender(msgconf,MessagingOperationTypes.SEND_RESPONSE);
									sender = new Sender(msgconf,MessagingOperationTypes.SEND_RESPONSE);
									sender.sendJsonMessage(newobject);
									log.info("[Agent Server: Send Query Response to Client]");
									sender.closeConnectionAndChannel();
								} catch (ShutdownSignalException e){
									log.info("[Agent Server: Messaging Response Error]" + e.toString());
								} catch (IOException e){
									log.info("[Agent Server: Messaging Response Error]" + e.toString());
								} 
							}
											
						}else if (type.equals("deposit")){
							
							Deposit deposit = new Deposit(requestMessage);
							
							JSONObject newobject = deposit.createOutput(deposit.getResponseKey(), deposit.getUniqueId(), 
									deposit.getOperation(), deposit.getTargetDirectory());
							
							newobject.put("Status", "Incoming message");
												
							Date dt =  new Date(System.currentTimeMillis());
								
							String check = idSystem.query((String) newobject.get("uniqueID"));
							
							
							if (check.equals("Not exist")){
													
								newobject.replace("Status", "Processing");
								
								String folder = (String) requestMessage.get("ROID");
								newobject.put("ROID", folder);
								
								try {
									
								    Sender sender = new Sender(msgconf,MessagingOperationTypes.SEND_RESPONSE);
									sender.sendJsonMessage(newobject);
									log.info("[Agent Server: Send First Deposit Response to Client]");
									sender.closeConnectionAndChannel();
								
																
									String oldDir = (String) newobject.get("targetDirectory");							
									String interchangeLocation = property.getProperties().getProperty("SeadInterchange");
									
									
									ProtocolInterchange interchange = new ProtocolInterchange(this.propertiesPath, interchangeLocation);			
									
									folder  = folder.substring(folder.lastIndexOf("/")+1, folder.length());
									ArrayList<String> errorLinks = new ArrayList<String>();
									try{
										if (oldDir.contains(" ")){
											List<String> dirList = new ArrayList<String>();
											String[] list = oldDir.split(" ");
											for (int i = 0; i < list.length ; i++){
												dirList.add(list[i]);
											}							
											interchange.verifyAndDownload(dirList,folder);		
										}else {	
											interchange.verifyAndDownload(oldDir, folder);
										}
										
										errorLinks = interchange.getErrorLinks();
										
										log.info("[Agent server: download Research Object into local server]");	
										String removePath = interchange.getDownlaodPath();
										newobject.replace("targetDirectory", removePath);
																			
										idSystem.insert((String) newobject.get("uniqueID"), "processing", dt, "none");
										log.info("[Agent server: Insert a unique ID information into database]" + newobject.get("uniqueID"));	
										
										try {
											
											SftpDeposit sftpDeposit = new SftpDeposit(propertiesPath);						
									
											String source = property.getProperties().getProperty("SDA");
											
											if (new File((String) newobject.get("targetDirectory")).exists()){
												sftpDeposit.depositDirectory((String) newobject.get("targetDirectory"), source);
												log.info("[Agent server: Deposit Research Object into Repository]");	
								
												sftpDeposit.depositDone();
					
												if (sftpDeposit.getSDAex()){
													if (errorLinks.isEmpty()){
														newobject.replace("Status", "ERROR! Folder is already existed");
														log.info("[Agent server: ERROR! Folder is already existed]");
													}else{
														
														JSONArray list = new JSONArray();
														for (String link : errorLinks){
															JSONObject errors = new JSONObject();
															errors.put("Error Link", link);
															list.add(errors);
														}
														newobject.replace("Status", "ERROR! Folder is already existed(Cannot download some objects)");
														newobject.put("Cannot find these links", list);
														log.info("[Agent server: ERROR! Folder is already existed(Cannot download some objects)]");
													}
												}else if (!errorLinks.isEmpty()){
													JSONArray list = new JSONArray();
													for (String link : errorLinks){
														JSONObject errors = new JSONObject();
														errors.put("Error Link", link);
														list.add(errors);
													}
													newobject.replace("Status", "ERROR! Cannot download some objects");
													newobject.put("Cannot find these links", list);
													log.info("[Agent server: ERROR! Cannot download some objects]");
												}else{
													newobject.replace("Status", "done");
												}
											}else{
												newobject.replace("Status", "ERROR! Fail to create Temporary RO folder");
												newobject.put("Cannot find these links", (String) newobject.get("targetDirectory"));
												log.info("[Agent server: ERROR! Fail to create Temporary RO folder]");
											}
											
											
											
											idSystem.update((String) newobject.get("uniqueID"), (String) newobject.get("Status"), "none");					
											log.info("[Agent Server: Update a record]" + newobject.get("uniqueID"));	
											idSystem.disconnetServer();
											
											FileManager manager = new FileManager();
											manager.removeTempFolder(newobject.get("targetDirectory").toString()+File.separator);
											log.info("[Agent Server: Remove temporary folder]");
											
											newobject.replace("targetDirectory", source + folder);
												
											try {
												sender = new Sender(msgconf,MessagingOperationTypes.SEND_RESPONSE);
												sender.sendJsonMessage(newobject);
												log.info("[Agent Server: Send Second Deposit Response to Client]");
												sender.closeConnectionAndChannel();
											} catch (ShutdownSignalException e){
												log.info("Agent Server: Messaging Response Error]" + e.toString());
											} catch (IOException e){
												log.info("Agent Server: Messaging Response Error]" + e.toString());
											} 
											
											
											
											
										} catch (JSchException e) {
											log.info("[Agent Server: Deposit fails]" + e.toString());
										}
									}catch(Exception e){
										log.info("[Agent Server: Downloading to local server fails]" + e.toString());
									}	
								} catch (ShutdownSignalException e){
									log.info("[Agent Server: Messaging Response Error]" + e.toString());
								} catch (IOException e){
									log.info("[Agent Server: Messaging Response Error]" + e.toString());
								}
								
							} else {
												
								newobject.replace("Status", "Duplicated uniqueID/Unique ID is not existed");
								
								idSystem.disconnetServer();
								
								try {
									Sender sender = new Sender(msgconf,MessagingOperationTypes.SEND_RESPONSE);
									sender.sendJsonMessage(newobject);
									log.info("[Agent Server: Send Error Deposit Response to Client]");
									sender.closeConnectionAndChannel();
								} catch (ShutdownSignalException e){
									log.info("Agent Server: Messaging Response Error]" + e.toString());
								} catch (IOException e){
									log.info("Agent Server: Messaging Response Error]" + e.toString());
								} 
							}
			
						}else{
							System.err.println("[Agent Server: Request Error (not Deposit or Query)]");
						}
					}catch(Exception e){
						log.info("[Agent Server: MySQL fails]"+e.toString());
						
					}	
			
				} else {
					System.err.println("[Agent Server: Incoming message is empty]");
				}
				
			
			} catch (ShutdownSignalException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				//log.error("Error", e);
				
				this.receiver.abortChannel();
				this.receiver.abortConnection();
				
				boolean reConnected = false;
				int retry_count = 0;
				
				while (reConnected){
					if (retry_count > this.retry_threshold){
						ServiceLauncher.shutDown();
						return;
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
						Thread.sleep(this.retry_interval*1000);
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
			} catch (ConsumerCancelledException e){
				e.printStackTrace();
				log.error("Error", e);
			}catch (InterruptedException e){
				e.printStackTrace();
				log.error("Error", e);
			}catch (IOException e){
				e.printStackTrace();
				log.error("Error", e);
			}catch (ParseException e){
				e.printStackTrace();
				log.error("Error", e);
			}catch (Exception e){
				e.printStackTrace();
				log.error("Error", e);
			}
			
		}
	}
	
	public static void main(String[] args){
		
	}

}
