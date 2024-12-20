package server.filter;

import server.model.Transaction;

import java.time.LocalDate;

public class DateFilterStrategy implements TransactionFilterStrategy {
	private final LocalDate fromDate;
	private final LocalDate toDate;
	
	public DateFilterStrategy (LocalDate fromDate, LocalDate toDate) {
		this.fromDate = fromDate;
		this.toDate = toDate;
	}
	
	@Override 
	public boolean filter (Transaction transaction) {
		return !(transaction.getDateTime().toLocalDate().isBefore(fromDate) || 
				 transaction.getDateTime().toLocalDate().isAfter(toDate));
	}
}
