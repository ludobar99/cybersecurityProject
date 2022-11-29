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

import database.DBAPI;

/*
 * 
 * This class provides methods to get private and public keys' byte arrays from a file/database.
 * 
 */
public class KeyGetter {
	
	/*
	 * Gets the public key corresponding to the email in the database. If the email
	 * doesn't correspond to any public key, returns null.
	 */
	public static byte[] getPublicKeyBytes(Connection conn, String email) throws SQLException {
		
		byte[] publicKeyBytes = DBAPI.getPublicKey(conn, email);
		
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
