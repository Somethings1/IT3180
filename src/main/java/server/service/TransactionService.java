package server.service;

import gui.app.App;
import server.dao.TransactionDAO;
import server.model.Transaction;

import java.util.List;


public class TransactionService {
    private final TransactionDAO transactionDAO;

    public TransactionService() {
        transactionDAO = new TransactionDAO();
    }

    public void addTransaction(Transaction transaction) throws Exception {
    	assertTransaction(transaction);
        transactionDAO.insert(transaction);
        App.getInstance().loadTransactionData();
    }

    public void updateTransaction(Transaction transaction) throws Exception {
    	assertTransaction(transaction);
        transactionDAO.update(transaction);
        App.getInstance().loadTransactionData();
    }

    public void removeTransaction(int transactionId) {
        transactionDAO.remove(transactionId);
        App.getInstance().loadTransactionData();
    }

    public Transaction getTransaction(int id) {
        return transactionDAO.findById(id);
    }
    
    public List<Transaction> getTransactionsByMonth (int month, int year) {
        return transactionDAO.findByMonth(month, year);
    }
    
    public List<Transaction> getAllTransactions () {
    	return transactionDAO.getAllTransactions();
    }
    
    private void assertTransaction (Transaction transaction) throws Exception {
    	if (transaction.getAmount() < 0) 
    		throw new Exception("Amount cannot be negative");
    	if (transaction.getSourceAccount() == 0) 
    		throw new Exception("Source account cannot be null");
    	if (transaction.getType().equals("Transfer") && transaction.getDestinationAccount() == 0) 
    		throw new Exception("Destination account cannot be null");
    	if (!transaction.getType().equals("Transfer") && transaction.getCategory() == 0)
    		throw new Exception("Category cannot be null");
    }
}
