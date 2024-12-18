package gui.components.form.account;

import gui.app.App;
import gui.components.util.ErrorModal;
import gui.components.util.Modal;
import gui.pages.SettingsPage;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import server.model.Account;
import server.service.AccountService;

public class EditAccountForm extends VBox {
    private Modal modal;
    private TextField nameField;
    private SettingsPage settingsPage;
    private Account account;

    public EditAccountForm(Modal modal, SettingsPage settingsPage, Account account) {
        this.modal = modal;
        this.settingsPage = settingsPage;
        this.account = account;
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
        Label titleLabel = new Label("Edit Account");
        titleLabel.getStyleClass().add("header1");

        // Account Name Row with Label, Spacer, and TextField
        Label nameLabel = new Label("Account name: ");
        nameField = new TextField(account.getName()); // Pre-fill with existing account name
        nameField.setPromptText("Account Name");
        nameField.getStyleClass().add("input-field");
        HBox nameRow = new HBox(10, nameLabel, createSpacer(), nameField);

        // Buttons (Cancel & Save)
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> modal.close());
        cancelButton.getStyleClass().addAll("button", "fill-neutral", "border-neutral");

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> saveEditedAccount());
        saveButton.getStyleClass().addAll("button", "fill-blue", "border-blue");

        buttons.getChildren().addAll(cancelButton, saveButton);
        
        // Add all components to the form
        getChildren().addAll(titleLabel, nameRow, buttons);
    }

    private void saveEditedAccount() {
        String newName = nameField.getText();
        try {
            // Update the account's name
            account.setName(newName);

            // Save changes using AccountService
            new AccountService().updateAccount(account);

            modal.close();
            settingsPage.refresh();
        } catch (Exception e) {
            new ErrorModal(e.getMessage()).show();
        }
    }
}
