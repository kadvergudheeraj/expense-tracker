// Use the labels already present in the HTML to explain required-field errors.
function getFieldLabel(field) {
	const label = field.labels && field.labels[0];
	return label ? label.textContent.replace('*', '').trim() : 'This field';
}

function setRequiredMessage(field) {
	const isPassword = field.type === 'password';
	const isEmpty = isPassword ? field.value.length === 0 : field.value.trim() === '';
	// Custom validity keeps the browser's validation popup but makes its message specific.
	field.setCustomValidity(isEmpty ? `${getFieldLabel(field)} is required.` : '');
	return !isEmpty;
}

function setupForm(form, validateForm, allowValidSubmit = false) {
	// Run custom validation even though browser-native submit validation is disabled.
	form.noValidate = true;

	form.addEventListener('input', validateForm);
	form.addEventListener('change', validateForm);
	form.addEventListener('submit', (event) => {
		if (!validateForm()) {
			event.preventDefault();
			form.reportValidity();
		} else if (!allowValidSubmit) {
			event.preventDefault();
		}
	});
}

function setupAuthForms() {
	document.querySelectorAll('.auth-form').forEach((form) => {
		const email = form.querySelector('input[type="email"]');
		const password = form.querySelector('input[type="password"]:not([id="confirm-password"])');
		const confirmPassword = form.querySelector('#confirm-password');
		const requiredFields = form.querySelectorAll('[required]');

		function validateAuthForm() {
			requiredFields.forEach((field) => field.setCustomValidity(''));
			requiredFields.forEach(setRequiredMessage);

			if (email.value.trim() && email.validity.typeMismatch) {
				email.setCustomValidity('Please enter a valid email address.');
			}

			if (password.value && password.minLength > 0 && password.value.length < password.minLength) {
				password.setCustomValidity(`Password must be at least ${password.minLength} characters long.`);
			}

			if (confirmPassword && confirmPassword.value && password.value !== confirmPassword.value) {
				confirmPassword.setCustomValidity('The passwords do not match.');
			}

			return form.checkValidity();
		}

		setupForm(form, validateAuthForm, true);
	});
}

function setupExpenseForm() {
	const form = document.querySelector('.expense-form');
	if (!form) return;

	const amount = form.querySelector('#amount');
	const category = form.querySelector('#category');
	const description = form.querySelector('#description');
	const date = form.querySelector('#expense-date');

	function validateExpenseForm() {
		[amount, category, description, date].forEach((field) => field.setCustomValidity(''));

		setRequiredMessage(amount);
		setRequiredMessage(category);
		setRequiredMessage(date);

		if (amount.value && Number(amount.value) <= 0) {
			amount.setCustomValidity('Amount must be greater than 0.');
		}
		if (!category.value) {
			category.setCustomValidity('Please select a category.');
		}
		if (!description.value.trim()) {
			description.setCustomValidity('Please enter a description.');
		}
		if (!date.value) {
			date.setCustomValidity('Please select an expense date.');
		}

		return form.checkValidity();
	}

	setupForm(form, validateExpenseForm, form.dataset.allowSubmit === 'true');
}

function setupExpenseEditForm() {
	const form = document.querySelector('.edit-expense-form');
	if (!form) return;

	const parameters = new URLSearchParams(window.location.search);
	form.querySelector('#expense-id').value = parameters.get('id') || '';
	form.querySelector('#amount').value = parameters.get('amount') || '';
	form.querySelector('#category').value = (parameters.get('category') || '').toLowerCase();
	form.querySelector('#description').value = parameters.get('description') || '';
	form.querySelector('#expense-date').value = parameters.get('expenseDate') || '';
}

function setupExpenseFilters() {
	const search = document.querySelector('#expense-search');
	const categoryFilter = document.querySelector('#category-filter');
	const dateFilter = document.querySelector('#date-filter');
	const tableBody = document.querySelector('.expenses-table tbody');
	const recordCount = document.querySelector('.record-count');

	if (!search || !categoryFilter || !dateFilter || !tableBody) return;

	function filterExpenses() {
		const searchText = search.value.trim().toLowerCase();
		const selectedCategory = categoryFilter.value.toLowerCase();
		const selectedDate = dateFilter.value;
		let visibleCount = 0;

		// Apply every active filter to each existing row; this does not fetch or store data.
		tableBody.querySelectorAll('tr').forEach((row) => {
			const descriptionCell = row.querySelector('.description-cell');
			const categoryLabel = row.querySelector('.category-label');
			const dateCell = row.querySelector('.date-cell');
			if (!descriptionCell || !categoryLabel || !dateCell) return;

			const description = descriptionCell.textContent.toLowerCase();
			const category = categoryLabel.textContent.trim().toLowerCase();
			const dateText = dateCell.textContent.trim();
			const rowDate = new Date(dateText);
			const normalizedRowDate = Number.isNaN(rowDate.getTime())
				? ''
				: `${rowDate.getFullYear()}-${String(rowDate.getMonth() + 1).padStart(2, '0')}-${String(rowDate.getDate()).padStart(2, '0')}`;

			const matchesSearch = description.includes(searchText) || category.includes(searchText);
			const matchesCategory = selectedCategory === 'all categories' || category === selectedCategory;
			const matchesDate = !selectedDate || normalizedRowDate === selectedDate;
			const isVisible = matchesSearch && matchesCategory && matchesDate;

			row.hidden = !isVisible;
			if (isVisible) visibleCount += 1;
		});

		if (recordCount) {
			recordCount.textContent = `${visibleCount} ${visibleCount === 1 ? 'record' : 'records'}`;
		}
	}

	search.addEventListener('input', filterExpenses);
	categoryFilter.addEventListener('change', filterExpenses);
	dateFilter.addEventListener('change', filterExpenses);

	// Only remove the row after the user confirms the action.
	tableBody.addEventListener('click', (event) => {
		const button = event.target.closest('.row-actions button');
		if (!button || !button.textContent.trim().toLowerCase().includes('delete')) return;
		if (button.closest('.delete-expense-form')) return;

		const row = button.closest('tr');
		const description = row.querySelector('.description-cell').textContent.trim();
		if (window.confirm(`Delete the expense "${description}"?`)) {
			row.remove();
			filterExpenses();
		}
	});

	tableBody.addEventListener('submit', (event) => {
		const form = event.target.closest('.delete-expense-form');
		if (!form) return;

		const description = form.closest('tr').querySelector('.description-cell').textContent.trim();
		if (!window.confirm(`Delete the expense "${description}"?`)) {
			event.preventDefault();
		}
	});

	filterExpenses();
}

document.addEventListener('DOMContentLoaded', () => {
	setupAuthForms();
	setupExpenseEditForm();
	setupExpenseForm();
	setupExpenseFilters();
});
