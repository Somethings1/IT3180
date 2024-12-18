package gui.components.form.account;

import gui.pages.SettingsPage;
import gui.app.App;
import gui.components.util.ErrorModal;
import gui.components.util.Modal;
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

public class EditSavingForm extends VBox {
    private Modal modal;
    private TextField nameField;
    private TextField goalField;
    private SettingsPage settingsPage;
    private Account account;

    public EditSavingForm(Modal modal, SettingsPage settingsPage, Account account) {
        this.modal = modal;
        this.settingsPage = settingsPage;
        this.account = account;
        setupForm();
    }

    private Region createSpacer() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }

    private void setupForm() {
        setSpacing(20);

        Label titleLabel = new Label("Edit Saving");
        titleLabel.getStyleClass().add("header1");

        Label nameLabel = new Label("Saving name:");
        nameField = new TextField(account.getName());  // Pre-fill with existing account name
        nameField.setPromptText("Saving Name");
        nameField.getStyleClass().add("input-field");
        HBox nameRow = new HBox(10, nameLabel, createSpacer(), nameField);

        Label goalLabel = new Label("Goal amount:");
        goalField = new TextField(String.valueOf(account.getGoal()));  // Pre-fill with existing goal amount
        goalField.setPromptText("Goal Amount");
        goalField.getStyleClass().add("input-field");
        HBox goalRow = new HBox(10, goalLabel, createSpacer(), goalField);

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> modal.close());
        cancelButton.getStyleClass().addAll("button", "fill-neutral", "border-neutral");

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> saveEditedSaving());
        saveButton.getStyleClass().addAll("button", "fill-blue", "border-blue");

        buttons.getChildren().addAll(cancelButton, saveButton);

        getChildren().addAll(titleLabel, nameRow, goalRow, buttons);
    }

    private void saveEditedSaving() {
        try {
            String name = nameField.getText();
            double goal = Double.parseDouble(goalField.getText());

            // Update account details
            account.setName(name);
            account.setGoal(goal);

            // Edit account in AccountService
            new AccountService().updateAccount(account);

            modal.close();
            settingsPage.refresh();
        } catch (NumberFormatException e) {
            new ErrorModal("Saving goal is invalid").show();
        } catch (Exception e) {
            new ErrorModal(e.getMessage()).show();
        }
    }
}
