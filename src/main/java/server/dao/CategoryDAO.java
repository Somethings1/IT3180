package server.dao;

import server.model.Category;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO extends BaseDAO {

    public void insert(Category category) {
        String sql = "INSERT INTO categories (name, budget, type) VALUES (?, ?, ?)";
        try {
            getConnection();
			PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, category.getName());
            stmt.setDouble(2, category.getBudget());
            stmt.setString(3, category.getType());
            stmt.executeUpdate();
            closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(Category category) {
        String sql = "UPDATE categories SET name = ?, budget = ? WHERE id = ?";
        try {
            getConnection();
			PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, category.getName());
            stmt.setDouble(2, category.getBudget());
            stmt.setInt(3, category.getId());
            stmt.executeUpdate();
            closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int categoryId) {
        String deleteTransactionsSql = "DELETE FROM transactions WHERE category = ?";
        String deleteCategorySql = "DELETE FROM categories WHERE id = ?";
        
        try {
            getConnection();

            // First, delete related transactions
            PreparedStatement stmtTransactions = connection.prepareStatement(deleteTransactionsSql);
            stmtTransactions.setInt(1, categoryId);
            stmtTransactions.executeUpdate();
            stmtTransactions.close();

            // Then, delete the category
            PreparedStatement stmtCategory = connection.prepareStatement(deleteCategorySql);
            stmtCategory.setInt(1, categoryId);
            stmtCategory.executeUpdate();
            stmtCategory.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }


    public List<Category> findAll() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories";
        try {
            getConnection();
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                categories.add(mapRowToCategory(rs));
            }
            closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }
    
    public List<Category> findAllIncome() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE type='income'";
        try {
            getConnection();
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                categories.add(mapRowToCategory(rs));
            }
            closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }
    
    public List<Category> findAllExpense() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE type='expense'";
        try {
            getConnection();
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                categories.add(mapRowToCategory(rs));
            }
            closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }
    
    public Category findCategory (int id) {
    	String sql = "SELECT * FROM categories WHERE id = " + id;
    	Category category = null;
        try {
            getConnection();
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                category = mapRowToCategory(rs);
            }
            closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return category;
    }

    private Category mapRowToCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getInt("id"));
        category.setName(rs.getString("name"));
        category.setBudget(rs.getDouble("budget"));
        category.setType(rs.getString("type"));
        return category;
    }
}
