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

	public static void setCSRFToken(HttpSession session, String token) {
		session.setAttribute("csrfToken", token);
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

	public static String getCSRFToken(HttpSession session) throws Exception {
		if (session.getAttribute("csrfToken") == null) throw new Exception("Missing token");
		return (String) session.getAttribute("csrfToken");
	}
}
