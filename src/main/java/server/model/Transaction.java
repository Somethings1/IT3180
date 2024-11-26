package server.model;

import java.time.LocalDateTime;

import server.service.AccountService;
import server.service.CategoryService;
import server.utils.TimeUtils;

public class Transaction {
    private int id;
    private LocalDateTime dateTime;
    private double amount;
    private int sourceAccount;  
    private int category;      
    private int destinationAccount;
    private String note;
    private String type; 

    public Transaction() {
    }

    public Transaction(int id, LocalDateTime dateTime, double amount, int sourceAccount, int category, int destinationAccount, String note, String type) {
        this.id = id;
        this.dateTime = dateTime;
        this.amount = amount;
        this.sourceAccount = sourceAccount;
        this.category = category;
        this.destinationAccount = destinationAccount;
        this.note = note;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getDateTime() {
    	return this.dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getSourceAccount() {
        return sourceAccount;
    }

    public void setSourceAccount(int sourceAccount) {
        this.sourceAccount = sourceAccount;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getDestinationAccount() {
        return destinationAccount;
    }

    public void setDestinationAccount(int destinationAccount) {
        this.destinationAccount = destinationAccount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSourceAccountName() {
        AccountService acc = new AccountService();
        return acc.getAccount(sourceAccount).getName();
    }

    public String getCategoryName() {
    	if (getType().equals("Transfer")) return getType();
        CategoryService cat = new CategoryService();
        Category category = cat.getCategory(this.category);
        return category.getName();
    }

    public String getDestinationAccountName() {
    	if (!getType().equals("Transfer")) return null;
        AccountService acc = new AccountService();
        Account account = acc.getAccount(destinationAccount);
        return account.getName();
    }

    public Transaction prototype() {
        return new Transaction(
            this.id,
            this.dateTime,
            this.amount, 
            this.sourceAccount,
            this.category,    
            this.destinationAccount,
            this.note,             
            this.type             
        );
    }
    
    public static Transaction createDefault() {
    	Transaction transaction = new Transaction();
    	transaction.dateTime = LocalDateTime.now();
    	transaction.setAmount(0);
    	transaction.setCategory(0);
    	transaction.setSourceAccount(0);
    	transaction.setDestinationAccount(0);
    	transaction.setNote("");
    	transaction.setType("Expense");
    	return transaction;
    }
    
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Transaction other = (Transaction) obj;
		return id == other.id;
	}
}
