package server_servlet;

import jakarta.servlet.http.HttpServlet;
import java.io.IOException;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.Date;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import jakarta.servlet.http.HttpSession;
import org.apache.commons.text.StringEscapeUtils;

import asymmetricEncryption.Encryptor;
import asymmetricEncryption.FromBytesToKeyConverter;
import asymmetricEncryption.KeyGetter;
import database.DBAPI;
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
	       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SendMailServlet() {
        super();
    }
    
    public void init() throws ServletException {

    	
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");

		// Session check
		HttpSession session = request.getSession(false);
		if (session == null)  {
			response.sendRedirect("login.html");
			return;
		}
		String sender = SessionManager.getSessionUser(session);

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

		// Extracting data from request
		String receiver = request.getParameter("receiver").replace("'", "''");
		String subject = request.getParameter("subject").replace("'", "''");
		String body = request.getParameter("body").replace("'", "''");
		String timestamp = new Date(System.currentTimeMillis()).toInstant().toString();

		String signature = request.getParameter("signature");
		if (signature != null) {
			signature = signature.replace("'", "''");
		}

		/*
		 * sanitizing fields
		 */
		receiver = StringEscapeUtils.escapeHtml4(receiver);
		subject = StringEscapeUtils.escapeHtml4(subject);
		body = StringEscapeUtils.escapeHtml4(body);
		signature = StringEscapeUtils.escapeHtml4(signature);

		// Validating receiver email
		if (!Validator.validateEmail(receiver)) {
			System.out.println("Invalid email");
			request.getRequestDispatcher("login.html").forward(request, response);
			return;
		}

		// Checking receiver existence
		try {
			if (DBAPI.getAccount(receiver) == null) {
				System.out.println("Receiver does not exist");
				response.sendError(500, "Request receiver does not exist");
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Saving database in email (sending)
		try {
			byte[] bodyBytes = body.getBytes();
			byte[] subjectBytes = subject.getBytes();

			byte[] signatureBytes = null;
			if (signature != null) {
				signatureBytes = signature.getBytes();
			}

			DBAPI.sendEmail(sender, receiver, subjectBytes, bodyBytes, signatureBytes, timestamp);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		request.setAttribute("email", sender);
		request.setAttribute("csrfToken", sessionCSRFToken);
		request.getRequestDispatcher("home.jsp").forward(request, response);
	}
	
}