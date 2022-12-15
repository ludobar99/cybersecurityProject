package database;

import java.security.PublicKey;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import client.User;
import mail.EMail;

public class DBAPI {
	
	//public static Connection conn = DBConnection.getInstance().getConn();
	
	/*
	 *  Gets user account. If no account corresponds to the email, returns null.
	 */
	public static User getAccount(Connection conn, String email) throws SQLException {
				
		User user;
		
		PreparedStatement statement = conn.prepareStatement("SELECT * FROM [user] WHERE email=?");
		
		statement.setString(1, email);
		
		ResultSet sqlRes = statement.executeQuery();
		
		if (sqlRes.next()) {
			
			user = new User(sqlRes.getString(3), sqlRes.getString(4));
			return user;
		
		}
		
		return null;
	
	}

	/*
	 *  Gets user inbox.
	 */
	public static ArrayList<EMail> getInbox(Connection conn, String receiverEmail) throws SQLException {
		
		PreparedStatement statement = conn.prepareStatement("SELECT * FROM mail WHERE receiver=? ORDER BY [time] DESC");
		
		statement.setString(1, receiverEmail);
		
		ResultSet sqlRes = statement.executeQuery();
		
		ArrayList<EMail> inbox = new ArrayList<EMail>();
		
		while (sqlRes.next()) {
			
			EMail email = new EMail(sqlRes.getString(1), receiverEmail, sqlRes.getBytes(3), sqlRes.getBytes(4), sqlRes.getBytes(5), sqlRes.getString(6));
			
			inbox.add(email);
		
		}
		return inbox;
		
	}
	
	/*
	 *  Gets user sent emails.
	 */
	public static ArrayList<EMail> getSentEmails(Connection conn, String senderEmail) throws SQLException {
		
		PreparedStatement statement = conn.prepareStatement("SELECT * FROM mail WHERE sender=? ORDER BY [time] DESC");
		
		statement.setString(1, senderEmail);
		
		ResultSet sqlRes = statement.executeQuery();
		
		ArrayList<EMail> inbox = new ArrayList<EMail>();
		
		while (sqlRes.next()) {
			
			EMail email = new EMail(senderEmail, sqlRes.getString(1), sqlRes.getBytes(3), sqlRes.getBytes(4), sqlRes.getBytes(5), sqlRes.getString(6));
			
			inbox.add(email);
		
		}
		return inbox;
		
	}

	/*
	 * Sending email aka saving it in the database.
	 */
	public static void sendEmail(Connection conn, String sender, String receiver, byte[] encryptedSubject, byte[] encryptedBody, byte[] encryptedDigest, String timestamp) throws SQLException {
		
		PreparedStatement statement = conn.prepareStatement("INSERT INTO mail ( sender, receiver, subject, body, digitalSignature, [time] ) VALUES ( ?, ?, ?, ?, ?, ?)");
		
		statement.setString(1, sender);
		statement.setString(2, receiver);
		statement.setBytes(3, encryptedSubject);
		statement.setBytes(4, encryptedBody);
		statement.setBytes(5, encryptedDigest);
		statement.setString(6, timestamp);
		
		
		statement.execute();
		
	}
	
	/*
	 * Checks if the user exists
	 */
	public static boolean checkIfUserExists(Connection conn, String email) throws SQLException {
		
		PreparedStatement statement = conn.prepareStatement("SELECT * FROM [user] WHERE email=?");
		statement.setString(1, email);
		ResultSet sqlRes = statement.executeQuery();
		
		/*
		 * Only one account per email can be created
		 * TODO: unique_key / primary_key database
		 */
		if (sqlRes.next()) {
			return true;
		} 
			
		return false;

		
	}

	/*
	 * Registers new user. Saves data in the database
	 */
	public static void registerUser(Connection conn, String name, String surname, String email, String password, String publicKey) throws SQLException {
		PreparedStatement statement2 = conn.prepareStatement(
				"INSERT INTO [user] ( name, surname, email, password, publickey ) VALUES (?,?,?,?,?)"
		);
		statement2.setString(1, name);
		statement2.setString(2, surname);
		statement2.setString(3, email);
		statement2.setString(4, password);	
		statement2.setString(5, publicKey);
		statement2.execute();

		
	}
	
	/*
	 * Gets user's public key.
	 */
	
	public static byte[] getPublicKeys(Connection conn) throws SQLException {
		PreparedStatement publicKeyStatement;
		ResultSet resSet;
		byte[] publicKeyBytes = null;
			
		publicKeyStatement = conn.prepareStatement("SELECT email, publickey FROM [user]");
		resSet = publicKeyStatement.executeQuery();


		while (resSet.next()) {
			publicKeyBytes = resSet.getBytes(2);
		}
		
		return publicKeyBytes;
	}

	public static byte[] getPublicKey(Connection conn, String email) throws SQLException {
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
	
}
	
	

