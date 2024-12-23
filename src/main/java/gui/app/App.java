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
	private final AccountService accountService;
	private final CategoryService categoryService;
	private final TransactionService transactionService;
	private int month, year;
	
	private App () {
		accountService = new AccountService();
		categoryService = new CategoryService();
		transactionService = new TransactionService();
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
		transactionList = transactionService.getTransactionsByMonth(month, year);
	}
	
	public int getMonth() {
		return month;
	}

	public int getYear() {
		return year;
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
	
	public boolean latestTime() {
        LocalDateTime now = LocalDateTime.now();
        return year == now.getYear() && month == now.getMonthValue();
    }

    public void prevTimeStamp() {
        if (month == 1) {
			month = 12;
			year--;
		} else {
			month--;
		}
        loadTransactionData();
    }

    public void nextTimeStamp() {
        LocalDateTime now = LocalDateTime.now();
		if (month == 12) {
			month = 1;
			year++;
		} else {
			month++;
		}
        loadTransactionData();
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
