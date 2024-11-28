package server.dao;

import server.model.Account;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO extends BaseDAO {

    public AccountDAO() {}

    // Helper method to manage connection and perform the operation
    private <T> T executeWithConnection(SQLFunction<Connection, T> sqlFunction) {
        getConnection();
        try {
            return sqlFunction.apply(connection);
        } catch (SQLException e) {
            handleSQLException(e); // Handle the SQLException
            return null; // Return null or a default value if needed
        } finally {
            closeConnection();
        }
    }

    public void insert(Account account) {
        String sql = "INSERT INTO accounts (name, group_name, balance, goal) VALUES (?, ?, ?, ?)";
        executeWithConnection(connection -> {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, account.getName());
            pstmt.setString(2, account.getGroup());
            pstmt.setDouble(3, account.getBalance());
            pstmt.setDouble(4, account.getGoal());
            pstmt.executeUpdate();
            return null;
        });
    }

    public void update(Account account) {
        String sql = "UPDATE accounts SET name = ?, group_name = ?, balance = ?, goal = ? WHERE id = ?";
        executeWithConnection(connection -> {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, account.getName());
            pstmt.setString(2, account.getGroup());
            pstmt.setDouble(3, account.getBalance());
            pstmt.setDouble(4, account.getGoal());
            pstmt.setInt(5, account.getId());
            pstmt.executeUpdate();
            return null;
        });
    }

    public void delete(int accountId) {
        String deleteTransactionsSql = "DELETE FROM transactions WHERE source_account = ? OR destination_account = ?";
        String deleteAccountsSql = "DELETE FROM accounts WHERE id = ?";

        // First, delete associated transactions
        executeWithConnection(connection -> {
            try (PreparedStatement pstmtTransactions = connection.prepareStatement(deleteTransactionsSql)) {
                pstmtTransactions.setInt(1, accountId);
                pstmtTransactions.setInt(2, accountId);
                pstmtTransactions.executeUpdate();
            }
            return null;
        });

        // Then, delete the account itself
        executeWithConnection(connection -> {
            try (PreparedStatement pstmtAccounts = connection.prepareStatement(deleteAccountsSql)) {
                pstmtAccounts.setInt(1, accountId);
                pstmtAccounts.executeUpdate();
            }
            return null;
        });
    }

    
    public double getBalanceAtEndOfMonth(int accountId, int month, int year) {
        String sql = "WITH AccountTransactions AS ( " +
                     "SELECT " +
                     "    date_time, " +
                     "    CASE " +
                     "        WHEN type = 'Income' THEN -amount " +
                     "        WHEN type = 'Expense' THEN amount " +
                     "        WHEN type = 'Transfer' AND source_account = ? THEN amount " +
                     "        WHEN type = 'Transfer' AND destination_account = ? THEN -amount " +
                     "        ELSE 0 " +
                     "    END AS balance_change " +
                     "FROM transactions " +
                     "WHERE " +
                     "    (source_account = ? OR destination_account = ?) " +
                     "    AND date_time >= strftime('%s', ?, 'start of month', '+1 month') * 1000 " + // First day of next month
                     ") " +
                     "SELECT " +
                     "    (SELECT balance FROM accounts WHERE id = ?) + COALESCE(SUM(balance_change), 0) AS end_of_month_balance " +
                     "FROM AccountTransactions;";

        String targetDate = String.format("%04d-%02d-01", year, month); // Format: YYYY-MM

        return executeWithConnection(connection -> {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, accountId);
            pstmt.setInt(2, accountId);
            pstmt.setInt(3, accountId);
            pstmt.setInt(4, accountId);
            pstmt.setString(5, targetDate);
            pstmt.setInt(6, accountId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("end_of_month_balance");
            }
            return 0.0; // Default value if no result found
        });
    }

    public List<Account> findAll() {
        String sql = "SELECT * FROM accounts";
        return executeWithConnection(connection -> {
            List<Account> accounts = new ArrayList<>();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                accounts.add(mapRowToAccount(rs));
            }
            return accounts;
        });
    }

    public Account findAccount(int id) {
        String sql = "SELECT * FROM accounts WHERE id = ?";
        return executeWithConnection(connection -> {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRowToAccount(rs);
            }
            return null;
        });
    }

    public void adjustAccountBalance(int accountId, double amount) {
        String updateAccountBalanceSQL = "UPDATE accounts SET balance = ? WHERE id = ?";
        executeWithConnection(connection -> {
            PreparedStatement pstmt = connection.prepareStatement(updateAccountBalanceSQL);
            double currentBalance = getAccountBalance(accountId, connection);
            pstmt.setDouble(1, currentBalance + amount);
            pstmt.setInt(2, accountId);
            pstmt.executeUpdate();
            return null;
        });
    }

    // Helper function to get the current account balance
    private double getAccountBalance(int accountId, Connection connection) {
        String getBalanceSQL = "SELECT balance FROM accounts WHERE id = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(getBalanceSQL);
            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("balance");
            } else {
                // Handle the case when the account is not found
                return 0.0; // Return a default value or handle as needed
            }
        } catch (SQLException e) {
            handleSQLException(e);
            return 0.0; // Return a default value or handle as needed
        }
    }

    // Map result set row to account object
    private Account mapRowToAccount(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setId(rs.getInt("id"));
        account.setName(rs.getString("name"));
        account.setGroup(rs.getString("group_name"));
        account.setBalance(rs.getDouble("balance"));
        account.setGoal(rs.getDouble("goal"));
        return account;
    }

    // Handle SQLException (log it or take appropriate action)
    private void handleSQLException(SQLException e) {
        // Log the exception or handle it as needed
        System.err.println("SQL Exception: " + e.getMessage());
        e.printStackTrace(); // Print stack trace for debugging
    }

    // Functional interface for SQL operations
    @FunctionalInterface
    public interface SQLFunction<T, R> {
        R apply(T t) throws SQLException;
    }
}
