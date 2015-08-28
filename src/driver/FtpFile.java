package driver;

import java.util.List;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import engine.PropertiesReader;

/**
 * @author yuluo
 *
 */
public class FtpFile {

	private FTPClient ftp = null;
	private Properties property;
	private String host;
	private String username;
	private String password;
	private boolean login;
	private String currentDir;
	private ArrayList<String> errorLinks;

	
	
	
	public FtpFile(String propertiesPath) throws IOException{
		PropertiesReader propery = new PropertiesReader(propertiesPath);
		this.property = propery.getProperties();
		this.host = property.getProperty("ftp.host");
		this.username = property.getProperty("ftp.username");
		this.password = property.getProperty("ftp.password");
		this.errorLinks = new ArrayList<String>();
		ftp = new FTPClient();
		
		
	}
	
	
	
	
	
	/**
	 * connect to ftp server.
	 */
	public void connection(){
		if (ftp.isConnected()){
			
			try {
				ftp.disconnect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			ftp.connect(host);
			login = ftp.login(username, password);
			try{
				ftp.setFileType(FTP.BINARY_FILE_TYPE);
				System.out.println("work");
			}catch(Exception e){
				System.out.println("desn't work");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	/**
	 * @param filePath
	 * @param downloadPath
	 * @throws IOException
	 * given a download link, download this file into local directory.
	 */
	public void downloadFile(String filePath, String downloadToPath) throws IOException{
		
		if (login){
			
			String name = getFileName(filePath);
			
			OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadToPath));
			
			boolean success = ftp.retrieveFile(filePath, outputStream);
			outputStream.close();
		
			if (success){
				System.out.println("[FTP FILE: FTP File downloaded into local server]"+filePath);
			}else{
				System.out.println("[FTP FILE: Cannot downloaded this FTP file into local server]"+filePath);
				errorLinks.add(filePath);
			}
			
            
		}else{
			System.out.println("[FTP FILE: Cannot log in the remote FTP server]");
			errorLinks.add(filePath);
		}
	}
	
	
	
	
	/**
	 * @param fileList
	 * @param filePath
	 * @param downloadPath
	 * @throws IOException
	 * given a list of download links, download them into a local directory.
	 */
	public void downloadFiles(List<String> fileList, String downloadToPath) throws IOException{
		if (login){
			for (String file : fileList){	
				String name = getFileName(file);
				
				OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadToPath));
				
				boolean success = ftp.retrieveFile(file, outputStream);
				outputStream.close();
				
				if (success){
					System.out.println("[FTP FILE: FTP File downloaded into local server]"+file);
				}else{
					System.out.println("[FTP FILE: Cannot downloaded this FTP file into local server]"+file);
					errorLinks.add(file);
				}			
			}
		}else{
			System.out.println("[FTP FILE: Cannot log in the remote FTP server]");
			errorLinks.addAll(fileList);
		}
	}
	
	
	
	
	/**
	 * @param dirPath
	 * @param downloadToPath
	 * given a download link of a directory, download all files in this directory into a local download directory.
	 */
	public void downloadDirectory(String dirPath, String downloadToPath){
		if (login){
			String dirName = dirPath.substring(dirPath.lastIndexOf("/")+1, dirPath.length());
			
			try {			
				if (downloadToPath.endsWith("/")){
					downloadToPath = downloadToPath + dirName + File.separator;
				}else{
					downloadToPath = downloadToPath +File.separator+ dirName + File.separator;
				}
				
				File newDir = new File(downloadToPath);
				
				newDir.mkdirs();
	                
				dirPath = dirPath + File.separator;
				ftp.changeWorkingDirectory(dirPath);
	       
				
				String[] files = ftp.listNames();
				for (String file : files){
						
					String currentDirPath = dirPath+file;
	                		
					if (file.startsWith(".")){
						continue;
					}
					if (isDirectory(currentDirPath)){
						downloadDirectory(currentDirPath, downloadToPath);
					} else {
						String newDownloadPath = downloadToPath + file;
						downloadFile(currentDirPath, newDownloadPath);
					}
                		
				}			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * @param path
	 * @return 
	 * check if the path is a directory
	 * @throws IOException 
	 */
	public Boolean isDirectory(String path) throws IOException{
		ftp.changeWorkingDirectory(path);
	    int returnCode = ftp.getReplyCode();
	    if (returnCode == 550) {
	        return false;
	    }
	    return true;
	}
	

	
	/**
	 * @param path
	 * @return
	 * check if the path is a file
	 * @throws IOException 
	 */
	public Boolean isFile(String path) throws IOException{
		InputStream inputStream = ftp.retrieveFileStream(path);
	    int returnCode = ftp.getReplyCode();
	    if (inputStream == null || returnCode == 550) {
	        return false;
	    }
	    return true;
	}
	
	/**
	 * @param path
	 * @return
	 * get file name;
	 */
	public String getFileName(String path){
		String name = path.substring(path.lastIndexOf("/")+1, path.length());	
		return name;
	}
	
	
	public ArrayList<String> getErrorLinks(){
		return this.errorLinks;
	}
	
	public String getCurrentDir() {
		return currentDir;
	}


	public void setCurrentDir(String currentDir) {
		this.currentDir = currentDir;
	}
	
	
	
	
	
	/**
	 * disconnect with FTP server.
	 */
	public void disconnection(){
		if (ftp.isConnected()){	
			try {
				this.ftp.logout();
				this.ftp.disconnect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	

	
	
	
	

	
	public static void main(String[] args) throws SocketException, IOException{	
	}











	
}
