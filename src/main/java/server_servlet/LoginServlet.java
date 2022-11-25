package server_servlet;

import jakarta.servlet.http.HttpServlet;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.text.StringEscapeUtils;
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
    
	/*
	 * TODO: environment variables
	 */
	private static final String USER = "sa";
	private static final String PWD = "Strong.Pwd-123";
	private static final String DRIVER_CLASS = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=examDB;encrypt=true;trustServerCertificate=true;";
    
	private static Connection conn;
	
	/**
     * @see HttpServlet#HttpServlet()
     */
    public LoginServlet() {
        super();
    }
    
    public void init() throws ServletException {
    	try {
			Class.forName(DRIVER_CLASS);
			
		    Properties connectionProps = new Properties();
		    connectionProps.put("user", USER);
		    connectionProps.put("password", PWD);
	
	        conn = DriverManager.getConnection(DB_URL, connectionProps);
		    
    	
    	} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		
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

			PreparedStatement statement = conn.prepareStatement("SELECT * FROM [user] WHERE email=?");
			
			statement.setString(1, email);
			
			ResultSet sqlRes = statement.executeQuery();
			
			if (sqlRes.next()) {
				
				String _email = sqlRes.getString(3);
				String _password = sqlRes.getString(4);
				
				/*
				 * checks the correctness of the password. It generates an hash from the password 
				 * and compares it with the hash in the database
				 * 
				 */
				if (!Hash.validatePassword(pwd, _password)) {
					
					System.out.println("Login failed!");
					request.getRequestDispatcher("login.html").forward(request, response);
				
					return;
				}
				
				/*
				 *  if login was successful, the session is associated with the user email
				 */
				SessionManager.setSessionUser(request.getSession(), _email);
				
				request.setAttribute("email", _email);
				
				System.out.println("Login succeeded!");
				request.setAttribute("content", "");
				request.getRequestDispatcher("home.jsp").forward(request, response);
				
				
			} else {
				System.out.println("Login failed!");
				request.getRequestDispatcher("login.html").forward(request, response);
			}
			
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