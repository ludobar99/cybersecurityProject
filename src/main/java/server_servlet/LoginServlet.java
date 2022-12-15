package server_servlet;

import jakarta.servlet.http.HttpServlet;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import org.apache.commons.text.StringEscapeUtils;

import client.User;
import database.DBAPI;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.Hash;
import util.SessionManager;
import util.Validator;

/**
 * Servlet implementation class HelloWorldServlet
 */
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
     	
	/**
     * @see HttpServlet#HttpServlet()
     */
    public LoginServlet() {
        super();
    }
    
    public void init() throws ServletException {

    	
   
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");

		// Extracting data
		String email = request.getParameter("email");
		String pwd = request.getParameter("password");
		
		/*
		 * validating email and password
		 */
		if (!Validator.validateEmail(email) | !Validator.validatePassword(pwd)) {
			System.out.println("invalid email or password");
			request.getRequestDispatcher("login.html").forward(request, response);
			return;
		} 
		
		/*
		 * sanitizing email and password
		 */
		email = StringEscapeUtils.escapeHtml4(email);
		pwd = StringEscapeUtils.escapeHtml4(pwd);
		
		try {

			/*
			 * getting user account
			 */
			User user = DBAPI.getAccount(email);

			/*
			 * no match was found for email address; user is null
			 */
			if (user == null) {
				System.out.println("Login failed!");
				request.getRequestDispatcher("login.html").forward(request, response);
				return;
			}
			
			/*
			 * checks the correctness of the password. It generates an hash from the password 
			 * and compares it with the hash in the database
			 * 
			 */
			if (!Hash.validatePassword(pwd, user.getPassword())) {
				System.out.println("Login failed!");
				request.getRequestDispatcher("login.html").forward(request, response);
				return;
			}
				
			/*
			 *  if login was successful, the session is associated with the user email
			 */
			System.out.println("Login succeeded!");

			SessionManager.setSessionUser(request.getSession(), user.getEmail());
			request.setAttribute("email", user.getEmail());
			request.setAttribute("content", "");
			request.getRequestDispatcher("home.jsp").forward(request, response);
			
		} catch (SQLException e) {
			e.printStackTrace();
			request.getRequestDispatcher("login.html").forward(request, response);}
		catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}