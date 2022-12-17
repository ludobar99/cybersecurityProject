package server_servlet;

import asymmetricEncryption.KeyGetter;
import database.DBAPI;
import database.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import util.Validator;

import java.io.IOException;
import java.sql.Connection;

@WebServlet("/ReceiversServlet")
public class ReceiversServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ReceiversServlet() {
        super();
    }

    public void init() throws ServletException {
        conn = DBConnection.getConn();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Session check
        HttpSession session = request.getSession(false);
        if (session == null)  {
            response.sendRedirect("login.html");
            return;
        }
        String email = request.getParameter("email").replace("'", "''");

        // Validating receiver email
        if (!Validator.validateEmail(email)) {
            System.out.println("Invalid email");
            request.getRequestDispatcher("login.html").forward(request, response);
            return;
        }

        // Checking receiver existence
        try {
            if (DBAPI.getAccount(email) == null) {
                System.out.println("Request receiver does not exist");
                response.sendError(500, "Request receiver does not exist");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Sending back requested key
        try {
            byte[] receiverPublicKeyBytes = KeyGetter.getPublicKeyBytes(email);

            response.getOutputStream().write(receiverPublicKeyBytes);
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
