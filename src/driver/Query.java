package driver;

import java.util.UUID;

import org.json.simple.JSONObject;



public class Query {	
	
	private JSONObject object;
	
	public Query(JSONObject object){
		this.object = object;
	}
	
	/**
	 * @return response key
	 */
	public String getResponseKey(){
		
		String responseKey = null;
		responseKey = (String) object.get("responseKey");
		
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
	 * @return unique ID
	 */
	public String getUniqueId(){
		
		String uniqueID = null;
		uniqueID = (String) object.get("uniqueID");
		return uniqueID;
	}
	
	
	
	/**
	 * @param responseKey
	 * @param uniqueID
	 * @param operation
	 * @return a new JSON file which contains response key, unique ID and operation.
	 */
	public JSONObject createOutput(String responseKey, String uniqueID, String operation){
		
		JSONObject newObject = new JSONObject();
		newObject.put("responseKey", responseKey);
		newObject.put("uniqueID", uniqueID);
		newObject.put("operation", operation);
		
		
		return newObject;
	}
	
	
	
	
	
	public static void main(String[] args){
	}

}
