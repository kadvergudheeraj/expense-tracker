package com.expensetracker.dao;

import com.expensetracker.model.User;
import com.expensetracker.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
	public boolean registerUser(User user) throws SQLException {
		String sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, user.getName());
			statement.setString(2, user.getEmail());
			statement.setString(3, user.getPassword());

			return statement.executeUpdate() > 0;
		}
	}

	public User loginUser(String email, String password) throws SQLException {
		String sql = "SELECT id, name, email FROM users WHERE email = ? AND password = ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, email);
			statement.setString(2, password);

			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					User user = new User();
					user.setId(resultSet.getInt("id"));
					user.setName(resultSet.getString("name"));
					user.setEmail(resultSet.getString("email"));
					return user;
				}
			}
		}

		return null;
	}
}