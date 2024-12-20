package server.filter;

import server.model.Transaction;

import java.util.ArrayList;
import java.util.List;

public class TransactionFilter {
    private final List<Transaction> transactions;
    private final List<TransactionFilterStrategy> filterStrategies = new ArrayList<>();

    public TransactionFilter(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void addFilterStrategy(TransactionFilterStrategy strategy) {
        filterStrategies.add(strategy);
    }

    public List<Transaction> applyFilters() {
        List<Transaction> filteredTransactions = new ArrayList<>(transactions);
        for (TransactionFilterStrategy strategy : filterStrategies) {
            filteredTransactions.removeIf(transaction -> !strategy.filter(transaction));
        }
        return filteredTransactions;
    }
}
