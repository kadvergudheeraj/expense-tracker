<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	import="com.expensetracker.model.Expense, java.text.NumberFormat, java.time.format.DateTimeFormatter, java.util.Collections, java.util.List, java.util.Locale, java.util.Map" %>
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

	private String categoryTone(String category) {
		if (category == null) {
			return "health";
		}

		switch (category.toLowerCase(Locale.ROOT)) {
			case "food":
				return "food";
			case "travel":
			case "transport":
				return "transport";
			case "home":
			case "bills":
				return "home";
			case "entertainment":
			case "leisure":
				return "leisure";
			default:
				return "health";
		}
	}
%>
<%
	Number totalValue = (Number) request.getAttribute("totalExpenses");
	Number currentMonthValue = (Number) request.getAttribute("currentMonthExpenses");
	Number expenseCountValue = (Number) request.getAttribute("currentMonthExpenseCount");
	double totalExpenses = totalValue == null ? 0.0 : totalValue.doubleValue();
	double currentMonthExpenses = currentMonthValue == null ? 0.0 : currentMonthValue.doubleValue();
	int currentMonthExpenseCount = expenseCountValue == null ? 0 : expenseCountValue.intValue();
	Expense highestExpense = (Expense) request.getAttribute("highestExpense");

	@SuppressWarnings("unchecked")
	Map<String, Double> categoryTotals = (Map<String, Double>) request.getAttribute("categoryTotals");
	if (categoryTotals == null) {
		categoryTotals = Collections.emptyMap();
	}

	@SuppressWarnings("unchecked")
	List<Expense> recentExpenses = (List<Expense>) request.getAttribute("recentExpenses");
	if (recentExpenses == null) {
		recentExpenses = Collections.emptyList();
	}

	String monthLabel = (String) request.getAttribute("monthLabel");
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
	<meta name="description" content="Review your spending and recent transactions in Expense Tracker.">
	<title>Dashboard | Expense Tracker</title>
	<link rel="stylesheet" href="css/style.css">
</head>
<body>
	<div class="app-shell">
		<header class="topbar">
			<a class="brand" href="dashboard" aria-label="Expense Tracker home">
				<span class="brand-mark" aria-hidden="true">E</span>
				<span>Expense<span class="brand-light">Tracker</span></span>
			</a>

			<nav class="main-nav" aria-label="Main navigation">
				<a class="nav-link active" href="dashboard" aria-current="page">Dashboard</a>
				<a class="nav-link" href="view-expenses">Expenses</a>
				<a class="nav-link nav-add" href="add-expense.html"><span aria-hidden="true">+</span> Add Expense</a>
			</nav>

			<a class="logout-link" href="/expense-tracker/logout"><span aria-hidden="true">↗</span> Logout</a>
		</header>

		<main class="dashboard">
			<section class="page-heading" aria-labelledby="page-title">
				<div>
					<p class="eyebrow">YOUR MONEY, IN FOCUS</p>
					<h1 id="page-title">Dashboard</h1>
					<p class="heading-note">A clear view of where your money is going.</p>
				</div>
				<p class="date-label"><span class="date-dot" aria-hidden="true"></span> <%= escapeHtml(monthLabel) %></p>
			</section>

			<section class="summary-grid" aria-label="Expense summary">
				<article class="summary-card total-card">
					<div class="card-topline"><span class="card-label">Total expenses</span><span class="card-icon icon-green" aria-hidden="true">₹</span></div>
					<p class="card-value">₹<%= amountFormat.format(totalExpenses) %></p>
					<p class="card-footnote">Across all recorded expenses</p>
				</article>
				<article class="summary-card">
					<div class="card-topline"><span class="card-label">This month</span><span class="card-icon icon-blue" aria-hidden="true">◷</span></div>
					<p class="card-value">₹<%= amountFormat.format(currentMonthExpenses) %></p>
					<p class="card-footnote"><%= escapeHtml(monthLabel) %></p>
				</article>
				<article class="summary-card">
					<div class="card-topline"><span class="card-label">Transactions</span><span class="card-icon icon-orange" aria-hidden="true">#</span></div>
					<p class="card-value"><%= currentMonthExpenseCount %></p>
					<p class="card-footnote">Recorded in <%= escapeHtml(monthLabel) %></p>
				</article>
				<article class="summary-card">
					<div class="card-topline"><span class="card-label">Highest expense</span><span class="card-icon icon-coral" aria-hidden="true">↗</span></div>
					<p class="card-value">₹<%= amountFormat.format(highestExpense == null ? 0.0 : highestExpense.getAmount()) %></p>
					<p class="card-footnote">
						<% if (highestExpense == null) { %>
							No expenses recorded
						<% } else { %>
							<%= escapeHtml(highestExpense.getCategory()) %>
							<% if (highestExpense.getExpenseDate() != null) { %>
								<span class="footnote-separator">·</span> <%= dateFormat.format(highestExpense.getExpenseDate().toLocalDate()) %>
							<% } %>
						<% } %>
					</p>
				</article>
			</section>

			<section class="content-grid" aria-label="Spending details">
				<section class="panel recent-panel" aria-labelledby="recent-title">
					<div class="panel-heading">
						<div>
							<p class="eyebrow">LATEST ACTIVITY</p>
							<h2 id="recent-title">Recent expenses</h2>
						</div>
						<a class="text-link" href="view-expenses">View all <span aria-hidden="true">→</span></a>
					</div>
					<div class="table-wrap">
						<table>
							<thead>
								<tr>
									<th scope="col">Category</th>
									<th scope="col">Description</th>
									<th scope="col">Amount</th>
									<th scope="col">Date</th>
									<th scope="col" class="actions-heading"><span class="visually-hidden">Actions</span></th>
								</tr>
							</thead>
							<tbody>
								<% if (recentExpenses.isEmpty()) { %>
									<tr><td colspan="5">No expenses found.</td></tr>
								<% } else {
									for (Expense expense : recentExpenses) { %>
									<tr>
										<td><span class="category-label"><span class="category-dot dot-<%= categoryTone(expense.getCategory()) %>"></span><%= escapeHtml(expense.getCategory()) %></span></td>
										<td class="description-cell"><%= escapeHtml(expense.getDescription()) %></td>
										<td class="amount-cell">₹<%= amountFormat.format(expense.getAmount()) %></td>
										<td class="date-cell"><%= expense.getExpenseDate() == null ? "" : dateFormat.format(expense.getExpenseDate().toLocalDate()) %></td>
										<td class="row-actions">&mdash;</td>
									</tr>
								<% }
								} %>
							</tbody>
						</table>
					</div>
				</section>

				<section class="panel category-panel" aria-labelledby="category-title">
					<div class="panel-heading">
						<div>
							<p class="eyebrow">SPENDING BREAKDOWN</p>
							<h2 id="category-title">By category</h2>
						</div>
						<span class="period-label"><%= escapeHtml(monthLabel) %></span>
					</div>
					<p class="category-total">₹<%= amountFormat.format(currentMonthExpenses) %> <span>total</span></p>
					<div class="category-list">
						<% for (Map.Entry<String, Double> category : categoryTotals.entrySet()) {
							double categoryAmount = category.getValue();
							int percentage = currentMonthExpenses <= 0 ? 0
									: (int) Math.round(categoryAmount * 100.0 / currentMonthExpenses);
							String tone = categoryTone(category.getKey()); %>
							<div class="category-item">
								<div class="category-line"><span><i class="category-dot dot-<%= tone %>"></i><%= escapeHtml(category.getKey()) %></span><span>₹<%= amountFormat.format(categoryAmount) %> <small><%= percentage %>%</small></span></div>
								<div class="progress-track"><span class="progress-fill fill-<%= tone %>" style="width: <%= percentage %>%"></span></div>
							</div>
						<% } %>
					</div>
					<p class="category-note">
						<% if (categoryTotals.isEmpty()) { %>
							No category totals for this month.
						<% } else { %>
							<span aria-hidden="true">✦</span> <%= escapeHtml(categoryTotals.keySet().iterator().next()) %> is your largest category this month.
						<% } %>
					</p>
				</section>
			</section>

			<footer class="page-footer">Expense Tracker <span>·</span> Your finances, at a glance</footer>
		</main>
	</div>
</body>
</html>