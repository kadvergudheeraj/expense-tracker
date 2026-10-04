<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	import="com.expensetracker.model.Expense, java.net.URLEncoder, java.nio.charset.StandardCharsets, java.text.NumberFormat, java.time.format.DateTimeFormatter, java.util.Collections, java.util.List, java.util.Locale" %>
<%!
	private String escapeHtml(String value) {
		if (value == null) {
			return "";
		}

		return value.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;")
				.replace("'", "&#39;");
	}

	private String encodeQueryValue(String value) {
		return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
	}
%>
<%
	// The servlet places the database results in the "expenses" request attribute.
	@SuppressWarnings("unchecked")
	List<Expense> expenses = (List<Expense>) request.getAttribute("expenses");
	if (expenses == null) {
		expenses = Collections.emptyList();
	}
	String message = null;
	if (session.getAttribute("message") instanceof String) {
		message = (String) session.getAttribute("message");
		session.removeAttribute("message");
	}

	NumberFormat amountFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN"));
	amountFormat.setMinimumFractionDigits(2);
	amountFormat.setMaximumFractionDigits(2);
	DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH);
%>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta name="description" content="View and filter your expenses in Expense Tracker.">
	<title>All Expenses | Expense Tracker</title>
	<link rel="stylesheet" href="css/style.css">
	<script src="script/script.js" defer></script>
</head>
<body>
	<div class="app-shell">
		<header class="topbar">
			<a class="brand" href="dashboard.html" aria-label="Expense Tracker home">
				<span class="brand-mark" aria-hidden="true">E</span>
				<span>Expense<span class="brand-light">Tracker</span></span>
			</a>

			<nav class="main-nav" aria-label="Main navigation">
				<a class="nav-link" href="dashboard.html">Dashboard</a>
				<a class="nav-link active" href="view-expenses" aria-current="page">Expenses</a>
				<a class="nav-link nav-add" href="add-expense.html"><span aria-hidden="true">+</span> Add Expense</a>
			</nav>

			<a class="logout-link" href="/expense-tracker/logout"><span aria-hidden="true">↗</span> Logout</a>
		</header>

		<main class="dashboard">
			<section class="page-heading expense-page-heading" aria-labelledby="page-title">
				<div>
					<p class="eyebrow">YOUR SPENDING HISTORY</p>
					<h1 id="page-title">All Expenses</h1>
					<p class="heading-note">Browse and review your recorded expenses.</p>
				</div>
				<a class="form-button form-button-primary" href="add-expense.html">+ Add Expense</a>
			</section>
			<% if (message != null) { %>
				<p class="heading-note" role="status"><%= escapeHtml(message) %></p>
			<% } %>

			<section class="panel expense-filters" aria-label="Expense filters">
				<div class="filter-grid">
					<div class="filter-field">
						<label for="expense-search">Search expenses</label>
						<input id="expense-search" type="search" placeholder="Search description or category">
					</div>
					<div class="filter-field">
						<label for="category-filter">Category</label>
						<select id="category-filter">
							<option>All Categories</option>
							<option>Food</option>
							<option>Travel</option>
							<option>Shopping</option>
							<option>Bills</option>
							<option>Entertainment</option>
							<option>Health</option>
							<option>Education</option>
							<option>Other</option>
						</select>
					</div>
					<div class="filter-field">
						<label for="date-filter">Date</label>
						<input id="date-filter" type="date">
					</div>
				</div>
			</section>

			<section class="panel expenses-panel" aria-labelledby="expenses-table-title">
				<div class="panel-heading">
					<div>
						<p class="eyebrow">TRANSACTION HISTORY</p>
						<h2 id="expenses-table-title">Expenses</h2>
					</div>
					<span class="record-count"><%= expenses.size() %> <%= expenses.size() == 1 ? "record" : "records" %></span>
				</div>
				<div class="table-wrap">
					<table class="expenses-table">
						<thead>
							<tr>
								<th scope="col">Category</th>
								<th scope="col">Description</th>
								<th scope="col">Amount</th>
								<th scope="col">Date</th>
								<th scope="col">Actions</th>
							</tr>
						</thead>
						<tbody>
							<% if (expenses.isEmpty()) { %>
								<tr>
									<td colspan="5">No expenses found.</td>
								</tr>
							<% } else {
								for (Expense expense : expenses) { %>
								<tr>
									<td><span class="category-label"><%= escapeHtml(expense.getCategory()) %></span></td>
									<td class="description-cell"><%= escapeHtml(expense.getDescription()) %></td>
									<td class="amount-cell">₹<%= amountFormat.format(expense.getAmount()) %></td>
									<td class="date-cell"><%= expense.getExpenseDate() == null ? "" : dateFormat.format(expense.getExpenseDate().toLocalDate()) %></td>
									<td class="row-actions">
										<a href="/expense-tracker/edit-expense.html?id=<%= expense.getId() %>&amp;amount=<%= encodeQueryValue(Double.toString(expense.getAmount())) %>&amp;category=<%= encodeQueryValue(expense.getCategory()) %>&amp;description=<%= encodeQueryValue(expense.getDescription()) %>&amp;expenseDate=<%= encodeQueryValue(expense.getExpenseDate() == null ? "" : expense.getExpenseDate().toString()) %>">Edit</a>
										<form class="delete-expense-form" action="/expense-tracker/delete-expense" method="post">
											<input type="hidden" name="id" value="<%= expense.getId() %>">
											<button type="submit">Delete</button>
										</form>
									</td>
								</tr>
							<% }
							} %>
						</tbody>
					</table>
				</div>
			</section>

			<footer class="page-footer">Expense Tracker <span>·</span> Your finances, at a glance</footer>
		</main>
	</div>
</body>
</html>
