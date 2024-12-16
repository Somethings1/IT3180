package gui.components.form.transaction;

import gui.components.util.ErrorModal;
import gui.components.util.Modal;
import gui.pages.OverviewPage;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import server.model.Transaction;
import server.service.TransactionService;

public class EditTransactionForm extends TransactionForm {

    public EditTransactionForm(Transaction transaction, Modal modal) {
        super(transaction, modal);
    }

    @Override
    protected HBox createButtonRow() {
        // Create the HBox for buttons
        HBox buttonRow = new HBox(10);
        
        // Create the buttons
        Button cancelButton = new Button("Cancel");
        Button saveButton = new Button("Save");
        Button removeButton = new Button("Remove");
        

        // Set up action handlers
        cancelButton.setOnAction(this::handleCancel);
        saveButton.setOnAction(this::handleSave);
        removeButton.setOnAction(this::handleRemove); // Added the handler for remove

        // Add buttons to the row
        buttonRow.getChildren().addAll(cancelButton, saveButton, removeButton);
        
        return buttonRow;
    }

    private void handleRemove(ActionEvent event) {
        // Remove the transaction
        new TransactionService().removeTransaction(transaction.getId());
        OverviewPage.getInstance().requestReloading();
        handleCancel(event);  // Close the form after removal
    }

    private void handleSave(ActionEvent event) {
        try {
        	new TransactionService().updateTransaction(this.transaction);
            OverviewPage.getInstance().requestReloading();
            handleCancel(event);
        }
        catch (Exception e) {
        	new ErrorModal(e.getMessage()).show();
        }
    }
}
