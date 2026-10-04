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
import java.sql.Date;
import java.sql.SQLException;

@WebServlet("/add-expense")
public class AddExpenseServlet extends HttpServlet {
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Get the logged-in user's ID from the existing session.
		HttpSession session = request.getSession(false);
		Object userIdValue = session == null ? null : session.getAttribute("userId");
		if (!(userIdValue instanceof Integer)) {
			response.sendRedirect(request.getContextPath() + "/login.html");
			return;
		}
		int userId = (Integer) userIdValue;

		// Read the submitted expense details from the form.
		String amountValue = request.getParameter("amount");
		String category = request.getParameter("category");
		String description = request.getParameter("description");
		String expenseDateValue = request.getParameter("expenseDate");

		double amount;
		try {
			amount = Double.parseDouble(amountValue);
		} catch (NumberFormatException exception) {
			session = request.getSession(true);
			session.setAttribute("errorMessage", "Please enter a valid amount.");
			response.sendRedirect(request.getContextPath() + "/add-expense.html");
			return;
		}

		Date expenseDate;
		try {
			expenseDate = Date.valueOf(expenseDateValue);
		} catch (IllegalArgumentException exception) {
			session = request.getSession(true);
			session.setAttribute("errorMessage", "Please enter a valid expense date.");
			response.sendRedirect(request.getContextPath() + "/add-expense.html");
			return;
		}

		// Create the model object from the submitted values and session user ID.
		Expense expense = new Expense(userId, amount, category, description, expenseDate);

		// Save the expense through the DAO rather than running SQL in the servlet.
		try {
			ExpenseDAO expenseDAO = new ExpenseDAO();
			boolean added = expenseDAO.addExpense(expense);
			if (added) {
				response.sendRedirect(request.getContextPath() + "/view-expenses");
			} else {
				session = request.getSession(true);
				session.setAttribute("errorMessage", "The expense could not be added. Please try again.");
				response.sendRedirect(request.getContextPath() + "/add-expense.html");
			}
		} catch (SQLException exception) {
			getServletContext().log("Unable to add expense.", exception);
			session = request.getSession(true);
			session.setAttribute("errorMessage", "A database error prevented the expense from being added.");
			response.sendRedirect(request.getContextPath() + "/add-expense.html");
		}
	}
}