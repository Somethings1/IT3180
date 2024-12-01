package server.service;

import gui.app.App;
import server.dao.CategoryDAO;
import server.model.Category;

import java.util.List;

public class CategoryService {
    private final CategoryDAO categoryDAO;

    public CategoryService() {
        categoryDAO = new CategoryDAO();
    }

    public void addCategory(Category category) throws Exception {
    	assertCategory(category);
        categoryDAO.insert(category);
        App.getInstance().loadCategoryData();
    }

    public void updateCategory(Category category) throws Exception {
    	assertCategory(category);
        categoryDAO.update(category);
        App.getInstance().loadCategoryData();
    }

    public void removeCategory(int categoryId) {
        categoryDAO.delete(categoryId);
        App.getInstance().loadCategoryData();
    }

    //Để export
    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }
    
    public List<Category> getAllIncomeCategories() {
        return categoryDAO.findAllIncome();
    }
    public List<Category> getAllExpenseCategories() {
        return categoryDAO.findAllExpense();
    }
    
    public Category getCategory (int id) {
    	return categoryDAO.findCategory(id);
    }
    
    private void assertCategory (Category category) throws Exception {
    	if (category.getName().isEmpty())
    		throw new Exception ("Category name cannot be null");
    }
}
