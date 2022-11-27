package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import client.User;

public class DBAPI {
	
	public static Connection conn = DBConnection.getInstance().getConn();
	
	/*
	 *  Gets user account. If no account corresponds to the email, returns null.
	 */
	public static User getAccount(String email) throws SQLException {
		
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

}
