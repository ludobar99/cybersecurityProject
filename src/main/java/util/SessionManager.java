package util;

import jakarta.servlet.http.HttpSession;

/*
 * Session information will be used to check if the session is valid. Furthermore, it will be checked
 * that the user email corresponds to session user email.
 * 
 */
public class SessionManager {
	
	/*
	 * Sets session user. ToCHECK: if session is not valid, user should be null.
	 */
	public static void setSessionUser(HttpSession session, String email) {
		session.setAttribute("user", email);
	
	}
	
	
	/*
	 * Sets session user. ToCHECK: if session is not valid, user should be null.
	 */
	public static String getSessionUser(HttpSession session) {

		try {
			
			if (session.getAttribute("user") == null) {
			
				return "invalid";
			}
			
		} catch (NullPointerException e) {System.out.println("user is null");}
		
		
		return (String) session.getAttribute("user");
	}


}
