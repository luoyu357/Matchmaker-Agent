package driver;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64;

/**
 * @author yuluo
 *
 */
public class HttpFile {	
	private String fileURL;
	private static HttpURLConnection httpConn;
	private InputStream inputStream;
	private String userpass;
	private ArrayList<String> errorLinks;
	
	
	public HttpFile(String fileURL){
		this.fileURL = fileURL;
		errorLinks = new ArrayList<String>();
	}
	
	public HttpFile(String fileURL, String userpass){
		this.userpass = userpass;
		this.fileURL = fileURL;
		errorLinks = new ArrayList<String>();
	}
	
	
	 
	
	
	/**
	 * connect to http server
	 */
	public void connection(){
		URL url;
		try {
			url = new URL(fileURL);
			httpConn = (HttpURLConnection) url.openConnection();
			String basicAuth = "Basic " + new String(new Base64().encode(this.userpass.getBytes()));
			httpConn.setRequestProperty ("Authorization", basicAuth);	
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
	
	
	/**
	 * @return 
	 * validate Http link if it is valid.
	 */
	public boolean validHttp(){
		int responseCode;
		boolean re = false;
		try {
			responseCode = httpConn.getResponseCode();
			if(responseCode == HttpURLConnection.HTTP_OK){
				re = true;
			}else{
				re = false;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			re = false;
			e.printStackTrace();
		}
		
		return re;
	}
	
	
	
	
	
	/**
	 * @param saveDir
	 * @throws IOException
	 * download the Http link file into a local directory.
	 */
	public void downloadFile(String saveDir) throws IOException{		
		
		if(validHttp()){
										            
			
			inputStream = httpConn.getInputStream();	
			String fileName = "";
            
			
			String savePath = saveDir;
			
			FileOutputStream outputStream = new FileOutputStream(savePath);
	            
			int bytesRead = -1;
			byte[] buffer = new byte[4096];
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
	 
			outputStream.close();
			inputStream.close();
	 
			System.out.println("[HTTP FILE: HTTP File downloaded into local server]"+fileURL);
			
			
		}else {
			System.out.println("[HTTP FILE: Invalid HTTP link]"+fileURL);
			errorLinks.add("[Invalid HTTP FILE] "+fileURL);
		}
		
	}
	
	
	public String getFileName(){
		String filename ="";
		String disposition = httpConn.getHeaderField("Content-Disposition");
		 
        if (disposition != null) {
            // extracts file name from header field
            
           filename = disposition.substring(disposition.lastIndexOf("=")+2,
                        disposition.length());
            
        } else {
            // extracts file name from URL
            filename = fileURL.substring(fileURL.lastIndexOf("/") + 1,
                    fileURL.length());
        }
        return filename;
	}
	
	
	

	
	
	/**
	 * disconnet with HTTP server
	 */
	public void disconnect(){
		httpConn.disconnect();
	}
	
	
	public ArrayList<String> getErrorLinks(){
		return this.errorLinks;
	}
	
	
	
	public static void main(String[] args){	
	}

}
