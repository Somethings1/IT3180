package server.filter;

import server.model.Transaction;

public class NoteFilterStrategy implements TransactionFilterStrategy {
	private final String note;
	
	public NoteFilterStrategy (String note) {
		this.note = note;
	}
	
	@Override
	public boolean filter (Transaction transaction) {
		return transaction.getNote().equalsIgnoreCase(note);
	}
}
