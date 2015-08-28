package adapter;

public class DefaultRepository {
	
	public Repository getRepository(){
		
		return new Repository("SDA", "propertes file path");
	}

}
