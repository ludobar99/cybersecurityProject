package server_servlet;

import jakarta.servlet.http.HttpServlet;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.text.StringEscapeUtils;

import client.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.Hash;
import util.Validator;

/**
 * Servlet implementation class RegisterServlet
 */
@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {
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
    public RegisterServlet() {
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
		 * The replacement escapes apostrophe special character in order to store it in SQL.
		 */
		String name = request.getParameter("name").replace("'", "''");
		String surname = request.getParameter("surname").replace("'", "''");
		String email = request.getParameter("email").replace("'", "''");
		String pwd = request.getParameter("password").replace("'", "''");
		
		
		/*
		 *  Validating fields
		 */
		if (!Validator.validateEmail(email) | !Validator.validatePassword(pwd) | !Validator.validateName(name)| !Validator.validateName(surname)) {
				System.out.println("invalid field");
				request.getRequestDispatcher("register.html").forward(request, response);
				return;
		}
		
		/*
		 * Sanitizing fields
		 */
		email = StringEscapeUtils.escapeHtml4(email);
		name = StringEscapeUtils.escapeHtml4(name);
		surname = StringEscapeUtils.escapeHtml4(surname);
		
		/*
		 * Hashing password. The hash is stored in the database
		 */
		String password = pwd;
		try {
			password = Hash.generateHash(pwd);
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvalidKeySpecException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		
		try {
			
			PreparedStatement statement = conn.prepareStatement("SELECT * FROM [user] WHERE email=?");
			
			statement.setString(1, email);
			
			ResultSet sqlRes = statement.executeQuery();
			
			/*
			 * Only one account per email can be created
			 */
			if (sqlRes.next()) {
				
				System.out.println("Email already registered!");
				request.getRequestDispatcher("register.html").forward(request, response);
				return;
				
			} 
		
			User thisUser = new User(email);
			
			/*
			 * Generates user's keypair (public and private key). It writes the private key in a file
			 * on the client side and  returns the public key.
			 */
			PublicKey publickey = thisUser.createKeys(email);
			
			/*
			 * Encodes the publickey to a byte array to store it in the database.
			 */
			byte[] publicKeyBytes = publickey.getEncoded();
			
			PreparedStatement statement2 = conn.prepareStatement("INSERT INTO [user] ( name, surname, email, password, publickey ) VALUES (?,?,?,?,?)");
	
			statement2.setString(1, name);
			statement2.setString(2, surname);
			statement2.setString(3, email);
			statement2.setString(4, password);	
			statement2.setBytes(5, publicKeyBytes);	
			
			statement2.execute();
					
			request.setAttribute("email", email);
			request.setAttribute("password", pwd);
				
			System.out.println("Registration succeeded!");
					
			/*
			 *  After registration, logs in via Login Servlet
			 */
			
			request.getRequestDispatcher("LoginServlet").forward(request, response);
			
			
			
		} catch (SQLException e) {
			e.printStackTrace();
			request.getRequestDispatcher("register.html").forward(request, response);
		}
	}

}