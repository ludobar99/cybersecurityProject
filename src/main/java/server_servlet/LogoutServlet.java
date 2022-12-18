package server_servlet;

import jakarta.servlet.http.HttpServlet;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import util.SessionManager;

/**
 * Servlet implementation class LogoutServlet
 */
@WebServlet("/LogoutServlet")
public class LogoutServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LogoutServlet() {
        super();
    }
    
    
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// Session check
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.sendRedirect("login.html");
			return;
		}

		// CSRF Check
		String sessionCSRFToken = null;
		try {
			sessionCSRFToken = SessionManager.getCSRFToken(session);
			String requestCSRFToken = request.getParameter("csrfToken");
			System.out.println(sessionCSRFToken + " " + requestCSRFToken);
			if (!sessionCSRFToken.equals(requestCSRFToken)) throw new Exception("CSRF Tokens do not match!");
		} catch (Exception error) {
			response.sendError(403, "CSRF Token error");
			return;
		}
		
		/*
		 * invalidates user session and returns to login page
		 */
		try {
			request.getSession(false).invalidate();
			request.getRequestDispatcher("login.html").forward(request, response);
		}	catch (NullPointerException e) {
			request.getRequestDispatcher("login.html").forward(request, response);
		}

	}

}
