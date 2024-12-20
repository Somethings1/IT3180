package server.filter;

import server.model.Account;
import server.model.Transaction;

public class AccountFilterStrategy implements TransactionFilterStrategy {
	private final Account account;
	
	public AccountFilterStrategy (Account account) {
		this.account = account;
	}
	
	@Override 
	public boolean filter (Transaction transaction) {
		return transaction.getDestinationAccount() == account.getId() ||
			   transaction.getSourceAccount() == account.getId();
	}
}
