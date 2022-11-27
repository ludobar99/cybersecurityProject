package asymmetricEncryption;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/*
 * 
 * This class provides methods to get private and public keys' byte arrays from a file/database.
 * 
 */
public class KeyGetter {
	
	/*
	 * TODO: environment variables
	 */
	private static final String USER = "sa";
	private static final String PWD = "Strong.Pwd-123";
	private static final String DRIVER_CLASS = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=examDB;encrypt=true;trustServerCertificate=true;";
    
	private static Connection conn;
    
    public static void init() {
    	try {
    		Class.forName(DRIVER_CLASS);
   
		    Properties connectionProps = new Properties();
		    connectionProps.put("user", USER);
		    connectionProps.put("password", PWD);
	
	        conn = DriverManager.getConnection(DB_URL, connectionProps);
		        	
    	} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
    }
    
	
	/*
	 * Gets the public key corresponding to the email in the database. If the email
	 * doesn't correspond to any public key, returns null.
	 */
	public static byte[] getPublicKeyBytes(String email) throws SQLException {
		
		PreparedStatement publicKeyStatement;
		ResultSet resSet;
		byte[] publicKeyBytes = null;
			
		publicKeyStatement = conn.prepareStatement("SELECT publickey FROM [user] WHERE email=?");
		publicKeyStatement.setString(1, email);
		resSet = publicKeyStatement.executeQuery();
		
		while (resSet.next()) {
			
			publicKeyBytes = resSet.getBytes(1);
		}
		
		return publicKeyBytes;
		
	}
	
	/*
	 * Gets the key bytes saved in a file named after the user email.
	 */
	public static byte[] getPrivateKeyBytes(String rootPath, String email) throws IOException {
		String filePath = rootPath + "/keys/" + email;
		Path path = Paths.get(filePath);

		return Files.readAllBytes(path);
	}

}
