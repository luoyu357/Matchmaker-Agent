package driver;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;

/**
 * @author yuluo
 *
 */
public class FileManager {
	
	public FileManager(){}
	
	
	
	/**
	 * @param destDirectory
	 * @throws IOException
	 * extract file and save files to a directory.
	 */
	public void unZip(String destDirectory, String zipFilePath) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        } 
        
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();

        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractZipFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }
	
	
	
	/**
	 * @param zipIn
	 * @param filePath
	 * @throws IOException
	 * 
	 */
	private void extractZipFile(ZipInputStream zipIn, String filePath) throws IOException{
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[4096];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }
	
	
	
	/**
	 * @param downloadPath
	 * delete a temporary file after finishing depositing this file into repository.
	 */
	public void removeTempFile(String downloadPath){
		File temp = new File(downloadPath);
		try{
			if (temp.exists()){
				temp.delete();
				System.out.println("Temporary File Deleted");
			}
		}catch(Exception e){
			System.err.println(e);
		}
	}
	
	
	/**
	 * @param downloadPath
	 * delete a temporary directory after finishing depositing this directory into repository.
	 */
	public void removeTempFolder(String downloadPath){
		File temp = new File(downloadPath);
		try{
			if (temp.exists()){
				FileUtils.deleteDirectory(temp);
				System.out.println("Folder Deleted");
			}
		}catch(IOException e){
			System.err.println(e);
		}
	}

	
	
	
	
	
	
	public static void main(String[] args) throws IOException{
		
	}

}
