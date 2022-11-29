package database;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import io.github.cdimascio.dotenv.Dotenv;
import util.Paths;

public class DBConnection {
	 
	private static DBConnection connInstance;
	public static Connection conn;
	
	/*
	 * Connecting with the database.
	 * The method uses the variables specified in the .env file.
	 * TODO: soluzione per la path brutta, da rivedere
	 */
	private DBConnection() {
		 
		/*
		 * TODO: fare file di caricament . env in paths get root etc?
		 */
//		String currentPath = "non la ha presa";
//		try {
//			currentPath = new java.io.File(".").getCanonicalPath();
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		 System.out.println("Current dir:" + currentPath);
//		 
//		 String currentDir = System.getProperty("user.dir");
//		 System.out.println("Current dir using System:" + currentDir);
//		
//		
		 Dotenv dotenv = null;
	   //  dotenv = Dotenv.configure().directory("/Users/ludo/Downloads/cybersecurityProject-master/").load();
		 dotenv = Dotenv.configure().load();
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
			
			//DBConnection.getInstance();
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