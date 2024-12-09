package gui.app;

import java.time.LocalDateTime;
import java.util.List;

import server.model.Account;
import server.model.Category;
import server.model.Transaction;
import server.service.AccountService;
import server.service.CategoryService;
import server.service.TransactionService;

public class App {
	private static App instance;
	private List<Transaction> transactionList;
	private List<Account> accountList;
	private List<Category> incomeCategoryList, expenseCategoryList;
	private AccountService accountService;
	private CategoryService categoryService;
	private TransactionService transactionService;
	private boolean monthView;
	private int month, year;
	
	private App () {
		accountService = new AccountService();
		categoryService = new CategoryService();
		transactionService = new TransactionService();
		
		monthView = true;
		month = LocalDateTime.now().getMonthValue();
		year = LocalDateTime.now().getYear();
		
		reload();
	}
	
	public void reload() {
		loadAccountData();
		loadCategoryData();
		loadTransactionData();
	}
	
	public void loadAccountData () {
		accountList = accountService.getAllAccounts();
	}
	
	public void loadCategoryData () {
		incomeCategoryList = categoryService.getAllIncomeCategories();
		expenseCategoryList = categoryService.getAllExpenseCategories();
	}
	
	public void loadTransactionData () {
		if (isMonthView())
			transactionList = transactionService.getTransactionsByMonth(month, year);
		else 
			transactionList = transactionService.getTransactionsByYear(year);
	}
	
	/*
	 * PUBLIC METHODS
	*/
	
	public boolean isMonthView () {
		return monthView;
	}
	
	public int getMonth() {
		return month;
	}

	public void setMonth(int month, int year) {
		this.month = month;
		this.year = year;
		loadTransactionData();
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
		loadTransactionData();
	}

	public List<Transaction> getTransactionList() {
		return transactionList;
	}

	public List<Account> getAccountList() {
		return accountList;
	}

	public List<Category> getIncomeCategoryList() {
		return incomeCategoryList;
	}

	public List<Category> getExpenseCategoryList() {
		return expenseCategoryList;
	}

	public void setMonthView(boolean monthView) {
		this.monthView = monthView;
		if (!monthView) {
			month = 12;
		}
		loadTransactionData();
	}
	
	public boolean latestTime() {
        LocalDateTime now = LocalDateTime.now();
        if (isMonthView()) {
            return year == now.getYear() && month == now.getMonthValue();
        } else {
            return year == now.getYear();
        }
    }

    public void prevTimeStamp() {
        if (isMonthView()) {
            if (month == 1) { // If current month is January
                month = 12; // Set month to December
                year--; // Decrease the year
            } else {
                month--; // Just decrease the month
            }
        } else {
            year--; // Decrease the year in year view
        }
        loadTransactionData(); // Reload transaction data
    }

    public void nextTimeStamp() {
        LocalDateTime now = LocalDateTime.now();
        if (isMonthView()) {
            if (month == 12) { // If current month is December
                month = 1; // Set month to January
                year++; // Increase the year
            } else {
                month++; // Just increase the month
            }
        } else {
            if (year < now.getYear()) {
                year++; // Increase the year in year view
            }
        }
        loadTransactionData(); // Reload transaction data
    }

	public static App getInstance () {
		if (instance == null)
			instance = new App();
		return instance;
	}

	public List<Category> getCategoryList() {
		return categoryService.getAllCategories();
	}
}
