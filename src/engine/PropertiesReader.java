package engine;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesReader {
	
	private Properties properties;
	
	public PropertiesReader(String file){
		
		try{	
			properties = new Properties();
			FileInputStream fileInputStream = new FileInputStream(file);
			properties.load(fileInputStream);
			
		}catch(IOException e){
			System.err.println("Error: unable to load properties file" + file);
			e.printStackTrace();
			System.exit(-1);}
		
	}
		
	
	/**
	 * @return properties
	 */
	public Properties getProperties(){
		return properties;
	}
		
}
	
	
	

