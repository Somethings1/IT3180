package server.filter;

import server.model.Category;
import server.model.Transaction;

public class CategoryFilterStrategy implements TransactionFilterStrategy {
	private final Category category;
	
	public CategoryFilterStrategy (Category category) {
		this.category = category;
	}
	
	@Override
	public boolean filter (Transaction transaction) {
		return transaction.getCategory() == category.getId();
	}
}
