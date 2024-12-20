package server.filter;

import server.model.Transaction;

@FunctionalInterface
public interface TransactionFilterStrategy {
    boolean filter(Transaction transaction);
}
