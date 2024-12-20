package server.filter;

import server.model.Transaction;

public class AmountFilterStrategy implements TransactionFilterStrategy {
	private final double fromAmount;
	private final double toAmount;
	
	public AmountFilterStrategy (double fromAmount, double toAmount) {
		this.fromAmount = fromAmount;
		this.toAmount = toAmount;
	}
	
	@Override 
	public boolean filter (Transaction transaction) {
		return transaction.getAmount() >= fromAmount && transaction.getAmount() <= toAmount;
	}
}
