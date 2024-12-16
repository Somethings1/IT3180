package gui.components.form.transaction;

import java.time.LocalDateTime;
import gui.app.App;
import gui.components.transaction.list.TransactionListItem;
import gui.components.util.ErrorModal;
import gui.components.util.Modal;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import server.model.Account;
import server.model.Category;
import server.model.Transaction;
import server.service.AccountService;
import server.service.CategoryService;

public abstract class TransactionForm extends VBox {
    protected Transaction transaction;

    // UI components
    protected Button incomeButton;
    protected Button expenseButton;
    protected Button transferButton;
    protected DatePicker datePicker;
    protected TextField amountField;
    protected ComboBox<Account> sourceAccountComboBox;
    protected ComboBox<Account> destinationAccountComboBox;
    protected ComboBox<Category> categoryComboBox;
    protected TextField noteField;
    protected VBox form;
    
    protected Modal modal;
    
    private boolean flag = false;

    // Constructors
    public TransactionForm(Modal modal) {
        this(Transaction.createDefault(), modal);
    }

    public TransactionForm(Transaction transaction, Modal modal) {
        this.transaction = transaction;
        initializeUI();
        this.setSpacing(30);
        this.modal = modal;
    }

    private void initializeUI() {


        // Type buttons (Income, Expense, Transfer)
        HBox typeRow = new HBox(10);
        typeRow.setAlignment(Pos.CENTER);
        incomeButton = new Button("Income");
        expenseButton = new Button("Expense");
        transferButton = new Button("Transfer");
        

        incomeButton.setOnAction(e -> setTransactionType("Income"));
        expenseButton.setOnAction(e -> setTransactionType("Expense"));
        transferButton.setOnAction(e -> setTransactionType("Transfer"));

        typeRow.getChildren().addAll(incomeButton, expenseButton, transferButton);
        
        this.getChildren().add(typeRow);

        // Form fields
        form = new VBox(20);
        this.getChildren().add(form);
        setTransactionType(transaction.getType());
    }

    private void updateFormFields() {
        form.getChildren().clear(); // Clear previous form

        // Date row
        HBox dateRow = createRow("Date", datePicker = new DatePicker(transaction.getDateTime().toLocalDate())); // Set the transaction's date

        // Listener to update the transaction's date field
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> transaction.setDateTime(newValue.atStartOfDay()));
        form.getChildren().add(dateRow);

        // Amount row
        HBox amountRow = createRow("Amount", amountField = new TextField());
        amountField.setPromptText("Enter amount");

        amountField.setText(String.valueOf(transaction.getAmount())); // Set the transaction's amount
        // Listener to update the transaction's amount field
        amountField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                transaction.setAmount(Double.parseDouble(newValue));
            } catch (NumberFormatException e) {
                new ErrorModal("Invalid amount").show();
            }
        });
        form.getChildren().add(amountRow);

        // Source account row
        HBox sourceAccountRow = createRow("Source Account", sourceAccountComboBox = new ComboBox<>());
        sourceAccountComboBox.getItems().addAll(App.getInstance().getAccountList());

        sourceAccountComboBox.setCellFactory(accountComboBox -> new ListCell<Account>() {
            @Override
            protected void updateItem(Account item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName());
            }
        });
        // Set the source account based on the transaction
        for (Account account : sourceAccountComboBox.getItems()) {
            if (account.getId() == transaction.getSourceAccount()) {
                sourceAccountComboBox.getSelectionModel().select(account);
                break;
            }
        }
        // Listener to update the transaction's source account field
        sourceAccountComboBox.valueProperty().addListener((observable, oldValue, newValue) -> transaction.setSourceAccount(newValue.getId()));
        form.getChildren().add(sourceAccountRow);

        if (transaction.getType().equals("Transfer")) {
            // Destination account row
            HBox destinationAccountRow = createRow("Destination Account", destinationAccountComboBox = new ComboBox<>());
            destinationAccountComboBox.getItems().addAll(App.getInstance().getAccountList());

            destinationAccountComboBox.setCellFactory(accountComboBox -> new ListCell<Account>() {
                @Override
                protected void updateItem(Account item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getName());
                }
            });
            // Set the destination account based on the transaction
            for (Account account : destinationAccountComboBox.getItems()) {
                if (account.getId() == transaction.getDestinationAccount()) {
                    destinationAccountComboBox.getSelectionModel().select(account);
                    break;
                }
            }
            // Listener to update the transaction's destination account field
            destinationAccountComboBox.valueProperty().addListener((observable, oldValue, newValue) -> transaction.setDestinationAccount(newValue.getId()));
            form.getChildren().add(destinationAccountRow);
        }

        // Category row (only for Income or Expense)
        if (transaction.getType().equals("Income") || transaction.getType().equals("Expense")) {
            HBox categoryRow = createRow("Category", categoryComboBox = new ComboBox<>());
            if (transaction.getType().equals("Income")) {
                categoryComboBox.getItems().addAll(App.getInstance().getIncomeCategoryList());
            } else {
                categoryComboBox.getItems().addAll(App.getInstance().getExpenseCategoryList());
            }
            // Set the category based on the transaction
            for (Category category : categoryComboBox.getItems()) {
                if (category.getId() == transaction.getCategory()) {
                    categoryComboBox.getSelectionModel().select(category);
                    break;
                }
            }
            // Listener to update the transaction's category field
            categoryComboBox.valueProperty().addListener((observable, oldValue, newValue) -> transaction.setCategory(newValue.getId()));
            form.getChildren().add(categoryRow);
        }

        // Note row
        HBox noteRow = createRow("Note", noteField = new TextField());
        noteField.setPromptText("Enter a note");
        noteField.setText(transaction.getNote()); // Set the transaction's note
        // Listener to update the transaction's note field
        noteField.textProperty().addListener((observable, oldValue, newValue) -> transaction.setNote(newValue));
        form.getChildren().add(noteRow);

        // Button row (leave to subclass implementation)
        HBox buttonRow = createButtonRow();

        form.getChildren().add(buttonRow);
    }


    private void setTransactionType(String type) {
        transaction.setType(type);
        
        if(flag) {
        	transaction.setCategory(0);
        	transaction.setDestinationAccount(0);
        }
        if (!flag) flag = true;

        updateFormFields(); // Update form fields based on the selected type
    }

    private HBox createRow(String label, Control control) {
        Label rowLabel = new Label(label);
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        HBox row = new HBox(10);
        row.getChildren().addAll(rowLabel, region, control);
        return row;
    }

    protected void handleCancel(ActionEvent event) {
    	if (modal != null) {
            modal.close();  // Close the modal
        }
    }

    // Abstract method to create the button row in subclasses
    protected abstract HBox createButtonRow();
}
