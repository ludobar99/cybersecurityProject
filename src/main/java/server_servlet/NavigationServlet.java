package server_servlet;

import jakarta.servlet.http.HttpServlet;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import jakarta.servlet.http.HttpSession;
import mail.EMail;

import org.apache.commons.text.StringEscapeUtils;

import database.DBAPI;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.SessionManager;
import util.Validator;

/**
 * Servlet implementation class NavigationServlet
 */
@WebServlet("/NavigationServlet")
public class NavigationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	  
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public NavigationServlet() {
        super();
    }
    
    public void init() throws ServletException {
    
    	
    	
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

		// CSRF Check
		String sessionCSRFToken = null;
		try {
			sessionCSRFToken = SessionManager.getCSRFToken(session);
			String requestCSRFToken = request.getParameter("csrfToken");
			System.out.println(sessionCSRFToken + " " + requestCSRFToken);
			if (!sessionCSRFToken.equals(requestCSRFToken)) throw new Exception("CSRF Tokens do not match!");
		} catch (Exception error) {
			response.sendError(403, "CSRF Token error");
			return;
		}
		
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
			request.setAttribute("content", getHtmlForNewMail(user, sessionCSRFToken));

		else if (request.getParameter("inbox") != null)
			request.setAttribute("content", getHtmlForInbox(user));

		else if (request.getParameter("sent") != null)
			request.setAttribute("content", getHtmlForSent(user));

		else if (request.getParameter("search") != null) {
			String item = request.getParameter("search").replace("'", "''");
			request.setAttribute("content", getHTMLforSearch(item));
		}
	
		request.setAttribute("email", user);
		request.setAttribute("csrfToken", sessionCSRFToken);
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
			ArrayList<EMail> inbox = DBAPI.getInbox(email);
			
			StringBuilder output = new StringBuilder();

			//Container
			output.append("<div class='mail-inbox-container'>");
		 
			for (int i = 0; i < inbox.size(); i++) {
				
				EMail currentEmail = inbox.get(i);

				byte[] subject = currentEmail.getSubject();
				byte[] body = currentEmail.getBody();
				byte[] signature = currentEmail.getDigitalSignature();

				String subjectString = new String(subject);
				String bodyString = new String(body);
				String timestamp = currentEmail.getTimestamp();
				String sender = currentEmail.getSender();

				/*
				 * sanitizing subject
				 */
				subjectString = StringEscapeUtils.escapeHtml4(subjectString);
				
				String signatureString = null;
				if (signature != null) {
					signatureString = new String(signature);
				}

				output.append("<div class='mail-inbox'><span>");
				output.append("FROM:&emsp;<span class='email-sender'>" + sender + "</span>&emsp;&emsp;AT:&emsp;" + timestamp);
				output.append("</span>");
				output.append("<br><b><span class=\"email-subject\">" + subjectString + "</span></b>\r\n");
				output.append("<br><span class=\"email-body\">" + bodyString + "</span>");

				if (signatureString != null) {
					output.append("<span class='hidden'><span class=\"email-signature\">" + signatureString + "</span></span>");
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
	
	
	private String getHtmlForNewMail(String email, String sessionCSRFToken) {
		/*
		 * Validating email
		 */
		if (!Validator.validateEmail(email)) {
			System.out.println("Invalid email");
			return "";
		}

		return 
				"<form id=\"submitForm\" class=\"form-resize\">\r\n"
				+ "		<input type=\"hidden\" name=\"csrfToken\" value=\""+ sessionCSRFToken +"\">\r\n"
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
			ArrayList<EMail> sentEmail = DBAPI.getSentEmails(email);
			
			StringBuilder output = new StringBuilder();

			output.append("<div>\r\n");
			
			for (int i = 0; i < sentEmail.size(); i++) {

				byte[] _subjectBytes = sentEmail.get(i).getSubject();
				byte[] _bodyBytes = sentEmail.get(i).getBody();
				String _body = new String(_bodyBytes);
				String _subject = new String(_subjectBytes);
				String _emailReceiver = sentEmail.get(i).getReceiver();
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
				String _sanitizedSubject = StringEscapeUtils.escapeHtml4(new String(_subject));
				
				output.append("<div class='mail-sent'><span>");
				output.append("TO:&emsp;" + _emailReceiver + "&emsp;&emsp;AT:&emsp;" + _timestamp);
				output.append("</span>");
				output.append("<br><b>" + _sanitizedSubject + "</b>\r\n");
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