package gui.components.form;

import gui.app.App;
import gui.components.util.ErrorModal;
import gui.components.util.Modal;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import server.filter.*;
import server.model.Account;
import server.model.Category;
import server.model.Transaction;
import server.service.TransactionService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExportDataForm extends VBox {
    private CheckBox dateCheckBox, amountCheckBox, accountCheckBox, categoryCheckBox, noteCheckBox;
    private DatePicker fromDate, toDate;
    private TextField fromAmount, toAmount, noteField;
    private ComboBox<Account> accountComboBox;
    private ComboBox<Category> categoryComboBox;
    private final Modal modal;

    public ExportDataForm(Modal modal) {
        this.modal = modal;
        setupForm();
    }

    private void setupForm() {
        setSpacing(20);

        Label titleLabel = new Label("Export Data");
        titleLabel.getStyleClass().add("header1");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));

        // Date filters
        dateCheckBox = new CheckBox("Date:");
        fromDate = new DatePicker();
        toDate = new DatePicker();
        fromDate.setDisable(true);
        toDate.setDisable(true);
        fromDate.getStyleClass().add("input-field");
        toDate.getStyleClass().add("input-field");
        dateCheckBox.setOnAction(e -> toggleField(dateCheckBox, fromDate, toDate));
        grid.addRow(0, dateCheckBox, fromDate, new Label("to"), toDate);

        // Amount filters
        amountCheckBox = new CheckBox("Amount:");
        fromAmount = new TextField();
        toAmount = new TextField();
        fromAmount.setDisable(true);
        toAmount.setDisable(true);
        fromAmount.getStyleClass().add("input-field");
        toAmount.getStyleClass().add("input-field");
        amountCheckBox.setOnAction(e -> toggleField(amountCheckBox, fromAmount, toAmount));
        grid.addRow(1, amountCheckBox, fromAmount, new Label("to"), toAmount);

        // Account filter
        accountCheckBox = new CheckBox("Account:");
        accountComboBox = new ComboBox<>();
        accountComboBox.getItems().addAll(App.getInstance().getAccountList());
        accountComboBox.setDisable(true);
        accountCheckBox.setOnAction(e -> toggleField(accountCheckBox, accountComboBox));
        grid.addRow(2, accountCheckBox, accountComboBox);

        // Category filter
        categoryCheckBox = new CheckBox("Category:");
        categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().addAll(App.getInstance().getIncomeCategoryList());
        categoryComboBox.getItems().addAll(App.getInstance().getExpenseCategoryList());
        categoryComboBox.setDisable(true);
        categoryCheckBox.setOnAction(e -> toggleField(categoryCheckBox, categoryComboBox));
        grid.addRow(3, categoryCheckBox, categoryComboBox);

        // Note filter
        noteCheckBox = new CheckBox("Note:");
        noteField = new TextField();
        noteField.setDisable(true);
        noteField.getStyleClass().add("input-field");
        noteCheckBox.setOnAction(e -> toggleField(noteCheckBox, noteField));
        grid.addRow(4, noteCheckBox, noteField);

        
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> modal.close());
        cancelButton.getStyleClass().addAll("border-neutral", "fill-neutral");
        
        // Export button
        Button exportButton = new Button("Export");
        exportButton.setOnAction(e -> exportData());
        exportButton.getStyleClass().addAll("border-blue", "fill-blue");
        
        HBox buttonBox = new HBox(10, cancelButton, exportButton);
        buttonBox.setAlignment(Pos.CENTER);

        getChildren().addAll(titleLabel, grid, buttonBox);
        setAlignment(Pos.CENTER);
    }

    private void toggleField(CheckBox checkBox, Control... controls) {
        for (Control control : controls) {
            control.setDisable(!checkBox.isSelected());
        }
    }

    private void exportData() {
        try {
            List<TransactionFilterStrategy> filters = new ArrayList<>();
            TransactionService transactionService = new TransactionService();
            List<Transaction> transactions = transactionService.getAllTransactions();

            if (dateCheckBox.isSelected()) {
                if (fromDate.getValue() == null || toDate.getValue() == null) {
                    new ErrorModal("Please select both From and To dates.").show();
                    return;
                }
                filters.add(new DateFilterStrategy(fromDate.getValue(), toDate.getValue()));
            }
            if (amountCheckBox.isSelected()) {
                try {
                    double minAmount = Double.parseDouble(fromAmount.getText());
                    double maxAmount = Double.parseDouble(toAmount.getText());
                    filters.add(new AmountFilterStrategy(minAmount, maxAmount));
                } catch (NumberFormatException e) {
                    new ErrorModal("Invalid amount values. Please enter valid numbers.").show();
                    return;
                }
            }
            if (accountCheckBox.isSelected()) {
                if (accountComboBox.getValue() == null) {
                    new ErrorModal("Please select an account.").show();
                    return;
                }
                filters.add(new AccountFilterStrategy(accountComboBox.getValue()));
            }
            if (categoryCheckBox.isSelected()) {
                if (categoryComboBox.getValue() == null) {
                    new ErrorModal("Please select a category.").show();
                    return;
                }
                filters.add(new CategoryFilterStrategy(categoryComboBox.getValue()));
            }
            if (noteCheckBox.isSelected()) {
                if (noteField.getText().isEmpty()) {
                    new ErrorModal("Please enter a note value.").show();
                    return;
                }
                filters.add(new NoteFilterStrategy(noteField.getText()));
            }

            TransactionFilter transactionFilter = new TransactionFilter(transactions);
            filters.forEach(transactionFilter::addFilterStrategy);
            List<Transaction> filteredTransactions = transactionFilter.applyFilters();

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save CSV");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            fileChooser.setInitialFileName("export.csv");

            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                saveToCsv(file.getAbsolutePath(), filteredTransactions);
                modal.close(); // Close modal after successful export
            }
        } catch (Exception e) {
            new ErrorModal("Error: " + e.getMessage()).show();
        }
    }

    private void saveToCsv(String filePath, List<Transaction> transactions) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("Date,Type,Amount,Source Account,Destination Account,Category,Note\n");
            for (Transaction t : transactions) {
                writer.write(String.format("%s,%s,%f,%s,%s,%s,%s\n",
                        t.getDateTime(), t.getType(), t.getAmount(), t.getSourceAccountName(),
                        t.getDestinationAccountName(), t.getCategoryName(), t.getNote()));
            }
        } catch (IOException e) {
            new ErrorModal("Failed to save file: " + e.getMessage()).show();
        }
    }
}
