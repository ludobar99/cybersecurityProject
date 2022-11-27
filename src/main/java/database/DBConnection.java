package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import io.github.cdimascio.dotenv.Dotenv;

public class DBConnection {
	
	
	
	/*
	 * TODO: initialize the object when loading the class?
	 */
	// public instance initialized when loading the class
	//private static DBConnection connInstance = new DBConnection();
	 
	private static DBConnection connInstance;
	public static Connection conn;
	
	/*
	 * Connecting with the database.
	 * The method uses the variables specified in the .env file.
	 */
	private DBConnection() {
		
		 Dotenv dotenv = null;
	     dotenv = Dotenv.configure().load();
	    	
		 try {
				Class.forName(dotenv.get("DRIVER_CLASS"));
				
			    Properties connectionProps = new Properties();
			    connectionProps.put("user", dotenv.get("DB_USER"));
			    connectionProps.put("password", dotenv.get("PWD"));
		
		        conn = DriverManager.getConnection(dotenv.get("DB_URL"), connectionProps);
			    
	    	
	    	} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			}
	}
	 
	/*
	 * Applying singleton pattern.
	 */
	public static DBConnection getInstance() {
		  	
		  if (connInstance == null) {
			  connInstance = new DBConnection();
		  }
	   
		  return connInstance;
	}
	
	/*
	 * Returns the connection object.
	 */
	public static Connection getConn() {
	       
		  return conn;
	}
	
	 

		public static void main(String[] args) {
			
			DBConnection.getInstance();
			Dotenv dotenv = null;
		     dotenv = Dotenv.load();
		     
		    	
			 try {
					Class.forName(dotenv.get("DRIVER_CLASS"));
					
				    Properties connectionProps = new Properties();
				    connectionProps.put("user", dotenv.get("DB_USER"));
				    connectionProps.put("password", dotenv.get("PWD"));
			
			        conn = DriverManager.getConnection(dotenv.get("DB_URL"), connectionProps);
				    System.out.println("CONN "+conn);
		    	
		    	} catch (ClassNotFoundException | SQLException e) {
					e.printStackTrace();
				}
		}
}