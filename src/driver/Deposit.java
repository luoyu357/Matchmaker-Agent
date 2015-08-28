package driver;

import java.util.UUID;

import org.json.simple.JSONObject;



public class Deposit {
	
	private static JSONObject object;
	
	
	public Deposit(JSONObject object){
		this.object = object;
	}
	
	
	/**
	 * @return response key
	 */
	public String getResponseKey(){
		
		String responseKey = null;
		responseKey =(String) object.get("responseKey");
		
		return responseKey;
	}
	
	
	
	
	/**
	 * @return operation
	 */
	public String getOperation(){
		
		String operation = null;
		operation = (String) object.get("operation");
		
		return operation;
	}
	
	
	
	
	/**
	 * @return target directory
	 */
	public String getTargetDirectory(){
		
		String directory = null;
		//JSONObject message = (JSONObject) object.get("message");
		directory = (String) object.get("contentUrl");
		
		return directory;
	}
	
	
	
	/**
	 * @return unique ID
	 */
	public String getUniqueId(){
		
		String uniqueID = null;
		uniqueID = (String) object.get("uniqueID");
		//uniqueID = UUID.randomUUID().toString();
		return uniqueID;
	}
	
	
	
	/**
	 * @param responseKey
	 * @param uniqueID
	 * @param operation
	 * @param targetDirectory
	 * @return a new JSON file which contains response key, unique ID, operation and target directory
	 */
	public JSONObject createOutput(String responseKey, String uniqueID, String operation,  String targetDirectory){
		
		JSONObject newObject = new JSONObject();
		newObject.put("responseKey", responseKey);
		newObject.put("uniqueID", uniqueID);
		newObject.put("operation", operation);	
		newObject.put("targetDirectory", targetDirectory);
		
		return newObject;
	}
	
	
	
	
	
	public static void main(String[] args){
	}

}
