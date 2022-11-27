package database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import io.github.cdimascio.dotenv.Dotenv;

public class DBConnection {
	 
	private static DBConnection connInstance;
	public static Connection conn;
	
	/*
	 * Connecting with the database.
	 * The method uses the variables specified in the .env file.
	 */
	private DBConnection() {
		 
		/*
		 * TODO: remove absolute path
		 */
		 Dotenv dotenv = null;
	     dotenv = Dotenv.configure().directory("/Users/ludo/Downloads/cybersecurityProject-master").load();
	     
	    	
		 try {
				Class.forName(dotenv.get("DRIVER_CLASS"));
				
			    Properties connectionProps = new Properties();
			    connectionProps.put("user", dotenv.get("DB_USER"));
			    connectionProps.put("password", dotenv.get("SA_PASSWORD"));
		
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
			try {
				String path = new File(".").getCanonicalPath();
				System.out.println(path);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			DBConnection.getInstance();
			Dotenv dotenv = null;
		     dotenv = Dotenv.load();
		     
		    	
			 try {
					Class.forName(dotenv.get("DRIVER_CLASS"));
					
				    Properties connectionProps = new Properties();
				    connectionProps.put("user", dotenv.get("DB_USER"));
				    connectionProps.put("password", dotenv.get("SA_PASSWORD"));
			
			        conn = DriverManager.getConnection(dotenv.get("DB_URL"), connectionProps);
				    System.out.println("CONN "+conn);
		    	
		    	} catch (ClassNotFoundException | SQLException e) {
					e.printStackTrace();
				}
		}
}