package com.expensetracker.model;

import java.sql.Date;

public class Expense {
	private int id;
	private int userId;
	private double amount;
	private String category;
	private String description;
	private Date expenseDate;

	public Expense() {
	}

	public Expense(int userId, double amount, String category, String description, Date expenseDate) {
		this.userId = userId;
		this.amount = amount;
		this.category = category;
		this.description = description;
		this.expenseDate = expenseDate;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getExpenseDate() {
		return expenseDate;
	}

	public void setExpenseDate(Date expenseDate) {
		this.expenseDate = expenseDate;
	}

	@Override
	public String toString() {
		return "Expense{" +
				"id=" + id +
				", userId=" + userId +
				", amount=" + amount +
				", category='" + category + '\'' +
				", description='" + description + '\'' +
				", expenseDate=" + expenseDate +
				'}';
	}
}