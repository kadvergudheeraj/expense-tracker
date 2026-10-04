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
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		Object userIdValue = session == null ? null : session.getAttribute("userId");
		if (!(userIdValue instanceof Integer)) {
			response.sendRedirect(request.getContextPath() + "/login.html");
			return;
		}
		int userId = (Integer) userIdValue;

		try {
			ExpenseDAO expenseDAO = new ExpenseDAO();
			double totalExpenses = expenseDAO.getTotalExpensesByUser(userId);
			double currentMonthExpenses = expenseDAO.getCurrentMonthExpensesByUser(userId);
			int currentMonthExpenseCount = expenseDAO.getCurrentMonthExpenseCountByUser(userId);
			Expense highestExpense = expenseDAO.getHighestExpenseByUser(userId);
			Map<String, Double> categoryTotals = expenseDAO.getCurrentMonthCategoryTotalsByUser(userId);
			List<Expense> recentExpenses = expenseDAO.getRecentExpensesByUser(userId);

			request.setAttribute("totalExpenses", totalExpenses);
			request.setAttribute("currentMonthExpenses", currentMonthExpenses);
			request.setAttribute("currentMonthExpenseCount", currentMonthExpenseCount);
			request.setAttribute("highestExpense", highestExpense);
			request.setAttribute("categoryTotals", categoryTotals);
			request.setAttribute("recentExpenses", recentExpenses);
			request.setAttribute("monthLabel", YearMonth.now()
					.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)));

			request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
		} catch (SQLException exception) {
			getServletContext().log("Unable to load dashboard statistics.", exception);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Unable to load dashboard statistics.");
		}
	}
}