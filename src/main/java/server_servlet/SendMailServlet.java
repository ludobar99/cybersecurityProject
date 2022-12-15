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

    	conn = DBConnection.getConn();
    	
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
		String sender = SessionManager.getSessionUser(session);

		// Extracting data from request
		String receiver = request.getParameter("receiver").replace("'", "''");
		String subject = request.getParameter("subject").replace("'", "''");
		String body = request.getParameter("body").replace("'", "''");
		String timestamp = new Date(System.currentTimeMillis()).toInstant().toString();

		/*
		 * sanitizing fields
		 */
		receiver = StringEscapeUtils.escapeHtml4(receiver);
		subject = StringEscapeUtils.escapeHtml4(subject);
		body = StringEscapeUtils.escapeHtml4(body);
		timestamp = StringEscapeUtils.escapeHtml4(timestamp);

		// Validating receiver email
		if (!Validator.validateEmail(receiver)) {
			System.out.println("Invalid email");
			request.getRequestDispatcher("login.html").forward(request, response);
			return;
		}

		// Checking receiver existence
		try {
			if (DBAPI.getAccount(conn, receiver) == null) {
				System.out.println("Request receiver does not exist");
				response.sendError(500, "Request receiver does not exist");
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
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

					byte[] privateKeyBytes = KeyGetter.getPrivateKeyBytes(rootPath.toString(), receiver);
					privateKey = FromBytesToKeyConverter.getPrivateKeyfromBytes(privateKeyBytes);
				} catch (InvalidKeySpecException | IOException e) {
					e.printStackTrace();
				}
				encryptedDigest = Encryptor.encrypt(digest, privateKey);
				
			
			} catch (NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e2) {
				e2.printStackTrace();
			}
		}
		
		// Saving database in email (sending)
		try {
			byte[] bodyBytes = body.getBytes();
			byte[] subjectBytes = subject.getBytes();

			DBAPI.sendEmail(conn, sender, receiver, subjectBytes, bodyBytes, encryptedDigest, timestamp);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		request.setAttribute("email", sender);
		request.getRequestDispatcher("home.jsp").forward(request, response);
	}
	
}