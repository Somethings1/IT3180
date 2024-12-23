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

public class AddAccountForm extends VBox {
    private Modal modal;
    private TextField nameField;
    private TextField balanceField;
    private SettingsPage settingsPage;

    public AddAccountForm(Modal modal, SettingsPage settingsPage) {
        setupForm();
        this.modal = modal;
        this.settingsPage = settingsPage;
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
        Label titleLabel = new Label("Add Account");
        titleLabel.getStyleClass().add("header1");

        // Account Name Row with Label, Spacer, and TextField
        Label nameLabel = new Label("Account name: ");
        nameField = new TextField();
        nameField.setPromptText("Account Name");
        nameField.getStyleClass().add("input-field");
        HBox nameRow = new HBox(10, nameLabel, createSpacer(), nameField);

        // Account Balance Row with Label, Spacer, and TextField
        Label balanceLabel = new Label("Initial balance: ");
        balanceField = new TextField("0");
        balanceField.setPromptText("Initial Balance");
        balanceField.getStyleClass().add("input-field");
        HBox balanceRow = new HBox(10, balanceLabel, createSpacer(), balanceField);

        // Buttons (Cancel & Save)
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> modal.close());
        cancelButton.getStyleClass().addAll("button", "fill-neutral", "border-neutral");

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> saveNewAccount());
        saveButton.getStyleClass().addAll("button", "fill-blue", "border-blue");

        buttons.getChildren().addAll(cancelButton, saveButton);

        // Add all components to the form
        getChildren().addAll(titleLabel, nameRow, balanceRow, buttons);
    }

    private void saveNewAccount() {
        String name = nameField.getText();
        try {
        	double balance = Double.parseDouble(balanceField.getText());

            // Create a new Account instance
            Account newAccount = new Account(0, name, "Account", balance, 0);
        	new AccountService().addAccount(newAccount);
            modal.close();
            settingsPage.refresh();
        }
        catch (NumberFormatException e) {
        	new ErrorModal("Balance is not a number").show();
        }
        catch (Exception e) {
        	new ErrorModal(e.getMessage()).show();
        }
    }
}
