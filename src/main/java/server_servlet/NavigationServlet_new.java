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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

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
public class NavigationServlet_new extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	  
	private static Connection conn;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public NavigationServlet_new() {
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
				            
				String _emailSender = currentEmail.getSender();
				byte[] _encryptedSubject = currentEmail.getSubject();
				byte[] _encryptedBody = currentEmail.getBody();
				byte[] _digitalSignature = currentEmail.getDigitalSignature();
				String _timestamp = currentEmail.getTimestamp();

				/*
				 * validating email
				 */
				if (!Validator.validateEmail(_emailSender)) {
					System.out.println("Invalid email");
					return "";
				}
				
				/*
				 * sanitizing inputs 
				 * TODO: sanification lato client
				 */
				_emailSender = StringEscapeUtils.escapeHtml4(_emailSender);
				_timestamp = StringEscapeUtils.escapeHtml4(_timestamp);
				
				
				output.append("<div class='mail-inbox'><span>");
				output.append("FROM:&emsp;" + _emailSender + "&emsp;&emsp;AT:&emsp;" + _timestamp);
				output.append("</span>");
				output.append("<br><b>" + _encryptedSubject + "</b>\r\n");
				output.append("<br>" + _encryptedBody);
				
				/*
				 * checks if the email was digitally signed
				 * 
				 */
				if (currentEmail.getDigitalSignature() != null && _encryptedBody != null) {
					
					output.append("\n" + _emailSender + " digitally signed this email.");
						
				} 
				
				output.append("</div>\r\n");
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