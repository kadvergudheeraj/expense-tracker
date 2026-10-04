package com.expensetracker.servlet;

import com.expensetracker.dao.UserDAO;
import com.expensetracker.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		HttpSession session = request.getSession();

		if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
			session.setAttribute("message", "Please enter your email and password.");
			response.sendRedirect(request.getContextPath() + "/login.html");
			return;
		}

		try {
			UserDAO userDAO = new UserDAO();
			User user = userDAO.loginUser(email.trim(), password);

			if (user == null) {
				session.setAttribute("message", "Email or password is incorrect.");
				response.sendRedirect(request.getContextPath() + "/login.html");
				return;
			}

			session.setAttribute("userId", user.getId());
			session.setAttribute("userName", user.getName());
			session.setAttribute("userEmail", user.getEmail());
			session.removeAttribute("message");
			response.sendRedirect(request.getContextPath() + "/dashboard.html");
		} catch (SQLException exception) {
			getServletContext().log("Unable to log in.", exception);
			session.setAttribute("message", "Login could not be completed. Please try again.");
			response.sendRedirect(request.getContextPath() + "/login.html");
		}
	}
}