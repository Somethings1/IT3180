package gui.components.transaction.list;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import server.model.Transaction;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TransactionListView extends ScrollPane {
    private VBox contentPane;
    List<Transaction> transactions;

    public TransactionListView(List<Transaction> transactions) {
        this.transactions = transactions;
        contentPane = new VBox(10);  // Spacing between items
        setContent(contentPane);
        groupAndDisplayTransactions();
        setFitToWidth(true);
        setFitToHeight(true);
    }

    public void remove(Transaction transaction) {
        contentPane.getChildren().clear();
        transactions.remove(transaction);
        groupAndDisplayTransactions();
    }

    public void add(Transaction transaction) {
        contentPane.getChildren().clear();
        transactions.add(transaction);
        groupAndDisplayTransactions();
    }

    private void groupAndDisplayTransactions() {
        // Clear the current content
        contentPane.getChildren().clear();

        // Check if transactions are empty
        if (transactions == null || transactions.isEmpty()) {
            Label noDataLabel = new Label("No data");
            noDataLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 14;"); // Style the no data label
            contentPane.getChildren().add(noDataLabel);
            return; // Exit the method if there are no transactions
        }

        // Group transactions by their date
        Map<LocalDate, List<Transaction>> groupedTransactions = transactions.stream()
            .collect(Collectors.groupingBy(transaction -> transaction.getDateTime().toLocalDate()));

        // For each date group, create a header and add transactions under it
        groupedTransactions.forEach((date, transactionsOnDate) -> {
            // Create and add the header (label) for the date
            Label dateHeader = createDateHeader(date);
            contentPane.getChildren().add(dateHeader);

            // Add each transaction under the date
            for (Transaction transaction : transactionsOnDate) {
                TransactionListItem item = new TransactionListItem(transaction);  // Assuming this class exists
                item.setListView(this);
                contentPane.getChildren().add(item);
            }
        });
    }

    // Create a simple label for the date header
    private Label createDateHeader(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        Label dateHeader = new Label(date.format(formatter));
        dateHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");  // Styling for the header
        return dateHeader;
    }
}
