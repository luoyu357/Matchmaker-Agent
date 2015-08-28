package driver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import engine.PropertiesReader;

/**
 * @author yuluo
 *
 */
public class ProtocolInterchange {
	

	private String propertiesPath;
	private String downloadPath;
	private List<String> downloadPathList;
	private String userpass;
	private ArrayList<String> errorLinks;
	

	public ProtocolInterchange(String propertiesPath, String downloadPath){
		this.downloadPath  = downloadPath;
		downloadPathList = null;
		this.propertiesPath = propertiesPath;
		PropertiesReader co = new PropertiesReader(this.propertiesPath);
		//collection username and password
		userpass = co.getProperties().getProperty("co.user")+":"+co.getProperties().getProperty("co.pass");
		errorLinks = new ArrayList<String>();
	}
	
	
	

	/**
	 * @param link
	 * @return downloadPath
	 * get download link and download linked object into local server.
	 * @throws IOException 
	 */
	public void verifyAndDownload(String link, String folder) throws Exception{
		System.out.println("1"+downloadPath);
		if (link.matches("^(http|https)://.*$")){
			
			HttpFile http = new HttpFile(link,userpass);
			http.connection();
			
			String fileName = http.getFileName();
	
			try {
				downloadPath = downloadPath + folder;
				createDirectory(downloadPath);
				http.downloadFile(downloadPath+File.separator+fileName);
				errorLinks.addAll(http.getErrorLinks());
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("[Interchange: Cannot download this HTTP File]" + link);
				errorLinks.add(link);
			}

			
			http.disconnect();			
			
		}else if (link.matches("^ftp:.*$")){
 
			link = link.substring(link.indexOf(":")+1, link.length());
			
			FtpFile ftp = new FtpFile(this.propertiesPath);
			ftp.connection();
			String fileName = ftp.getFileName(link);
			
			downloadPath = downloadPath + folder;
			if (ftp.isFile(link)){
				try {
					createDirectory(downloadPath);
					ftp.downloadFile(link, downloadPath+File.separator+fileName);
					errorLinks.addAll(ftp.getErrorLinks());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.err.println("[Interchange: Cannot download this FTP File]" + link);
					errorLinks.add(link);
				}
			}else if (ftp.isDirectory(link)){
				try {
					createDirectory(downloadPath);
					ftp.downloadDirectory(link, downloadPath);
					errorLinks.addAll(ftp.getErrorLinks());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.err.println("[Interchange: Cannot download this FTP Directory]" + link);
					errorLinks.add(link);
				}
			} else {
				System.err.println("[Interchange: Cannot verify FTP link]" + link);
				errorLinks.add(link);
			}
			
			
			ftp.disconnection();

		}else if (link.matches("^sftp:.*$")){
			link = link.substring(link.indexOf(":")+1, link.length());
			SftpFile sftp = new SftpFile(this.propertiesPath);
			sftp.connectSessionAndChannel();
			downloadPath = downloadPath + folder;
		
			if (sftp.isDirectory(link)){
				try{		
					createDirectory(downloadPath);
					sftp.downloadDirectory(link, downloadPath);
					errorLinks.addAll(sftp.getErrorLinks());
				}catch(Exception e){
					e.printStackTrace();
					System.err.println("[Interchange: Cannot download this SFTP Directory]" + link);
					errorLinks.add(link);
				}
			}else if (sftp.isFile(link)) {
				try{
					createDirectory(downloadPath);
					sftp.downloadFile(link, downloadPath);
					errorLinks.addAll(sftp.getErrorLinks());
				}catch(Exception e){
					e.printStackTrace();
					System.err.println("[Interchange: Cannot download this FTP File]" + link);
					errorLinks.add(link);
				}
			} else {
				System.err.println("[Interchange: Cannot verify this SFTP link]" + link);
				errorLinks.add(link);
			}
			
			sftp.disConnectSessionAndChannel();
			
			
		}else{
			System.err.println("[Interchange: Cannot verify this Link" + link);
			errorLinks.add(link);

		}
		
		System.out.println(downloadPath);
		if (errorLinks.isEmpty()){
			exBagIt(downloadPath);
		}
	}
	
	
	
	
	
	
	

	/**
	 * @param links
	 * @param folderName
	 * @return a list of download location
	 * given a list of links, create a directory in local server to save all these objects. 
	 */
	public void verifyAndDownload(List<String> links, String folderName){
		downloadPath = downloadPath + folderName;
		File temp = new File(downloadPath);
		temp.mkdirs();
		
		
		for (String link : links){
			if (link.matches("^(http|https)://.*$")){
				try{
					HttpFile http = new HttpFile(link,userpass);
					http.connection();
					String fileName = http.getFileName();
					String downloadItemPath = downloadPath + File.separator + fileName;
					http.downloadFile(downloadItemPath);
					errorLinks.addAll(http.getErrorLinks());
					http.disconnect();
				}catch(Exception e){		
					e.printStackTrace();
					System.err.println("[Interchange: Cannot download this HTTP File]" + link);
					errorLinks.add(link);
				}
			}else if (link.matches("^ftp://.*$")){
				try{
					FtpFile ftp = new FtpFile(this.propertiesPath);
					ftp.connection();
					String fileName = ftp.getFileName(link);
					String downloadItemPath = downloadPath + File.separator + fileName;
					ftp.downloadFile(link, downloadItemPath);
					errorLinks.addAll(ftp.getErrorLinks());
					ftp.disconnection();	
				}catch(Exception e){
					e.printStackTrace();
					System.err.println("[Interchange: Cannot download this FTP File]" + link);
					errorLinks.add(link);
				}
			}else if (link.matches("^sftp://.*$")){
				try{
					SftpFile sftp = new SftpFile(this.propertiesPath);
					sftp.connectSessionAndChannel();
					String fileName = sftp.getName(link);
					String downlaodItemPath = downloadPath + File.separator+fileName;
					sftp.downloadFile(link, downlaodItemPath);
					errorLinks.addAll(sftp.getErrorLinks());
					sftp.disConnectSessionAndChannel();
				}catch(Exception e){
					e.printStackTrace();
					System.err.println("[Interchange: Cannot download this FTP File]" + link);
					errorLinks.add(link);
				}
			}else{
				System.err.println("[Interchange: Cannot verify this link]" + link);
				errorLinks.add(link);
			}
		}
		
	}
	
	

	/**
	 * @param link
	 * @return
	 * get temporary directory name
	 */
	public String getName(String link){
		String dirName = link.substring(link.lastIndexOf("/")+1, link.length());
		return dirName; 
	}
	
	
	
	public ArrayList<String> getErrorLinks(){
		return this.errorLinks;
	}
	
	
	
	
	public void createDirectory(String path){
		File newDir = new File(path);
		newDir.mkdir();
		
	}
	
	
	public String getDownlaodPath(){
		return this.downloadPath;
	}
	
	
	public void exBagIt(String downloadPath){
		File current = new File(downloadPath);
		File[] files = current.listFiles();
		if (files.length == 1 && files[0].getName().endsWith(".zip")){
			
			BagItFormat bagit = new BagItFormat(files[0].getPath());
		}
	}
	
	
	
	public static void main(String[] args) throws IOException {	

	}


}
