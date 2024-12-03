package server.service;

import gui.app.App;
import server.dao.AccountDAO;
import server.model.Account;

import java.util.List;

public class AccountService {
    private final AccountDAO accountDAO;

    public AccountService() {
        accountDAO = new AccountDAO();
    }

    public void addAccount(Account account) throws Exception {
    	assertAccount(account);
        accountDAO.insert(account);
        App.getInstance().loadAccountData();
    }

    public void updateAccount(Account account) throws Exception {
    	assertAccount(account);
        accountDAO.update(account);
        App.getInstance().loadAccountData();
    }

    public void removeAccount(int accountId) {
        accountDAO.delete(accountId);
        App.getInstance().loadAccountData();
    }

    public List<Account> getAllAccounts() {
        return accountDAO.findAll();
    }
    
    public Account getAccount (int id) {
    	return accountDAO.findAccount(id);
    }
    
    public void adjustBalance (int id, double amount) {
    	accountDAO.adjustAccountBalance(id, amount);
    	App.getInstance().loadAccountData();
    }
    
    public double getBalance (int id, int year, int month) {
    	return accountDAO.getBalanceAtEndOfMonth(id, month, year);
    }
    
    private void assertAccount(Account account) throws Exception {
    	if (account.getName().isEmpty()) 
    		throw new Exception("Account/Saving name cannot be null.");
    	if (account.getGroup().equals("Saving") && account.getGoal() == 0)
    		throw new Exception("Saving goal is not set.");
    }
}
