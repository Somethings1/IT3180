package gui.pages;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.layout.Priority;

import server.model.Account;
import server.model.Transaction;
import gui.app.App;
import gui.components.chart.SmoothedLineChart;
import gui.components.chart.util.GraphDataConverter;
import gui.components.chart.util.GraphDataPoint;
import gui.components.util.BalanceLabel;

import java.util.List;

public class AccountsPage extends ScrollPane {

    public AccountsPage() {
        VBox content = new VBox();
        content.setSpacing(10);
        getStyleClass().addAll("main-layout", "edge-to-edge");

        // Title
        Label title = new Label("My accounts");
        title.getStyleClass().addAll("page-title"); // Apply CSS style
        content.getChildren().add(title);

        // Fetch accounts from the App instance
        List<Account> accounts = App.getInstance().getAccountList();
        
        // Create an account box for each account
        for (Account account : accounts) {
        	if (account.getGoal() > 0) continue;
            CustomTitledPane accountPane = new CustomTitledPane(account);
            content.getChildren().add(accountPane);
        }

        // Set the content of the ScrollPane
        this.setContent(content);
        this.setFitToWidth(true);
    }
    
    private class CustomTitledPane extends VBox {
        private boolean expanded = false; // Track the expansion state
        private VBox contentBox;
        private SmoothedLineChart chart; // Single chart instance

        public CustomTitledPane(Account account) {
            setPadding(new Insets(5));
            getStyleClass().add("account-pane"); // Apply CSS style

            // Create header
            HBox header = new HBox();
            header.setSpacing(10);
            header.setPadding(new Insets(5));
            header.setStyle("-fx-cursor: hand;"); // Set cursor to hand

            // Account name on the left
            Label accountName = new Label(account.getName());
            accountName.getStyleClass().add("account-name"); // Apply CSS style

            // Growing spacer
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Account balance on the right
            BalanceLabel accountBalance = new BalanceLabel(account.getTimeRelatedBalance(), true);
            accountBalance.getStyleClass().add("account-balance"); // Apply CSS style

            // Arrow for expansion state
            Label arrow = new Label("▼");
            arrow.getStyleClass().add("arrow"); // Apply CSS style

            // Add elements to the header
            header.getChildren().addAll(accountName, spacer, accountBalance, arrow);
            getChildren().add(header); // Add header to the custom pane

            // Create the content VBox for the chart
            contentBox = new VBox();
            contentBox.setPadding(new Insets(10)); // Optional padding for the chart container

            // Initialize the single SmoothedLineChart
            chart = new SmoothedLineChart();

            // Create data points
            List<Transaction> transactions = App.getInstance().getTransactionList();
            List<GraphDataPoint> balanceData = GraphDataConverter.convertToAccountBalance(transactions, account);
            List<GraphDataPoint> incomeData = GraphDataConverter.convertToAccountIncome(transactions, account);
            List<GraphDataPoint> expenseData = GraphDataConverter.convertToAccountExpense(transactions, account);

            // Add series to the single chart
            chart.addSeries(balanceData, "Balance", Color.BLUE);
            chart.addSeries(incomeData, "Income", Color.GREEN);
            chart.addSeries(expenseData, "Expenses", Color.RED);

            // Set the initial state to collapsed
            contentBox.setVisible(expanded);
            contentBox.getChildren().add(chart); // Add the chart directly to the content box
            getChildren().add(contentBox); // Add the content box

            // Add click event to the header to toggle expansion
            header.setOnMouseClicked(event -> toggleContent(arrow));

            // Set contentBox's visibility based on the expansion state
            contentBox.setManaged(false); // Ensure it does not take space when hidden
        }

        private void toggleContent(Label arrow) {
            expanded = !expanded; // Toggle the state
            contentBox.setVisible(expanded); // Show or hide the content box
            arrow.setText(expanded ? "▲" : "▼"); // Update the arrow direction
            contentBox.setManaged(expanded); // Allow it to take space when visible or not
        }
    }
}
