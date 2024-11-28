package server.dao;

import server.model.Transaction;
import server.service.AccountService;
import server.utils.TimeUtils;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO extends BaseDAO {

	public void insert(Transaction transaction) {
	    String sql = "INSERT INTO transactions (date_time, amount, source_account, category, destination_account, note, type) VALUES (?, ?, ?, ?, ?, ?, ?)";

	    try {
			getConnection();
	        PreparedStatement stmt = connection.prepareStatement(sql);

	        Timestamp utcTimestamp = TimeUtils.toUtcTimestamp(transaction.getDateTime());

	        stmt.setTimestamp(1, utcTimestamp);
	        stmt.setDouble(2, transaction.getAmount());
	        stmt.setInt(3, transaction.getSourceAccount());
	        if (transaction.getCategory() != 0) {
	            stmt.setInt(4, transaction.getCategory());
	        } else {
	            stmt.setNull(4, Types.INTEGER);
	        }
	        if (transaction.getDestinationAccount() != 0) {
	            stmt.setInt(5, transaction.getDestinationAccount());
	        } else {
	            stmt.setNull(5, Types.INTEGER);
	        }
	        stmt.setString(6, transaction.getNote());
	        stmt.setString(7, transaction.getType());

	        stmt.executeUpdate();
	        closeConnection();
	        applyTransactionOnAccount(transaction);
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	public void update(Transaction transaction) {
		remove(transaction.getId());
		insert(transaction);
	}
	public void remove(int id) {
		String sql = "DELETE FROM transactions WHERE id = ?";

		try {
			rollbackTransactionOnAccount(findById(id));
			getConnection();
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setInt(1, id);
			stmt.executeUpdate();
			closeConnection();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Transaction findById(int id) {
		String sql = "SELECT * FROM transactions WHERE id = ?";
		Transaction transaction = null;

		try {
			getConnection();
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				transaction = mapRowToTransaction(rs);
			}
			closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return transaction;
	}

	public List<Transaction> getAllTransactions() {
		String sql = "SELECT * FROM transactions";
		List<Transaction> transactions = new ArrayList<>();

		try {
			getConnection();
			PreparedStatement stmt = connection.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				Transaction transaction = mapRowToTransaction(rs);
				transactions.add(transaction);
			}
			closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return transactions;
	}
	
	public List<Transaction> findByMonth(int month, int year) {
	    String sql = "SELECT * FROM transactions WHERE date_time >= ? AND date_time < ? ORDER BY date_time DESC";
	    List<Transaction> transactions = new ArrayList<>();

	    // Get the start and end of the month in local time
	    LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
	    LocalDateTime endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth()).plusDays(1); // Start of the next month

	    // Convert start and end of the month to UTC
	    Timestamp utcStartTimestamp = TimeUtils.toUtcTimestamp(startOfMonth);
	    Timestamp utcEndTimestamp = TimeUtils.toUtcTimestamp(endOfMonth);

	    try {
			getConnection();
	        PreparedStatement stmt = connection.prepareStatement(sql);
	        stmt.setTimestamp(1, utcStartTimestamp);
	        stmt.setTimestamp(2, utcEndTimestamp);
	        ResultSet rs = stmt.executeQuery();

	        while (rs.next()) {
	            Transaction transaction = mapRowToTransaction(rs);
	            transactions.add(transaction);
	        }
	        closeConnection();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return transactions;
	}
	
	public List<Transaction> findByYear(int year) {
	    String sql = "SELECT * FROM transactions WHERE strftime('%Y', datetime(date_time / 1000, 'unixepoch')) = ? ORDER BY date_time DESC";
	    List<Transaction> transactions = new ArrayList<>();

	    try {
			getConnection();
	        PreparedStatement stmt = connection.prepareStatement(sql);
	        stmt.setString(1, String.valueOf(year)); // Pass the year as a string
	        ResultSet rs = stmt.executeQuery();

	        while (rs.next()) {
	            Transaction transaction = mapRowToTransaction(rs);
	            transactions.add(transaction);
	        }
	        closeConnection();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return transactions;
	}
	
	public Transaction mapRowToTransaction(ResultSet rs) throws SQLException {
		Transaction transaction = new Transaction();
		transaction.setId(rs.getInt("id"));
        // Assuming the column name is "date_time"
        Timestamp timestamp = rs.getTimestamp("date_time");

        // Convert UTC Timestamp to LocalDateTime
        LocalDateTime localDateTime = TimeUtils.toLocalDateTime(timestamp);
        transaction.setDateTime(localDateTime);

        transaction.setAmount(rs.getDouble("amount"));
        transaction.setSourceAccount(rs.getInt("source_account"));
        transaction.setCategory(rs.getInt("category"));
        transaction.setDestinationAccount(rs.getInt("destination_account"));
        transaction.setNote(rs.getString("note"));
        transaction.setType(rs.getString("type"));

        return transaction;
    }
	
	private void rollbackTransactionOnAccount(Transaction transaction) {
		AccountService serv = new AccountService();
	    if (transaction.getType().equals("Income")) {
	        // Subtract from the account (reverse the income)
	        serv.adjustBalance(transaction.getSourceAccount(), -transaction.getAmount());
	    } else if (transaction.getType().equals("Expense")) {
	        // Add back to the account (reverse the expense)
	        serv.adjustBalance(transaction.getSourceAccount(), transaction.getAmount());
	    } else if (transaction.getType().equals("Transfer")) {
	        // Reverse transfer: add back to source, subtract from destination
	        serv.adjustBalance(transaction.getSourceAccount(), transaction.getAmount());
	        serv.adjustBalance(transaction.getDestinationAccount(), -transaction.getAmount());
	    }
	}

	private void applyTransactionOnAccount(Transaction transaction) {
		AccountService serv = new AccountService();
	    if (transaction.getType().equals("Income")) {
	        // Add to the account (apply income)
	        serv.adjustBalance(transaction.getSourceAccount(), transaction.getAmount());
	    } else if (transaction.getType().equals("Expense")) {
	        // Subtract from the account (apply expense)
	        serv.adjustBalance(transaction.getSourceAccount(), -transaction.getAmount());
	    } else if (transaction.getType().equals("Transfer")) {
	        // Transfer: subtract from source, add to destination
	        serv.adjustBalance(transaction.getSourceAccount(), -transaction.getAmount());
	        serv.adjustBalance(transaction.getDestinationAccount(), transaction.getAmount());
	    }
	}
}
