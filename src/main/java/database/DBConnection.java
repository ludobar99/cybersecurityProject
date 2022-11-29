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
		 * checks if .env file exists. If not, generateEnv script must run with right path
		 * 1. copy the path printed from the program
		 * 2. update your generateEnv file with the right path
		 * 3. run the script in your teminal
		 */
		 String currentDir = System.getProperty("user.dir");
		 File f = new File(currentDir + "/.env");
		 
		 if(f.exists() && !f.isDirectory()) { 
		     System.out.println(".env file found");
		 } else {
			 System.out.println(".env file not foun in " + currentDir);
			 System.out.println("Run generateEnv.sh/generateEnv.bat and make sure that the path in the script is: " + currentDir);
		 }
		
		 Dotenv dotenv = null;
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
		  
		  DBConnection.getInstance();
		  return conn;
	
	}
}