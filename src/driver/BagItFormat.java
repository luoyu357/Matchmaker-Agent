package driver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class BagItFormat {
	private String filePath;
	private String destDirectory;
	private ArrayList<String> errorLinks;
	
	public BagItFormat(String filePath){
		this.filePath = filePath;
		this.errorLinks = new ArrayList<String>();
		if (isZip()){
			FileManager manager = new FileManager();
			destDirectory = filePath.substring(0, filePath.lastIndexOf("/"));
			try{
				manager.unZip(destDirectory, filePath);
				catchData();
			}catch(Exception e){
				System.err.println("[FILE MANAGER: Cannot extract this file]"+filePath);		
			}
		}
	}
	
	

	
	public String getFilePath(){
		return this.filePath;
	}
	
	
	public void setFilePath(String newFilePath){
		this.filePath = newFilePath;
	}
	
	
	
	
	public Boolean isZip(){
		if (filePath.endsWith(".zip") || filePath.endsWith(".tar")){
			return true;
		}
		return false;
	}
	
	
	public void catchData() {
		File currentDir = new File(destDirectory);
		File[] files = currentDir.listFiles();
		for (File file : files){
			if (!file.getName().equals("data")){
				System.out.println(file.getPath());
				file.delete();
			} else{
				File[] inFiles = file.listFiles();
				for (File inFile : inFiles){
					try{
						File target = new File(this.destDirectory+file.separator+inFile.getName());
						inFile.renameTo(target);
					}catch(Exception e){
						e.printStackTrace();
					}
				}			
			}
			File deleteData = new File(this.destDirectory+file.separator+"data");
			deleteData.delete();
		}
	}
	
	
	
	
	public static void main(String[] args){
	}
	
	
	
}
