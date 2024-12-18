package gui.components.form.category;

import gui.app.App;
import gui.components.util.ErrorModal;
import gui.components.util.Modal;
import gui.pages.SettingsPage;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import server.model.Category;
import server.service.CategoryService;

public class EditCategoryForm extends VBox {
    private Modal modal;
    private TextField nameField;
    private ComboBox<String> typeComboBox;
    private TextField budgetField;
    private SettingsPage settingsPage;
    private Category category;

    public EditCategoryForm(Modal modal, SettingsPage settingsPage, Category category) {
        this.modal = modal;
        this.settingsPage = settingsPage;
        this.category = category;
        setupForm();
    }

    // Spacer for layout
    private Region createSpacer() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }
    
    private void setupForm() {
        setSpacing(20);

        // Label for form title
        Label titleLabel = new Label("Edit Category");
        titleLabel.getStyleClass().add("header1");

        // Category Name Row with Label, Spacer, and TextField
        Label nameLabel = new Label("Category name: ");
        nameField = new TextField(category.getName());  // Pre-fill with the current category name
        nameField.setPromptText("Category Name");
        nameField.getStyleClass().add("input-field");
        HBox nameRow = new HBox(10, nameLabel, createSpacer(), nameField);

        // ComboBox for category type (Income or Expense) with Label and Spacer
        Label typeLabel = new Label("Category type: ");
        typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("Income", "Expense");
        typeComboBox.setValue(category.getType());  // Pre-fill with the current category type
        HBox typeRow = new HBox(10, typeLabel, createSpacer(), typeComboBox);

        // Budget Field Row with Label, Spacer, and TextField
        Label budgetLabel = new Label("Category budget: ");
        budgetField = new TextField(String.valueOf(category.getBudget()));  // Pre-fill with the current category budget
        budgetField.setPromptText("Category Budget");
        budgetField.getStyleClass().add("input-field");
        HBox budgetRow = new HBox(10, budgetLabel, createSpacer(), budgetField);

        // Buttons (Cancel & Save)
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> modal.close());
        cancelButton.getStyleClass().addAll("button", "fill-neutral", "border-neutral");

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> saveEditedCategory());
        saveButton.getStyleClass().addAll("button", "fill-blue", "border-blue");

        buttons.getChildren().addAll(cancelButton, saveButton);
        
        // Add all components to the form
        getChildren().addAll(titleLabel, nameRow, typeRow, budgetRow, buttons);
    }

    private void saveEditedCategory() {
        String newName = nameField.getText();
        String newType = typeComboBox.getValue();
        double newBudget;

        try {
            newBudget = Double.parseDouble(budgetField.getText());
            
            // Update the category with new values
            category.setName(newName);
            category.setType(newType);
            category.setBudget(newBudget);

            // Save changes using CategoryService
            new CategoryService().updateCategory(category);

            modal.close();
            settingsPage.refresh();
        } catch (NumberFormatException e) {
            new ErrorModal("Invalid budget input").show();
        } catch (Exception e) {
            new ErrorModal(e.getMessage()).show();
        }
    }
}
