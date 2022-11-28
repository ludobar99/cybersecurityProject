package database;

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
		
		PreparedStatement statement = conn.prepareStatement("SELECT * FROM mail WHERE sender=? ORDER BY [time] DESC");
		
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
}
