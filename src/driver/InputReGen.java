package driver;

import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class InputReGen {
	
	
	private JSONObject input;
	private static JSONObject output = new JSONObject();
	
	
	public InputReGen(JSONObject input){
		read(input);
	}
	
	
	public static void read(JSONObject obj){
		Set keys = obj.keySet();
		Object[] keyList = keys.toArray();
		
		for (Object key : keyList){
			if (key.toString().matches("^(operation|responseKey|contentUrl|uniqueID|ROID)")){
				output.put(key, obj.get(key));
			} else if (obj.get(key) instanceof JSONObject){
				read((JSONObject) obj.get(key));
			} else if (obj.get(key) instanceof JSONArray){
				JSONArray insideArray = (JSONArray) obj.get(key);
				for (int i = 0 ; i< insideArray.size(); i++){
					read((JSONObject) insideArray.get(i));
				}
			}
		}
 	}
	
	
	
	
	public JSONObject getReGenInput(){
		return this.output;
	}
	
	
	
	public static void  main(String[] args){
		
	}

}
