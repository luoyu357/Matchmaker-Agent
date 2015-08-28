package engine;


import engine.PropertiesReader;
import java.io.File;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class Sftp {
	
	private JSch jsch;
	private String SFTPHOST;
	private String SFTPUSER;
	private String SFTPPASS;
	
	
	private Session session;
	private Channel channel;
	public ChannelSftp channelSftp;
	private Properties properites;
	private final Logger log = Logger.getLogger(Sftp.class);
	
	
	
	public Sftp(String propertiesPath) throws JSchException{
		
		this.jsch = new JSch();
		
		properites = new PropertiesReader(propertiesPath).getProperties();
		
		SFTPHOST = properites.getProperty("SFTPHOST");
	    SFTPUSER = properites.getProperty("SFTPUSER");
	    SFTPPASS = properites.getProperty("SFTPPASS");
		
		session = jsch.getSession(this.SFTPUSER, this.SFTPHOST,22);
		
		session.setPassword(this.SFTPPASS);
		
		log.info("Sftp session and channel created");
		
	}
	
	
	/**
	 * @param directoryName
	 * create a directory on current path
	 */
	public void createDirectory(String directoryName){
		
		while(true){
			try{
				channelSftp.stat(directoryName);
			}catch(SftpException e){
				
				int tryNum = 1;
				
				try{
					channelSftp.mkdir(directoryName);
				}catch(SftpException e1){
					if(tryNum > 3){
						e1.printStackTrace();
						break;
					}else{
						tryNum++;
						continue;
					}
					
				}
				break;
			}
		}
	}
	
	
	public void downloadFlies(String sourcePath, List<String> fileNames, String destination){
		try {
			channelSftp.cd(sourcePath);
			
			for (String fileName : fileNames){		
				try{
					channelSftp.get(fileName, destination);
				}catch(SftpException e){
					e.printStackTrace();
				}		
			}
		} catch (SftpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public void downloadFil(String sourcePath, String fileName, String destination){
		
		int tryNum = 1;
		
		while(true){
			try{
				channelSftp.cd(sourcePath);
				channelSftp.get(fileName, destination);
			}catch(SftpException e){
				if(tryNum > 3){
					e.printStackTrace();
					break;
				}else{
					tryNum++;
					continue;
				}
				
			}
			break;
		}
	}
	
	
	public void downloadFil(String filePath, String destination){
		
		int tryNum = 1;
		
		while(true){
			try{
				channelSftp.get(filePath, destination);
			}catch(SftpException e){
				if(tryNum > 3){
					e.printStackTrace();
					break;
				}else{
					tryNum++;
					continue;
				}
				
			}
			break;
		}
	}
	
	
	/**
	 * @param filePath
	 * @param destination
	 * deposit a file into repository
	 */
	public void depositFile(String filePath, String destination){	
		
		int tryNum = 1; 
		
		while(true){
			try{
				
				channelSftp.put(filePath, destination);
						
			}catch(SftpException e){
				
				if(tryNum > 3){
					e.printStackTrace();
					break;
				}else{
					tryNum++;
					continue;
				}
				
			}
			break;
		}
	}
	
	
	
	
	/**
	 * connect Session and Channel
	 */
	public void connectSessionAndChannel(){
		
		
		Properties config = new Properties();
		config.put("StrictHostKeyChecking","no");
		session.setConfig(config);
		
		try{
			session.connect();	
			
			try{
				channel  = session.openChannel("sftp");
				channel.connect();
		
				channelSftp = (ChannelSftp) channel;
			}catch(Exception e1){
				e1.printStackTrace();
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * disconnect Session and Channel
	 */
	public void disConnectSessionAndChannel(){
		
		if (channelSftp.isConnected()){
			channelSftp.exit();	
			channel.disconnect();
			if (session.isConnected()){
				session.disconnect();
			}
		}
        
	}
	
	
	/**
	 * @param dir
	 * change directory to specified directory
	 */
	public void cdDir(String dir){
		try {
			this.channelSftp.cd(dir);
		} catch (SftpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * @param path
	 * @return
	 * check if the directory is existed.
	 */
	public Boolean dirIsExisted(String path){
		Boolean re = false;
		if (new File(path).isFile()){
			re = false;
		}else if (new File(path).isDirectory()){
			re = true;
		}		
		return re;
		
	}
}