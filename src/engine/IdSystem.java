package engine;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;




/**
 * @author yuluo
 *
 */
public class IdSystem {
	
	static String url;
	static String dbname;
	static String driver;
	static String username;
	static String password;
	
	static Connection conn;
	private static PreparedStatement preparedStat;
	private static ResultSet result;		
	
	public IdSystem(String propertyPath){
		 Properties properites = new PropertiesReader(propertyPath).getProperties();
		    
		 url = properites.getProperty("url");
		 dbname = properites.getProperty("dbname");
		 driver = properites.getProperty("driver");
		 username = properites.getProperty("username");
		 password = properites.getProperty("password");
	}
	
	
	/**
	 * connect to MySQL server
	 */
	public void connectServer(){
		
		try {
			Class.forName(driver).newInstance();
			conn = DriverManager.getConnection(url+dbname,username,password);
		} catch (Exception e){
			e.printStackTrace();
		}	
	}
	
	
	
	/**
	 * @param id
	 * @param status
	 * @param created
	 * @param other
	 * create a record in MySQL database.
	 */
	public void insert(String id, String status, Date created, String other){
		String query = "insert into IDsystem values (?,?,?,?)";
		try {
			preparedStat = conn.prepareStatement(query);
			preparedStat.setString(1, id);
			preparedStat.setString(2, status);
			preparedStat.setDate(3, created);
			preparedStat.setString(4, other);
			preparedStat.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * @param id
	 * @param status
	 * @param other
	 * update a record in MySQL database
	 */
	public void update(String id, String status, String other){
		
		String query = "update IDsystem set status = ?, other = ? where ID = ?";
		try {
			preparedStat = conn.prepareStatement(query);
			preparedStat.setString(1, status);
			preparedStat.setString(2, other);
			preparedStat.setString(3, id);
			preparedStat.executeUpdate();
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * @param id
	 * @return query result
	 * query a ID in MySQL database
	 */
	public String query(String id){
		String exist = "Not exist";
		int count = 0;
		
		try {
			String query = "select count(*) from IDsystem where id = ?";
			preparedStat = conn.prepareStatement(query);
			preparedStat.setString(1, id);
			result = preparedStat.executeQuery();
			if (result.next()){
				count = result.getInt(1);
			}
			
			if (count == 1){
				
				query = "select * from IDsystem where id = ?";
				
				preparedStat = conn.prepareStatement(query);
				preparedStat.setString(1, id);
				result = preparedStat.executeQuery();
				
				if (result.next()){
					exist = result.getString("status");
				}
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return exist;
		
	}
	
	
	
	/**
	 * disconnect to MySQL server
	 */
	public void disconnetServer(){
		try{
			conn.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	
	public static void main(String[] args) throws SQLException{
		}
	
}
