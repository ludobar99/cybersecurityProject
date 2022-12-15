package server_servlet;

import jakarta.servlet.http.HttpServlet;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

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
    
    	conn = DBConnection.getConn();
    	
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
		String user = SessionManager.getSessionUser(session);
		
		/*
		 * validating user
		 */
		if (!Validator.validateEmail(user)) {
			System.out.println("Invalid user");
			request.getRequestDispatcher("login.html").forward(request, response);
			return;
		}

		/*
		 * sanitizing user
		 */
		user = StringEscapeUtils.escapeHtml4(user);
					
		
		if (request.getParameter("newMail") != null)
			request.setAttribute("content", getHtmlForNewMail(user));

		else if (request.getParameter("inbox") != null)
			request.setAttribute("content", getHtmlForInbox(user));

		else if (request.getParameter("sent") != null)
			request.setAttribute("content", getHtmlForSent(user));

		else if (request.getParameter("search") != null) {
			String item = request.getParameter("search").replace("'", "''");
			request.setAttribute("content", getHTMLforSearch(item));
		}
	
		request.setAttribute("email", user);
		request.getRequestDispatcher("home.jsp").forward(request, response);
	}

	/*
	 * mail in-box
	 */
	private String getHtmlForInbox(String email) {
		
		try {
			
			/*
			 * Getting inbox emails
			 */
			ArrayList<EMail> inbox = DBAPI.getInbox(conn, email);
			
			StringBuilder output = new StringBuilder();

			//Container
			output.append("<div class='mail-inbox-container'>");
		 
			for (int i = 0; i < inbox.size(); i++) {
				
				EMail currentEmail = inbox.get(i);

				String sender = currentEmail.getSender();
				String timestamp = currentEmail.getTimestamp();
				byte[] subject = currentEmail.getSubject();
				byte[] body = currentEmail.getBody();

				String subjectString = new String(subject);
				String bodyString = new String(body);

				output.append("<div class='mail-inbox'><span>");
				output.append("FROM:&emsp;" + sender + "&emsp;&emsp;AT:&emsp;" + timestamp);
				output.append("</span>");
				output.append("<br><b><span class=\"email-subject\">" + subjectString + "</span></b>\r\n");
				output.append("<br><span class=\"email-body\">" + bodyString + "</span>");
				
				/*
				 * checks if the email was digitally signed
				 * 
				 */
				if (currentEmail.getDigitalSignature() != null && body != null) {
					
					byte[] senderPublicKeyBytes = KeyGetter.getPublicKeyBytes(conn, sender);
					byte[] digitalSignature = currentEmail.getDigitalSignature();
					
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
						
							digitalSignature = Decryptor.decrypt(digitalSignature, senderPublicKey);
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
							digest = DigestGenerator.generateDigest(bodyString);
						} catch (NoSuchAlgorithmException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						if (Arrays.equals(digest, digitalSignature)) {
						
							output.append("\n" + sender + " digitally signed this email.");
						
						} else {
					
							output.append("\n" + sender + " digitally signed this email, but something went wrong.\n");
							output.append(sender + " didn't sign this email or the content was altered.");
					
						}
				
					}
				
				}
					
				output.append("</div>\r\n");
			}

			output.append("</div>");
			
			return output.toString();
			
		} catch (SQLException e) {
			e.printStackTrace();
			return "ERROR IN FETCHING INBOX MAILS!";
		} catch (Exception e) {
			e.printStackTrace();
			return "";
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
				"<form id=\"submitForm\" class=\"form-resize\">\r\n"
				+ "		<input type=\"hidden\" name=\"email\" value=\""+email+"\">\r\n"
				+ "		<input class=\"single-row-input\" type=\"email\" name=\"receiver\" placeholder=\"Receiver\" required>\r\n"
				+ "		<input class=\"single-row-input\" type=\"text\"  name=\"subject\" placeholder=\"Subject\" required>\r\n"
				+ "		<textarea class=\"textarea-input\" name=\"body\" placeholder=\"Body\" wrap=\"hard\" rows='10' required></textarea>\r\n"
				+ "		<div class='controls'><input type=\"submit\" name=\"sent\" value=\"Send\"><div class='signature'>"
                + "     <label for=\"digitalSignature\">Digital Signature</label>"
				+ "		<input type=\"checkbox\" name=\"digitalSignature\" value=\"yes\"></div>"
				+ "		</div>\r\n"
				+ "	</form>";
		}
	
	private String getHtmlForSent(String email) {
		try {
			
			/*
			 * Getting sent emails
			 */
			ArrayList<EMail> sentEmail = DBAPI.getSentEmails(conn, email);
			
			StringBuilder output = new StringBuilder();

			output.append("<div>\r\n");
			
			for (int i = 0; i < sentEmail.size(); i++) {
				
				String _emailReceiver = sentEmail.get(i).getReceiver();
				byte[] _subject = sentEmail.get(i).getSubject();
				byte[] _body = sentEmail.get(i).getBody();
				String _timestamp = sentEmail.get(i).getTimestamp();
				
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
				
				output.append("<div class='mail-sent'><span>");
				output.append("TO:&emsp;" + _emailReceiver + "&emsp;&emsp;AT:&emsp;" + _timestamp);
				output.append("</span>");
				output.append("<br><b>" + _subject + "</b>\r\n");
				output.append("<br>" + _body);
				output.append("</div>\r\n");
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