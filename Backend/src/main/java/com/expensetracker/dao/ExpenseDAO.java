package com.expensetracker.dao;

import com.expensetracker.model.Expense;
import com.expensetracker.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExpenseDAO {
	public boolean addExpense(Expense expense) throws SQLException {
		String sql = "INSERT INTO expenses " +
				"(user_id, amount, category, description, expense_date) " +
				"VALUES (?, ?, ?, ?, ?)";

		// Bind each value to the prepared statement before executing the insert.
		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, expense.getUserId());
			statement.setDouble(2, expense.getAmount());
			statement.setString(3, expense.getCategory());
			statement.setString(4, expense.getDescription());
			statement.setDate(5, expense.getExpenseDate());

			return statement.executeUpdate() > 0;
		}
	}

	public List<Expense> getExpensesByUser(int userId) throws SQLException {
		String sql = "SELECT id, user_id, amount, category, description, expense_date " +
				"FROM expenses WHERE user_id = ? " +
				"ORDER BY expense_date DESC, id DESC";
		List<Expense> expenses = new ArrayList<>();

		// Read all matching rows into a list; no matches leave the list empty.
		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, userId);

			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					expenses.add(mapExpense(resultSet));
				}
			}
		}

		return expenses;
	}

	public Expense getExpenseById(int id, int userId) throws SQLException {
		String sql = "SELECT id, user_id, amount, category, description, expense_date " +
				"FROM expenses WHERE id = ? AND user_id = ?";

		// Return the matching row, or null when the user does not own that expense.
		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, id);
			statement.setInt(2, userId);

			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return mapExpense(resultSet);
				}
			}
		}

		return null;
	}

	public boolean updateExpense(Expense expense) throws SQLException {
		String sql = "UPDATE expenses " +
				"SET amount = ?, category = ?, description = ?, expense_date = ? " +
				"WHERE id = ? AND user_id = ?";

		// Update only the expense belonging to the given user.
		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setDouble(1, expense.getAmount());
			statement.setString(2, expense.getCategory());
			statement.setString(3, expense.getDescription());
			statement.setDate(4, expense.getExpenseDate());
			statement.setInt(5, expense.getId());
			statement.setInt(6, expense.getUserId());

			return statement.executeUpdate() > 0;
		}
	}

	public boolean deleteExpense(int id, int userId) throws SQLException {
		String sql = "DELETE FROM expenses WHERE id = ? AND user_id = ?";

		// Delete only the expense belonging to the given user.
		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, id);
			statement.setInt(2, userId);

			return statement.executeUpdate() > 0;
		}
	}

	public double getTotalExpensesByUser(int userId) throws SQLException {
		String sql = "SELECT COALESCE(SUM(amount), 0) AS total_amount " +
				"FROM expenses WHERE user_id = ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, userId);

			try (ResultSet resultSet = statement.executeQuery()) {
				return resultSet.next() ? resultSet.getDouble("total_amount") : 0.0;
			}
		}
	}

	public double getCurrentMonthExpensesByUser(int userId) throws SQLException {
		String sql = "SELECT COALESCE(SUM(amount), 0) AS total_amount " +
				"FROM expenses WHERE user_id = ? AND expense_date >= ? AND expense_date < ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, userId);
			setCurrentMonthRange(statement, 2);

			try (ResultSet resultSet = statement.executeQuery()) {
				return resultSet.next() ? resultSet.getDouble("total_amount") : 0.0;
			}
		}
	}

	public int getCurrentMonthExpenseCountByUser(int userId) throws SQLException {
		String sql = "SELECT COUNT(*) AS expense_count FROM expenses " +
				"WHERE user_id = ? AND expense_date >= ? AND expense_date < ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, userId);
			setCurrentMonthRange(statement, 2);

			try (ResultSet resultSet = statement.executeQuery()) {
				return resultSet.next() ? resultSet.getInt("expense_count") : 0;
			}
		}
	}

	public Expense getHighestExpenseByUser(int userId) throws SQLException {
		String sql = "SELECT id, user_id, amount, category, description, expense_date " +
				"FROM expenses WHERE user_id = ? " +
				"ORDER BY amount DESC, expense_date DESC, id DESC LIMIT 1";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, userId);

			try (ResultSet resultSet = statement.executeQuery()) {
				return resultSet.next() ? mapExpense(resultSet) : null;
			}
		}
	}

	public Map<String, Double> getCurrentMonthCategoryTotalsByUser(int userId) throws SQLException {
		String sql = "SELECT category, SUM(amount) AS category_total FROM expenses " +
				"WHERE user_id = ? AND expense_date >= ? AND expense_date < ? " +
				"GROUP BY category ORDER BY category_total DESC";
		Map<String, Double> categoryTotals = new LinkedHashMap<>();

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, userId);
			setCurrentMonthRange(statement, 2);

			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					categoryTotals.put(resultSet.getString("category"), resultSet.getDouble("category_total"));
				}
			}
		}

		return categoryTotals;
	}

	public List<Expense> getRecentExpensesByUser(int userId) throws SQLException {
		String sql = "SELECT id, user_id, amount, category, description, expense_date " +
				"FROM expenses WHERE user_id = ? " +
				"ORDER BY expense_date DESC, id DESC LIMIT 5";
		List<Expense> expenses = new ArrayList<>();

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, userId);

			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					expenses.add(mapExpense(resultSet));
				}
			}
		}

		return expenses;
	}

	private void setCurrentMonthRange(PreparedStatement statement, int firstDateParameter) throws SQLException {
		LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
		statement.setDate(firstDateParameter, Date.valueOf(firstDayOfMonth));
		statement.setDate(firstDateParameter + 1, Date.valueOf(firstDayOfMonth.plusMonths(1)));
	}

	private Expense mapExpense(ResultSet resultSet) throws SQLException {
		Expense expense = new Expense();
		expense.setId(resultSet.getInt("id"));
		expense.setUserId(resultSet.getInt("user_id"));
		expense.setAmount(resultSet.getDouble("amount"));
		expense.setCategory(resultSet.getString("category"));
		expense.setDescription(resultSet.getString("description"));
		expense.setExpenseDate(resultSet.getDate("expense_date"));
		return expense;
	}
}