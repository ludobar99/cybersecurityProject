package server_servlet;

import jakarta.servlet.http.HttpServlet;
import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import util.SessionManager;

/**
 * Servlet implementation class HelloWorldServlet
 */
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
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
		
		/*
		 * Logs out and redirects to login.html. The session is invalidated
		 * It would be better to have another servlet for this.
		 * 
		 */
		if (request.getParameter("logout") != null) {
			
			request.getSession(false).invalidate();
			request.getRequestDispatcher("login.html").forward(request, response);
		
		} else {
		
		String email = request.getParameter("email");
		String pwd = request.getParameter("password");
		
		try {
			
			PreparedStatement statement = conn.prepareStatement("SELECT * FROM [user] WHERE email=? AND PASSWORD=?");
			
			statement.setString(1, email);
			statement.setString(2, pwd);
			
			ResultSet sqlRes = statement.executeQuery();
			
			if (sqlRes.next()) {
				
				// if login succeeded, the session is associated with a user
				SessionManager.setSessionUser(request.getSession(), sqlRes.getString(3));
				
				request.setAttribute("email", sqlRes.getString(3));
				request.setAttribute("password", sqlRes.getString(4));
				
				System.out.println("Login succeeded!");
				request.setAttribute("content", "");
				request.getRequestDispatcher("home.jsp").forward(request, response);
				
				
			} else {
				System.out.println("Login failed!");
				request.getRequestDispatcher("login.html").forward(request, response);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			request.getRequestDispatcher("login.html").forward(request, response);
		}
		}
	}
}