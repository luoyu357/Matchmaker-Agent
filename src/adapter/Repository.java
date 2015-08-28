package adapter;

import java.util.Properties;

import engine.PropertiesReader;

public class Repository {

	private String name;
	private String propertiesPath;
	private Properties properties;
	
	public Repository(String name, String propertiesPath){
		this.name = name;
		this.propertiesPath = propertiesPath;
	}
	
	
	public Repository(String name){
		this.name = name;
	}
	
	public String getName(){
		return this.name;
	}
	
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getRepository(){
		return this.name;
	}
	
	public Properties getpRroperties(){
		PropertiesReader propertiesReader = new PropertiesReader(this.propertiesPath);
		this.properties = propertiesReader.getProperties();
		
		return this.properties;
	}
	
}
