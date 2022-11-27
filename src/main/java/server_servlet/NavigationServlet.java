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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import jakarta.servlet.http.HttpSession;
import mail.EMail;

import org.apache.commons.text.StringEscapeUtils;

import asymmetricEncryption.Decryptor;
import asymmetricEncryption.FromBytesToKeyConverter;
import asymmetricEncryption.KeyGetter;
import database.DBAPI;
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
 * Servlet implementation class NavigationServlet
 */
@WebServlet("/NavigationServlet")
public class NavigationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	  
	private static Connection conn;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public NavigationServlet() {
        super();
    }
    
    public void init() throws ServletException {
    
    	String sourcePath = getServletContext().getRealPath("/" );
    	conn = DBConnection.getInstance(sourcePath).getConn();
    	
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");

		// Session check
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.sendRedirect("login.html");
			return;
		}
		String email = SessionManager.getSessionUser(session);
		
		/*
		 * validating email
		 */
		if (!Validator.validateEmail(email)) {
			System.out.println("Invalid email");
			request.getRequestDispatcher("login.html").forward(request, response);
			return;
		}
		
		
		/*
		 * sanitizing email
		 */
		email = StringEscapeUtils.escapeHtml4(email);
					
		
		if (request.getParameter("newMail") != null)
		
			request.setAttribute("content", getHtmlForNewMail(email));
		
		else if (request.getParameter("inbox") != null)
			
			request.setAttribute("content", getHtmlForInbox(email));
	
		else if (request.getParameter("sent") != null)
			
			request.setAttribute("content", getHtmlForSent(email));
		
		else if (request.getParameter("search") != null) {
			
			String item = request.getParameter("search").replace("'", "''");
			
			request.setAttribute("content", getHTMLforSearch(item));
	
		
		}
	
		request.setAttribute("email", email);
		request.getRequestDispatcher("home.jsp").forward(request, response);
			
	}

	/*
	 * mail in-box
	 */
	private String getHtmlForInbox(String email) {
		
		/*
		 * TODO: create method  getMails
		 */
		try {
			
			/*
			 * Getting inbox emails
			 */
			ArrayList<EMail> inbox = DBAPI.getInbox(conn, email);
			
			StringBuilder output = new StringBuilder();

			for (int i = 0; i < inbox.size(); i++) {
				
				EMail currentEmail = inbox.get(i);
				
				/*
				 * email decryption via private key 
				 */
				byte[] decryptedBody = null;
				byte[] decryptedSubject = null;
				
				/*
				 * TODO: remove this method somehow
				 */
				KeyGetter.init();
				
				byte[] privateKeyBytes = null;
				
				try {
					String sourcePath = getServletContext().getRealPath("/");
					Path rootPath = Paths.getRootPath(sourcePath);

					privateKeyBytes = KeyGetter.getPrivateKeyBytes(rootPath.toString(), email);
				} catch (IOException e1) {

					e1.printStackTrace();
				}
				
				PrivateKey privateKey = null;
				try {
					privateKey = FromBytesToKeyConverter.getPrivateKeyfromBytes(privateKeyBytes);
				} catch (InvalidKeySpecException | NoSuchAlgorithmException e1) {

					e1.printStackTrace();
				}
				
				String _body = null;
				String _subject = null;
				try {
				
					
					decryptedBody = Decryptor.decrypt(currentEmail.getBody(), privateKey);
					_body = new String(decryptedBody);
					
					decryptedSubject = Decryptor.decrypt(currentEmail.getSubject(), privateKey);
					_subject = new String(decryptedSubject);
				
				} catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException
						| NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				/*
				 * validating email
				 */
				if (!Validator.validateEmail(currentEmail.getSender())) {
					System.out.println("Invalid email");
					return "";
				}
				
				/*
				 * sanitizing inputs 
				 */
				String _emailSender = StringEscapeUtils.escapeHtml4(currentEmail.getSender());
				_body = StringEscapeUtils.escapeHtml4(_body);
				_subject = StringEscapeUtils.escapeHtml4(_subject);
				String _timestamp = StringEscapeUtils.escapeHtml4(currentEmail.getTimestamp());
				
				
				output.append("<div style=\"white-space: pre-wrap;\"><span style=\"color:grey;\">");
				output.append("FROM:&emsp;" + _emailSender + "&emsp;&emsp;AT:&emsp;" + _timestamp);
				output.append("</span>");
				output.append("<br><b>" + _subject + "</b>\r\n");
				output.append("<br>" + _body);
				
				/*
				 * checks if the email was digitally signed
				 * 
				 */
				if (currentEmail.getDigitalSignature() != null) {
					
					byte[] senderPublicKeyBytes = KeyGetter.getPublicKeyBytes(_emailSender);
					byte[] digitalSignature = null;
					
					// checking that the public key is not null
					if (senderPublicKeyBytes != null) {
					
						/*
						 * decrypting digest
						 */
						PublicKey senderPublicKey = null;
						try {
							senderPublicKey = FromBytesToKeyConverter.getPublicKeyFromBytes(senderPublicKeyBytes);
						} catch (NoSuchAlgorithmException | InvalidKeySpecException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						try {
						
							digitalSignature = Decryptor.decrypt(currentEmail.getDigitalSignature(), senderPublicKey);
							System.out.println(digitalSignature);
						
						} catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException
								| NoSuchPaddingException | NoSuchAlgorithmException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						/*
						 *  generating digest and comparing it with the received one
						 */
						byte[] digest = null;
						
						try {
							digest = DigestGenerator.generateDigest(_body);
						
						} catch (NoSuchAlgorithmException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						if (Arrays.equals(digest, digitalSignature)) {
						
							output.append("\n" + _emailSender + " digitally signed this email.");
						
						} else {
					
							output.append("\n" + _emailSender + " digitally signed this email, but something went wrong.\n");
							output.append(_emailSender + " didn't sign this email or the content was altered.");

					
						}
				
					}
				
				} else {
					// TODO: perchè lo stampa??
					System.out.println("Error getting sender public key. The user might not exist.");
				}
				
				
				
				output.append("</div>\r\n");
				
				output.append("<hr style=\"border-top: 2px solid black;\">\r\n");
			}
			
			output.append("</div>");
			
			return output.toString();
			
		} catch (SQLException e) {
			e.printStackTrace();
			return "ERROR IN FETCHING INBOX MAILS!";
		}
	}
	
	
	private String getHtmlForNewMail(String email) {
		/*
		 * Validating email
		 */
		if (!Validator.validateEmail(email)) {
			System.out.println("Invalid email");
			return "";
		}
		
		/*
		 * Sanitizing email
		 */
		email = StringEscapeUtils.escapeHtml4(email);

		
		return 
				"<form id=\"submitForm\" class=\"form-resize\" action=\"SendMailServlet\" method=\"post\">\r\n"
				+ "		<input type=\"hidden\" name=\"email\" value=\""+email+"\">\r\n"
				+ "		<input class=\"single-row-input\" type=\"email\" name=\"receiver\" placeholder=\"Receiver\" required>\r\n"
				+ "		<input class=\"single-row-input\" type=\"text\"  name=\"subject\" placeholder=\"Subject\" required>\r\n"
				+ "		<textarea class=\"textarea-input\" name=\"body\" placeholder=\"Body\" wrap=\"hard\" required></textarea>\r\n"
				+ "		<label for=\"digitalSignature\">Digital Signature</label><br>"
				+ "		<input type=\"checkbox\" name=\"digitalSignature\" value=\"yes\">"
				+ "		<input type=\"submit\" name=\"sent\" value=\"Send\">\r\n"
				+ "	</form>";
		}
	
	private String getHtmlForSent(String email) {
		try {
			
			PreparedStatement statement = conn.prepareStatement("SELECT * FROM mail WHERE sender=? ORDER BY [time] DESC");
			
			statement.setString(1, email);
			
			ResultSet sqlRes = statement.executeQuery();
			
			
			StringBuilder output = new StringBuilder();
			output.append("<div>\r\n");
			
			while (sqlRes.next()) {
				String _emailReceiver = sqlRes.getString(2);
				byte[] _subject = sqlRes.getBytes(3);
				byte[] _body = sqlRes.getBytes(4);
				String _timestamp = sqlRes.getString(5);
				
				/*
				 * Validating receiver's email
				 */
				if (!Validator.validateEmail(_emailReceiver)) {
					System.out.println("Invalid email");
					return "";
				}
				
				/*
				 * Sanitizing 
				 */
				_emailReceiver = StringEscapeUtils.escapeHtml4(_emailReceiver);
				_timestamp = StringEscapeUtils.escapeHtml4(_timestamp);
				
				output.append("<div style=\"white-space: pre-wrap;\"><span style=\"color:grey;\">");
				output.append("TO:&emsp;" + _emailReceiver + "&emsp;&emsp;AT:&emsp;" + _timestamp);
				output.append("</span>");
				output.append("<br><b>" + _subject + "</b>\r\n");
				output.append("<br>" + _body);
				output.append("</div>\r\n");
				
				output.append("<hr style=\"border-top: 2px solid black;\">\r\n");
			}
			
			output.append("</div>");
			
			return output.toString();
			
		} catch (SQLException e) {
			e.printStackTrace();
			return "ERROR IN FETCHING INBOX MAILS!";
		}
	}
	
	private String getHTMLforSearch(String item) {
		
			StringBuilder output = new StringBuilder();
			
			/*
			 * sanitizing item
			 */
			item = StringEscapeUtils.escapeHtml4(item);
			
			output.append("<div>\r\n");
			
			output.append("<p>You searched for: "+ item +"</p>");
			
			output.append("</div>");
			
			return output.toString();
			
	}
}