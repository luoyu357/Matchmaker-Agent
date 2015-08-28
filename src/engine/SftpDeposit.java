package engine;

import engine.*;

import java.io.File;
import java.util.Vector;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

public class SftpDeposit{
	
	private String propertiesPath;
	private String objectPath;
	private Sftp sftp;
	private Boolean SDAex;


	
	public SftpDeposit(String propertiesPath, String objectPath) throws JSchException{
		this.propertiesPath = propertiesPath;
		this.objectPath = objectPath;
		sftp = new Sftp(this.propertiesPath);
		sftp.connectSessionAndChannel();
		this.SDAex = false;
	}
	
	public SftpDeposit(String propertiesPath) throws JSchException{
		this.propertiesPath = propertiesPath;
		sftp = new Sftp(this.propertiesPath);
		sftp.connectSessionAndChannel();
		this.SDAex = false;

	}
	
	
	
	/**
	 * @param objectPath
	 * @param source
	 * create a new directory on that source path in repository and deposit the object in it.
	 * @throws SftpException 
	 */
	public void depositSingleFile(String objectPath, String source) throws SftpException{
		String dirName = objectPath.substring(objectPath.lastIndexOf("/")+1, objectPath.lastIndexOf("."));
		sftp.cdDir(source);
		if (!dirIsExisted(objectPath, source)){
			sftp.createDirectory(dirName);
			String newSource = source + File.separator + dirName;	
			
			try{
				sftp.depositFile(objectPath, newSource);
			}catch(Exception e){
				e.printStackTrace();
			}
			sftp.disConnectSessionAndChannel();
			
			
		}else{
			System.out.println("Directory is Existed!");
		}
	}
	
	
	/**
	 * @param objectPath
	 * @param source
	 * create a same name directory on that source path in repository and deposit the object in it.
	 * @throws SftpException 
	 */
	public void depositDirectory(String objectPath, String source) throws SftpException{
		
		if (!dirIsExisted(objectPath, source)){//!dirIsExisted(objectPath, source)
			sftp.cdDir(source);
		
			String dirName = objectPath.substring(objectPath.lastIndexOf("/")+1, objectPath.length());
			sftp.createDirectory(dirName);
		
			String newSource = source + dirName + File.separator;
			sftp.cdDir(newSource);
		
			objectPath = objectPath +File.separator;
		
			File[] files = new File(objectPath).listFiles();
			for (File file : files){
				String filePath = objectPath + file.getName();
				if (isDirectory(filePath)){		
					depositDirectory(filePath, newSource);
					
				} else {
					sftp.cdDir(newSource);
					sftp.depositFile(filePath, newSource);
				}
			}
		}else {
			String dirName = null;
			if (objectPath.contains(".")){
				dirName = objectPath.substring(objectPath.lastIndexOf("/")+1, objectPath.lastIndexOf("."));
			} else {
				dirName = objectPath.substring(objectPath.lastIndexOf("/")+1, objectPath.length());
			}
			System.out.println("[SDA: The folder is Already Existed!]"+dirName);
			this.SDAex = true;
		}
	}
	
	
	
	public Boolean getSDAex(){
		return this.SDAex;
	}

	
	/**
	 * @param path
	 * @return
	 * check if the object is a directory
	 */
	public Boolean isDirectory(String path){
		Boolean re = false;
		if (new File(path).isDirectory()){
			re = true;
		}
		
		return re;
	}
	
	
	/**
	 * @param objectPath
	 * @param source
	 * @return
	 * check if the directory is existed
	 * @throws SftpException 
	 */
	public Boolean dirIsExisted(String objectPath, String source) throws SftpException{
		String dirName = null;
		if (objectPath.contains(".")){
			dirName = objectPath.substring(objectPath.lastIndexOf("/")+1, objectPath.lastIndexOf("."));
		} else {
			dirName = objectPath.substring(objectPath.lastIndexOf("/")+1, objectPath.length());
		}
		Vector<ChannelSftp.LsEntry> files = sftp.channelSftp.ls(source);
		Boolean exist = false;
		for (ChannelSftp.LsEntry file : files){
			if (file.getFilename().equals(dirName)){
				exist = true;
			}
		}
		return exist;
		
	}
	
	
	/**
	 * @param objectPath
	 * @param source
	 * @return
	 * check if the file is existed
	 * @throws SftpException 
	 */
	public Boolean fileIsExisted(String objectPath, String source) throws SftpException{
		String fileName = null;
		if (objectPath.contains(".")){
			fileName = objectPath.substring(objectPath.lastIndexOf("/")+1, objectPath.length());
		}
		Vector files = sftp.channelSftp.ls(source);
		Boolean exist = false;
		for (int i = 0 ; i < files.size(); i++){
			if (files.get(i).toString().equals(fileName)){
				exist = true;
			}
		}
		return exist;
		
	}
	
	
	
	
	
	
	/**
	 * finish depositing and disconnect Session and Channel.
	 */
	public void depositDone(){
		sftp.disConnectSessionAndChannel();
	}


public static void main(String[] args) throws JSchException, SftpException{

	}
}
