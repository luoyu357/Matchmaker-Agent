package adapter;

public class AdapterTest {
	
	private static Repository getRepository(RepositoryAdapter repoAdapter, String name){
		switch(name){
		case "SDA": return repoAdapter.getSDA();
		case "Dspace": return repoAdapter.getDspace();
		case "S3": return repoAdapter.getS3();
		default: return repoAdapter.getSDA();
		}
	}
	
	public static void main(String[] args){
		
		RepositoryAdapter ra = new RepositoryAdapterImpl();
		
		Repository SDA = getRepository(ra, "SDA");
		Repository Dspace = getRepository(ra, "Dspace");
		Repository S3 = getRepository(ra, "S3");
		
		
		System.out.println(SDA.getName());
		System.out.println(Dspace.getName());
		System.out.println(S3.getName());
		System.out.println(SDA.getName());
		
	}
}
