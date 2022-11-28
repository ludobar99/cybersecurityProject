package server_servlet;

import jakarta.servlet.http.HttpServlet;
import java.io.IOException;
import java.nio.file.Path;
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
import database.DBAPI;
import database.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.Hash;
import util.Paths;
import util.Validator;

/**
 * Servlet implementation class RegisterServlet
 */
@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static Connection conn;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegisterServlet() {
        super();
    }
    
    public void init() throws ServletException {

    	conn = DBConnection.getInstance().getConn();

    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
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
		if (!Validator.validateEmail(email)
				| !Validator.validatePassword(pwd)
				| !Validator.validateName(name)
				| !Validator.validateName(surname)
		) {
				System.out.println("invalid field");
				response.sendRedirect("register.html");
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
			
			if (DBAPI.checkIfUserExists(conn, email)) {
				System.out.println("Email already registered!");
				response.sendRedirect("register.html");
				return;
			}
			
			/*
			 * Generates user's keypair (public and private key). It writes the private key in a file
			 * on the "client side" and returns the public key.
			 */
			String sourcePath = getServletContext().getRealPath("/" );
			Path rootPath = Paths.getRootPath(sourcePath);
			User thisUser = new User(email, "a");

			PublicKey publickey = thisUser.createKeys(rootPath.toString() + "/keys/" + email);
			
			/*
			 * Encodes the publickey to a byte array to store it in the database.
			 * TODO: save in bytes or save in a string?
			 */
			byte[] publicKeyBytes = publickey.getEncoded();
			
			DBAPI.registerUser(conn, name, surname, email, password, publicKeyBytes);
			
			/*
			 *  After registration, redirects to login
			 */
			System.out.println("Registration succeeded!");
			response.sendRedirect("login.html");
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendRedirect("register.html");
		} catch (Exception e) {
			e.printStackTrace();
			response.sendRedirect("register.html");
		}
	}

}