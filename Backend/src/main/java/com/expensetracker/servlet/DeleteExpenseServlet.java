package com.expensetracker.servlet;

import com.expensetracker.dao.ExpenseDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/delete-expense")
public class DeleteExpenseServlet extends HttpServlet {
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession existingSession = request.getSession(false);
		Object userIdValue = existingSession == null ? null : existingSession.getAttribute("userId");
		if (!(userIdValue instanceof Integer)) {
			response.sendRedirect(request.getContextPath() + "/login.html");
			return;
		}
		int userId = (Integer) userIdValue;
		HttpSession session = request.getSession();

		int id;
		try {
			id = Integer.parseInt(request.getParameter("id"));
			if (id <= 0) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException exception) {
			session.setAttribute("message", "The expense could not be identified.");
			response.sendRedirect(request.getContextPath() + "/view-expenses");
			return;
		}

		try {
			ExpenseDAO expenseDAO = new ExpenseDAO();
			if (expenseDAO.deleteExpense(id, userId)) {
				session.setAttribute("message", "Expense deleted successfully.");
			} else {
				session.setAttribute("message", "Expense was not found or could not be deleted.");
			}
			response.sendRedirect(request.getContextPath() + "/view-expenses");
		} catch (SQLException exception) {
			getServletContext().log("Unable to delete expense.", exception);
			session.setAttribute("message", "The expense could not be deleted. Please try again.");
			response.sendRedirect(request.getContextPath() + "/view-expenses");
		}
	}
}