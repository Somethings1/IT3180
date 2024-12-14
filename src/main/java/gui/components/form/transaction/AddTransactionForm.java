package gui.components.form.transaction;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import gui.components.transaction.list.TransactionListView;
import gui.components.util.ErrorModal;
import gui.components.util.Modal;
import gui.pages.OverviewPage;
import server.service.TransactionService;

public class AddTransactionForm extends TransactionForm {

    private TransactionListView view;

    public AddTransactionForm(Modal modal) {
        super(modal);
    }

    public void setListView(TransactionListView view) {
        this.view = view;
    }

    @Override
    protected HBox createButtonRow() {
        // Create the HBox for buttons
        HBox buttonRow = new HBox(10);  // Horizontal spacing between buttons
        
        // Create the buttons
        Button cancelButton = new Button("Cancel");
        Button saveButton = new Button("Save");

        // Set up action handlers for the buttons
        cancelButton.setOnAction(this::handleCancel);
        saveButton.setOnAction(this::handleSave);

        // Add buttons to the row
        buttonRow.getChildren().addAll(cancelButton, saveButton);
        
        return buttonRow;
    }

    private void handleSave(ActionEvent event) {
        try {
        	new TransactionService().addTransaction(transaction);
            OverviewPage.getInstance().requestReloading();
            handleCancel(null); 
        } catch (Exception e) {
        	new ErrorModal(e.getMessage()).show();
        }
    }
}
