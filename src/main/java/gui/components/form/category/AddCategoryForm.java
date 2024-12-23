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

public class AddCategoryForm extends VBox {
    private Modal modal;
    private TextField nameField;
    private ComboBox<String> typeComboBox;
    private TextField budgetField;
    private SettingsPage settingsPage;

    // Modify constructor to accept a category type
    public AddCategoryForm(Modal modal, String categoryType, SettingsPage settingsPage) {
        this.modal = modal;
        setupForm(categoryType); // Pass categoryType to setupForm method
        this.settingsPage = settingsPage;
    }

    // Spacer for layout
    private Region createSpacer() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }

    private void setupForm(String categoryType) {
        setSpacing(20);

        // Label for form title
        Label titleLabel = new Label("Add Category");
        titleLabel.getStyleClass().add("header1");

        // Account Name Row with Label, Spacer, and TextField
        Label nameLabel = new Label("Category name: ");
        nameField = new TextField();
        nameField.setPromptText("Category Name");
        nameField.getStyleClass().add("input-field");
        HBox nameRow = new HBox(10, nameLabel, createSpacer(), nameField);

        // ComboBox for category type (Income or Expense) with Label and Spacer
        Label typeLabel = new Label("Category type: ");
        typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("Income", "Expense");
        typeComboBox.setValue(categoryType);  // Set the value based on the passed category type
        HBox typeRow = new HBox(10, typeLabel, createSpacer(), typeComboBox);

        // Budget Field Row with Label, Spacer, and TextField
        Label budgetLabel = new Label("Category budget: ");
        budgetField = new TextField("0");
        budgetField.setPromptText("Category Budget");
        budgetField.getStyleClass().add("input-field");
        HBox budgetRow = new HBox(10, budgetLabel, createSpacer(), budgetField);

        // Buttons (Cancel & Save)
        HBox buttons = new HBox(10);
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> modal.close());
        cancelButton.getStyleClass().addAll("button", "fill-neutral", "border-neutral");

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> saveNewCategory());
        saveButton.getStyleClass().addAll("button", "fill-blue", "border-blue");

        buttons.getChildren().addAll(cancelButton, saveButton);

        // Add all components to the form
        getChildren().addAll(titleLabel, nameRow, typeRow, budgetRow, buttons);
    }

    private void saveNewCategory() {
        String name = nameField.getText();
        String type = typeComboBox.getValue();
        double budget = 0;

        try {
            budget = Double.parseDouble(budgetField.getText());

            Category newCategory = new Category(0, name, budget, type);
            new CategoryService().addCategory(newCategory);
            modal.close();
            settingsPage.refresh();
        } catch (NumberFormatException e) {
            new ErrorModal("Invalid budget input").show();
        } catch (Exception e) {
        	new ErrorModal(e.getMessage()).show();
        }
    }
}
