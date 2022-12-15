package server_servlet;

import jakarta.servlet.http.HttpServlet;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import org.apache.commons.text.StringEscapeUtils;

import database.DBAPI;
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
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegisterServlet() {
        super();
    }
    
    public void init() throws ServletException {


    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		/*
		 * The replacement escapes apostrophe special character in order to store it in SQL.
		 */
		String name = request.getParameter("name").replace("'", "''");
		String surname = request.getParameter("surname").replace("'", "''");
		String email = request.getParameter("email").replace("'", "''");
		String pwd = request.getParameter("password").replace("'", "''");
		String publicKey = request.getParameter("publicKey").replace("'", "''");

		// Validation
		if (!Validator.validateEmail(email)
				| !Validator.validatePassword(pwd)
				| !Validator.validateName(name)
				| !Validator.validateName(surname)
		) {
				System.out.println("invalid field");
				response.sendRedirect("register.html");
				return;
		}
		
		//	Sanitization
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
			e1.printStackTrace();
		} catch (InvalidKeySpecException e1) {
			e1.printStackTrace();
		}

		try {
			
			if (DBAPI.getAccount(email) != null) {
				System.out.println("Email already registered!");
				response.sendRedirect("register.html");
				return;
			}

			/*
			 * Encodes the publickey to a byte array to store it in the database.
			 */
			byte[] publicKeyBytes = publicKey.getBytes();
			DBAPI.registerUser(name, surname, email, password, publicKeyBytes);
			
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