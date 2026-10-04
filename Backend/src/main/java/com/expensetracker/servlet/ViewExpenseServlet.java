package com.expensetracker.servlet;

import com.expensetracker.dao.ExpenseDAO;
import com.expensetracker.model.Expense;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/view-expenses")
public class ViewExpenseServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Get the logged-in user's ID from the session.
		HttpSession session = request.getSession(false);
		Object userIdValue = session == null ? null : session.getAttribute("userId");
		if (!(userIdValue instanceof Integer)) {
			response.sendRedirect(request.getContextPath() + "/login.html");
			return;
		}
		int userId = (Integer) userIdValue;

		try {
			// Load this user's expenses through the DAO.
			ExpenseDAO expenseDAO = new ExpenseDAO();
			List<Expense> expenses = expenseDAO.getExpensesByUser(userId);

			// Make the expense list available to the JSP.
			request.setAttribute("expenses", expenses);

			// Forward the same request to the expenses view.
			request.getRequestDispatcher("/expenses.jsp").forward(request, response);
		} catch (SQLException exception) {
			throw new ServletException("Unable to load expenses.", exception);
		}
	}
}