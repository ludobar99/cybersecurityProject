package server_servlet;

import jakarta.servlet.http.HttpServlet;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.text.StringEscapeUtils;

import database.DBAPI;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
		request.getRequestDispatcher("home.jsp").forward(request, response);
	}
	
}