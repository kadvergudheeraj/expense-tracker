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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.SQLException;

@WebServlet("/update-expense")
public class UpdateExpenseServlet extends HttpServlet {
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

		String idValue = request.getParameter("id");
		int id;
		try {
			id = Integer.parseInt(idValue);
			if (id <= 0) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException exception) {
			session.setAttribute("message", "The expense could not be identified.");
			response.sendRedirect(request.getContextPath() + "/view-expenses");
			return;
		}

		String amountValue = request.getParameter("amount");
		String category = request.getParameter("category");
		String description = request.getParameter("description");
		String expenseDateValue = request.getParameter("expenseDate");

		double amount;
		try {
			amount = Double.parseDouble(amountValue);
		} catch (NumberFormatException exception) {
			redirectToEditForm(request, response, idValue, amountValue, category, description, expenseDateValue,
					"Enter a valid amount greater than zero.");
			return;
		}

		if (!Double.isFinite(amount) || amount <= 0 || category == null || category.trim().isEmpty()
				|| description == null || description.trim().isEmpty()) {
			redirectToEditForm(request, response, idValue, amountValue, category, description, expenseDateValue,
					"Complete all expense fields and enter an amount greater than zero.");
			return;
		}

		Date expenseDate;
		try {
			expenseDate = Date.valueOf(expenseDateValue);
		} catch (IllegalArgumentException exception) {
			redirectToEditForm(request, response, idValue, amountValue, category, description, expenseDateValue,
					"Enter a valid expense date.");
			return;
		}

		Expense expense = new Expense(userId, amount, category.trim(), description.trim(), expenseDate);
		expense.setId(id);

		try {
			ExpenseDAO expenseDAO = new ExpenseDAO();
			if (expenseDAO.updateExpense(expense)) {
				session.setAttribute("message", "Expense updated successfully.");
			} else {
				session.setAttribute("message", "Expense was not found or could not be updated.");
			}
			response.sendRedirect(request.getContextPath() + "/view-expenses");
		} catch (SQLException exception) {
			getServletContext().log("Unable to update expense.", exception);
			session.setAttribute("message", "The expense could not be updated. Please try again.");
			response.sendRedirect(request.getContextPath() + "/view-expenses");
		}
	}

	private void redirectToEditForm(HttpServletRequest request, HttpServletResponse response,
			String id, String amount, String category, String description, String expenseDate, String message)
			throws IOException {
		String editUrl = request.getContextPath() + "/edit-expense.html?id=" + encode(id)
				+ "&amount=" + encode(amount)
				+ "&category=" + encode(category)
				+ "&description=" + encode(description)
				+ "&expenseDate=" + encode(expenseDate);
		request.getSession().setAttribute("message", message);
		response.sendRedirect(editUrl);
	}

	private String encode(String value) {
		return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
	}
}