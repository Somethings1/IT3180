package gui.components.chart.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import server.model.Account;
import server.model.Transaction;

public class GraphDataConverter {

    public static List<GraphDataPoint> convertToIncome(List<Transaction> transactions) {
        return convertToGraphDataPoints(transactions, "Income");
    }

    public static List<GraphDataPoint> convertToExpense(List<Transaction> transactions) {
        return convertToGraphDataPoints(transactions, "Expense");
    }

    public static List<GraphDataPoint> convertToBalance(List<Transaction> transactions, double endOfMonthBalance) {
        List<GraphDataPoint> dataPoints = new ArrayList<>();
        Map<LocalDate, Double> dailyTotals = calculateDailyTotals(transactions);

        // Determine the date range from the first day of the first transaction's month to the last transaction date
        LocalDate lastDate = dailyTotals.keySet().stream().max(LocalDate::compareTo).orElse(LocalDate.now());
        LocalDate firstTransactionDate = dailyTotals.keySet().stream().min(LocalDate::compareTo).orElse(LocalDate.now());
        LocalDate firstDate = firstTransactionDate.withDayOfMonth(1); // Start from the first day of the month

        double cumulativeBalance = endOfMonthBalance;

        // Loop from the last date back to the first date
        for (LocalDate date = lastDate; !date.isBefore(firstDate); date = date.minusDays(1)) {
            // Adjust balance if there's a transaction on this date, otherwise keep it the same
            double dailyTotal = dailyTotals.getOrDefault(date, 0.0);
            cumulativeBalance -= dailyTotal;

            // Insert at the beginning to maintain chronological order
            dataPoints.add(0, new GraphDataPoint(date, cumulativeBalance));
        }

        return dataPoints;
    }
    
    public static List<GraphDataPoint> convertToAccountBalance(List<Transaction> transactions, Account account) {
        List<GraphDataPoint> dataPoints = new ArrayList<>();
        double endOfMonthBalance = account.getBalance(); // Get the current balance for the account
        Map<LocalDate, Double> dailyTotals = calculateAccountDailyTotals(transactions, account);

        // Determine the date range from the first day of the current month to today
        LocalDate today = LocalDate.now();
        LocalDate firstDate = today.withDayOfMonth(1); // Start from the first day of the current month

        double cumulativeBalance = endOfMonthBalance;

        // Loop from today back to the first date of the month
        for (LocalDate date = today; !date.isBefore(firstDate); date = date.minusDays(1)) {
            double dailyTotal = dailyTotals.getOrDefault(date, 0.0);
            dataPoints.add(0, new GraphDataPoint(date, cumulativeBalance));
            cumulativeBalance -= dailyTotal;
        }

        return dataPoints;
    }

    public static List<GraphDataPoint> convertToAccountIncome(List<Transaction> transactions, Account account) {
        List<GraphDataPoint> dataPoints = new ArrayList<>();
        Map<LocalDate, Double> dailyTotals = calculateAccountIncomeTotals(transactions, account);

        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        for (LocalDate date = firstDayOfMonth; !date.isAfter(today); date = date.plusDays(1)) {
        	dataPoints.add(new GraphDataPoint(date, dailyTotals.containsKey(date) ? dailyTotals.get(date) : 0));
        }
        
        return dataPoints;
    }

    public static List<GraphDataPoint> convertToAccountExpense(List<Transaction> transactions, Account account) {
        List<GraphDataPoint> dataPoints = new ArrayList<>();
        Map<LocalDate, Double> dailyTotals = calculateAccountExpenseTotals(transactions, account);

        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        for (LocalDate date = firstDayOfMonth; !date.isAfter(today); date = date.plusDays(1)) {
        	dataPoints.add(new GraphDataPoint(date, dailyTotals.containsKey(date) ? dailyTotals.get(date) : 0));
        }
        
        return dataPoints;
    }

    // Private methods to calculate daily totals for specific accounts

    private static Map<LocalDate, Double> calculateAccountDailyTotals(List<Transaction> transactions, Account account) {
        Map<LocalDate, Double> dailyTotals = new TreeMap<>();
        for (Transaction transaction : transactions) {
            LocalDate date = transaction.getDateTime().toLocalDate();
            double amount = transaction.getAmount();

            if (transaction.getType().equalsIgnoreCase("Income") && transaction.getSourceAccount() == account.getId()) {
                dailyTotals.put(date, dailyTotals.getOrDefault(date, 0.0) + amount);
            } else if (transaction.getType().equalsIgnoreCase("Expense") && transaction.getSourceAccount() == account.getId()) {
                dailyTotals.put(date, dailyTotals.getOrDefault(date, 0.0) - amount);
            } else if (transaction.getType().equalsIgnoreCase("Transfer")) {
                // Handle transfer logic
                if (transaction.getSourceAccount() == account.getId()) {
                    // Treat transfer as an expense
                    dailyTotals.put(date, dailyTotals.getOrDefault(date, 0.0) - amount);
                } else if (transaction.getDestinationAccount() == account.getId()) {
                    // Treat transfer as income
                    dailyTotals.put(date, dailyTotals.getOrDefault(date, 0.0) + amount);
                }
            }
        }
        return dailyTotals;
    }

    private static Map<LocalDate, Double> calculateAccountIncomeTotals(List<Transaction> transactions, Account account) {
        Map<LocalDate, Double> incomeTotals = new TreeMap<>();
        for (Transaction transaction : transactions) {
            LocalDate date = transaction.getDateTime().toLocalDate();

            if (transaction.getType().equalsIgnoreCase("Income") && transaction.getSourceAccount() == account.getId()) {
                incomeTotals.put(date, incomeTotals.getOrDefault(date, 0.0) + transaction.getAmount());
            } else if (transaction.getType().equalsIgnoreCase("Transfer") && transaction.getDestinationAccount() == account.getId()) {
                incomeTotals.put(date, incomeTotals.getOrDefault(date, 0.0) + transaction.getAmount());
            }
        }
        return incomeTotals;
    }
    
    private static Map<LocalDate, Double> calculateAccountExpenseTotals(List<Transaction> transactions, Account account) {
        Map<LocalDate, Double> expenseTotals = new TreeMap<>();
        for (Transaction transaction : transactions) {
            LocalDate date = transaction.getDateTime().toLocalDate();

            if (transaction.getType().equalsIgnoreCase("Expense") && transaction.getSourceAccount() == account.getId()) {
                expenseTotals.put(date, expenseTotals.getOrDefault(date, 0.0) + transaction.getAmount());
            } else if (transaction.getType().equalsIgnoreCase("Transfer") && transaction.getSourceAccount() == account.getId()) {
                expenseTotals.put(date, expenseTotals.getOrDefault(date, 0.0) + transaction.getAmount());
            }
        }
        return expenseTotals;
    }

    private static List<GraphDataPoint> convertToGraphDataPoints(List<Transaction> transactions, String type) {
        List<GraphDataPoint> dataPoints = new ArrayList<>();
        Map<LocalDate, Double> dailyTotals = calculateDailyTotals(transactions, type);

        for (Map.Entry<LocalDate, Double> entry : dailyTotals.entrySet()) {
            dataPoints.add(new GraphDataPoint(entry.getKey(), entry.getValue()));
        }
        return dataPoints;
    }

    private static Map<LocalDate, Double> calculateDailyTotals(List<Transaction> transactions, String type) {
        Map<LocalDate, Double> dailyTotals = new TreeMap<>();
        for (Transaction transaction : transactions) {
            LocalDate date = transaction.getDateTime().toLocalDate();
            if (transaction.getType().equalsIgnoreCase(type)) {
                dailyTotals.put(date, dailyTotals.getOrDefault(date, 0.0) + transaction.getAmount());
            }
        }
        return dailyTotals;
    }

    private static Map<LocalDate, Double> calculateDailyTotals(List<Transaction> transactions) {
        Map<LocalDate, Double> incomeMap = calculateDailyTotals(transactions, "Income");
        Map<LocalDate, Double> expenseMap = calculateDailyTotals(transactions, "Expense");
        Map<LocalDate, Double> dailyTotals = new TreeMap<>();

        // Create a unified set of dates that includes all dates from both maps
        Set<LocalDate> allDates = new TreeSet<>(incomeMap.keySet());
        allDates.addAll(expenseMap.keySet());

        // Calculate daily total by combining income and expense values for each date
        for (LocalDate date : allDates) {
            double totalIncome = incomeMap.getOrDefault(date, 0.0);
            double totalExpense = expenseMap.getOrDefault(date, 0.0);
            dailyTotals.put(date, totalIncome - totalExpense);
        }

        return dailyTotals;
    }
}
