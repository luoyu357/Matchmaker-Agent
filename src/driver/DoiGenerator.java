package driver;

import org.seadva.services.EzidService;


public class DoiGenerator {
	
	private String target;
	private String metadata;

	
	public DoiGenerator(String target, String metadata){
		this.target = target;
		this.metadata = metadata;
	}
	
	
	public String getDoi(){
		EzidService ezidService = new EzidService();
		String doi_url = ezidService.createDOI(metadata, target);
		return doi_url;
	}
	
	
	public String updateDoi(String doi, String target){
		EzidService ezidService = new EzidService();
		String newdoi_url = ezidService.updateDOI(doi, target);
		return newdoi_url;
	}
	
	
	public static void main(String[] args){
		 
	}

}
