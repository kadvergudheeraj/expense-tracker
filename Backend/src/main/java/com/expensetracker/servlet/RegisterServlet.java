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
import java.sql.SQLIntegrityConstraintViolationException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Accept the requested parameter names and the current form's field names.
		String name = request.getParameter("name");
		if (name == null) {
			name = request.getParameter("full-name");
		}
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		String confirmPassword = request.getParameter("confirmPassword");
		if (confirmPassword == null) {
			confirmPassword = request.getParameter("confirm-password");
		}
		String normalizedName = name == null ? "" : name.trim();
		String normalizedEmail = email == null ? "" : email.trim();
		String submittedPassword = password == null ? "" : password;

		HttpSession session = request.getSession();
		String errorMessage = null;
		if (normalizedName.isEmpty()) {
			errorMessage = "Please enter your name.";
		} else if (normalizedEmail.isEmpty()) {
			errorMessage = "Please enter your email address.";
		} else if (submittedPassword.isEmpty()) {
			errorMessage = "Please enter a password.";
		} else if (!submittedPassword.equals(confirmPassword)) {
			errorMessage = "The passwords do not match.";
		}

		if (errorMessage != null) {
			session.setAttribute("message", errorMessage);
			response.sendRedirect(request.getContextPath() + "/register.html");
			return;
		}

		User user = new User(normalizedName, normalizedEmail, submittedPassword);
		UserDAO userDAO = new UserDAO();

		try {
			if (userDAO.registerUser(user)) {
				session.setAttribute("message", "Registration successful. Please log in.");
				response.sendRedirect(request.getContextPath() + "/login.html");
			} else {
				session.setAttribute("message", "Registration could not be completed. Please try again.");
				response.sendRedirect(request.getContextPath() + "/register.html");
			}
		} catch (SQLIntegrityConstraintViolationException exception) {
			session.setAttribute("message", "An account with this email already exists.");
			response.sendRedirect(request.getContextPath() + "/register.html");
		} catch (SQLException exception) {
			getServletContext().log("Unable to register user.", exception);
			session.setAttribute("message", "Registration could not be completed. Please try again.");
			response.sendRedirect(request.getContextPath() + "/register.html");
		}
	}
}