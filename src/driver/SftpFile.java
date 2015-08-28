package driver;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

import engine.PropertiesReader;

public class SftpFile {
	
	private JSch jsch;
	private String sftpHost;
	private String sftpUser;
	private String sftpPass;
	
	
	private Session session = null;
	private Channel channel;
	private static ChannelSftp channelSftp;
	private Properties properites;
	private ArrayList<String> errorLinks;
	
	public SftpFile(String propertiesPath){
		this.jsch = new JSch();

		this.properites = new PropertiesReader(propertiesPath).getProperties();
		
		this.sftpHost = this.properites.getProperty("server.host");
		this.sftpUser = this.properites.getProperty("server.user");
		this.sftpPass = this.properites.getProperty("server.pass");
		
		try {
			this.session = this.jsch.getSession(sftpUser, sftpHost, 22);
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.session.setPassword(sftpPass);
		
		this.errorLinks = new ArrayList<String>();
	}
	
	
	

	
	
	
	/**
	 * @param filePaths
	 * @param destination
	 * given a list of download links, download them into local server.
	 */
	public void downloadFlies(List<String> filePaths, String destination){
		
		for (String filePath : filePaths){		
			try{
				channelSftp.get(filePath, destination);
				System.out.println("[SFTP FILE: SFTP File downloaded into local server]"+filePath);
			}catch(SftpException e){
				e.printStackTrace();
				System.out.println("[SFTP FILE: Cannot downloaded this SFTP file into local server]"+filePath);
				errorLinks.add(filePath);
			}		
		}
		
	}
	
	
	/**
	 * @param sourcePath
	 * @param fileName
	 * @param destination
	 * given a source path and its file name, download it to the local server.
	 */
	public void downloadFile(String sourcePath, String fileName, String destination){
		
		int tryNum = 1;
		
		while(true){
			try{
				channelSftp.cd(sourcePath);
				channelSftp.get(fileName, destination);
				System.out.println("[SFTP FILE: SFTP File downloaded into local server]"+sourcePath+fileName);
			}catch(SftpException e){
				if(tryNum > 3){
					e.printStackTrace();
					System.err.println("[SFTP FILE: Cannot downloaded SFTP file into local server]"+sourcePath+fileName);
					errorLinks.add(sourcePath+fileName);
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
	 * give a download link, download this linked file into local sever
	 */
	public void downloadFile(String filePath, String destination){
		
		int tryNum = 1;
		
		while(true){
			try{
				channelSftp.get(filePath, destination);
				System.out.println("[SFTP FILE: SFTP File downloaded into local server]"+filePath);
			}catch(SftpException e){
				if(tryNum > 3){
					e.printStackTrace();
					System.err.println("[SFTP FILE: Cannot downloaded SFTP file into local server]"+filePath);
					errorLinks.add(filePath);
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
	 * @param dirPath
	 * @param downloadToPath
	 * given a directory link, download all file in this directory into local server.
	 */
	public void downloadDirectory(String dirPath, String downloadToPath){
		
		try {
			String dirName = dirPath.substring(dirPath.lastIndexOf("/")+1, dirPath.length());
			if (downloadToPath.endsWith("/")){
				downloadToPath = downloadToPath + dirName+File.separator;
			}else{
				downloadToPath = downloadToPath + File.separator + dirName+File.separator;
			}
			File newDir = new File(downloadToPath);
			boolean created = newDir.mkdirs();
			
			dirPath = dirPath + File.separator;
			
			
			
			if (created){
				Vector<ChannelSftp.LsEntry> files = this.channelSftp.ls(dirPath);
				
				for (ChannelSftp.LsEntry file : files){
					String fileName = file.getFilename();
					String currentDirPath = dirPath + fileName;
					
					if (fileName.startsWith(".")){
						continue;
					}
					
					if (isDirectory(currentDirPath)){
						downloadDirectory(currentDirPath, downloadToPath);
					} else if (isFile(currentDirPath)){
						String newDownloadPath = downloadToPath+fileName;
						downloadFile(currentDirPath, newDownloadPath);
					}
				}			
			}
		} catch (SftpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	
	
	/**
	 * @param path
	 * @return 
	 * check if the path is a directory
	 * @throws SftpException 
	 */
	public Boolean isDirectory(String path) throws SftpException{
	    try{
	    	this.channelSftp.lcd(path);
	    	return true;
	    }catch(Exception e){
	    	return false;
	    }
	}
	

	
	/**
	 * @param path
	 * @return
	 * check if the path is a file
	 */
	public Boolean isFile(String path){
		try{
			channelSftp.stat(path);
			return true;
		}catch(Exception e){
			return false;
		}
	}
	
	
	/**
	 * @param dirPath
	 * @return directory name
	 * given the directory link, return the name of directory.
	 */
	public String getName(String dirPath){
		String dirName = dirPath.substring(dirPath.lastIndexOf("/")+1, dirPath.length());
		return dirName;
	}
	
	/**
	 * connect Session and Channel.
	 */
	public void connectSessionAndChannel(){
		
		
		Properties config = new Properties();
		config.put("StrictHostKeyChecking","no");
		session.setConfig(config);
		
		try {
			session.connect();
			channel  = session.openChannel("sftp");
			channel.connect();
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		channelSftp = (ChannelSftp) channel;
		
		
	}
	
	
	
	public ArrayList<String> getErrorLinks(){
		return this.errorLinks;
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
	
public static void main(String[] args) throws SocketException, IOException, JSchException, SftpException{
	}
	

}
