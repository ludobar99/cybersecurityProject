package server_servlet;

import jakarta.servlet.http.HttpServlet;
import java.io.IOException;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import jakarta.servlet.http.HttpSession;
import org.apache.commons.text.StringEscapeUtils;

import asymmetricEncryption.Encryptor;
import asymmetricEncryption.FromBytesToKeyConverter;
import asymmetricEncryption.KeyGetter;
import database.DBConnection;
import digitalSignature.DigestGenerator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.Paths;
import util.SessionManager;
import util.Validator;

/**
 * Servlet implementation class SendMailServlet
 */
@WebServlet("/SendMailServlet")
public class SendMailServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static Connection conn;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SendMailServlet() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public void init() throws ServletException {
   
    	conn = DBConnection.getInstance().getConn();
    	
    }

    /*
     * TODO: ora il corpo e l'oggetto dell'e-mail vengono criptati nel server. 
     * All'interno di navigation servlet, devo modificare il codice in modo tale che 
     * questi payload vengano criptati sul lato client.
     */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");

		// Session check
		HttpSession session = request.getSession(false);
		if (session == null)  {
			response.sendRedirect("login.html");
			return;
		}
		String user = SessionManager.getSessionUser(session);

		String sender = request.getParameter("email").replace("'", "''");
		String receiver = request.getParameter("receiver").replace("'", "''");
		String subject = request.getParameter("subject").replace("'", "''");
		String body = request.getParameter("body").replace("'", "''");
		String timestamp = new Date(System.currentTimeMillis()).toInstant().toString();
		
		/*
		 * if the email of the session and the email in the request are different, the user is redirected to login.html
		 */
		if (user.compareTo(sender) != 0) {
			request.getRequestDispatcher("login.html").forward(request, response);
			return;
		}
		
		/*
		 *  validating both e-mails
		 */
		if (!Validator.validateEmail(sender) | !Validator.validateEmail(receiver)) {
			System.out.println("Invalid email");
			request.getRequestDispatcher("login.html").forward(request, response);
			return;
		}
		
		/*
		 * sanitizing fields
		 */
		sender = StringEscapeUtils.escapeHtml4(sender);
		receiver = StringEscapeUtils.escapeHtml4(receiver);
		subject = StringEscapeUtils.escapeHtml4(subject);
		body = StringEscapeUtils.escapeHtml4(body);
		
		//TODO: mail encryption
		//1. get public key of the receiver
		//2. encrypt email (body and subject) with public key
		//3. send email
		// ENSURE that the e-mail is encrypted on the client side!
		
		byte[] encryptedBody = null;
		byte[] encryptedSubject = null;
		/*
		 * TODO: remove this method somehow
		 */
		KeyGetter.init();
		byte[] receiverPublicKeyBytes = null;
		try {
			receiverPublicKeyBytes = KeyGetter.getPublicKeyBytes(receiver);
		} catch (SQLException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		
		/*
		 * if receiverPublicKeyBytes is null (the email does not exist), it prints an error message
		 */
		if (receiverPublicKeyBytes == null) {
			
			System.out.println("Email address " + receiver + " does not exist.");
			request.getRequestDispatcher("home.jsp").forward(request, response);
			return;
		}
		
		/*
		 * If the user checks the digital signature check-box, a message digest is generated.
		 */
		byte[] encryptedDigest = null;

		if (request.getParameter("digitalSignature") != null) {
						 
			try {
				
				byte[] digest = DigestGenerator.generateDigest(body);
				
				// encrypting digest with private key
				PrivateKey privateKey = null;
				try {
					String sourcePath = getServletContext().getRealPath("/");
					Path rootPath = Paths.getRootPath(sourcePath);

					byte[] privateKeyBytes = KeyGetter.getPrivateKeyBytes(rootPath.toString(), user);
					privateKey = FromBytesToKeyConverter.getPrivateKeyfromBytes(privateKeyBytes);
				} catch (InvalidKeySpecException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				encryptedDigest = Encryptor.encrypt(digest, privateKey);
				
			
			} catch (NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		}
		
		/*
		 * encrypts email (body and subject) with public key
		 */
		 
		PublicKey receiverPublicKey = null;
		try {
			receiverPublicKey = FromBytesToKeyConverter.getPublicKeyFromBytes(receiverPublicKeyBytes);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
 
		try {
		
			encryptedBody = Encryptor.encrypt(body.getBytes(), receiverPublicKey);
			encryptedSubject = Encryptor.encrypt(subject.getBytes(), receiverPublicKey);
		
		} catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException
				| NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		/*
		 * sending email (saving it in the database)
		 * TODO: add digest (but before move that code on the client side)
		 */
		try {
			
			PreparedStatement statement = conn.prepareStatement("INSERT INTO mail ( sender, receiver, subject, body, digitalSignature, [time] ) VALUES ( ?, ?, ?, ?, ?, ?)");
			
			statement.setString(1, sender);
			statement.setString(2, receiver);
			statement.setBytes(3, encryptedSubject);
			statement.setBytes(4, encryptedBody);
			statement.setBytes(5, encryptedDigest);
			statement.setString(6, timestamp);
			
			
			statement.execute();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		request.setAttribute("email", sender);
		request.getRequestDispatcher("home.jsp").forward(request, response);
	}
	
}