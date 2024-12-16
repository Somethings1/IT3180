package gui.pages;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import gui.app.App;
import gui.components.chart.SmoothedLineChart;
import gui.components.chart.util.GraphDataConverter;
import gui.components.chart.util.GraphDataPoint;
import gui.components.form.transaction.AddTransactionForm;
import gui.components.transaction.list.TransactionListView;
import gui.components.util.BalanceLabel;
import gui.components.util.Modal;
import gui.components.util.RoundedPane;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import server.model.Account;
import server.model.Transaction;

public class OverviewPage extends BorderPane {
    private static final double VBOX_SPACING = 20;
    private static final double SUMMARY_PANE_SPACING = 20;
    private static final double DETAILS_PANE_SPACING = 20;
    private static final String INCOME_TYPE = "Income";
    private static final String EXPENSE_TYPE = "Expense";

    private final App app;
    private VBox headerPane;
    private HBox summaryPane;
    private HBox detailsPane;
    private RoundedPane totalIncomePane;
    private RoundedPane totalExpensePane;
    private RoundedPane balancePane;
    private RoundedPane transactionListPane;
    private RoundedPane incomeChart;
    private RoundedPane expenseChart; 
    private VBox chartsPane;
    private VBox mainLayout;

    private final BooleanProperty reloadRequest = new SimpleBooleanProperty(false);
    private static OverviewPage overviewPage;

    private OverviewPage() {
        app = App.getInstance();
        setupLayout();
        getStyleClass().add("main-layout");
        
        reloadRequest.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                reload();  // Reload the page when the value changes to true
                reloadRequest.set(false);  // Reset the property to false after reloading
            }
        });
    }
    
    public static OverviewPage getInstance() {
    	if (overviewPage == null) overviewPage = new OverviewPage();
    	return overviewPage;
    }
    
    public void requestReloading () {
    	reloadRequest.set(true);
    }
    
    private void reload () {
    	this.getChildren().clear();
    	setupLayout();
    }

    private void setupLayout() {
        mainLayout = createMainLayout();
        loadHeader();
        loadSummaryPane();
        loadDetailsPane();
        mainLayout.getChildren().addAll(headerPane, summaryPane, detailsPane);
        setCenter(createScrollPane(mainLayout));
    }

    private void loadNavigationButtons() {
        HBox navigationButtons = new HBox();
        navigationButtons.setAlignment(Pos.TOP_RIGHT);
        navigationButtons.getStyleClass().add("navigation-buttons");
        
        Button prevButton = createPrevButton();
        Button nextButton = createNextButton();
        
        navigationButtons.getChildren().addAll(prevButton, nextButton);
        headerPane.getChildren().add(navigationButtons); // Add to header or another appropriate layout
        updateNavigationButtonsState(nextButton); // Update button state on load
    }

    private Button createPrevButton() {
        Button prevButton = new Button("<");
        prevButton.getStyleClass().addAll("nav-button", "prev-button", "border-neutral", "fill-neutral"); // Add both classes
        prevButton.setOnAction(e -> {
            app.prevTimeStamp(); // Call method to go to previous timestamp
            reloadOverviewPage(); // Reload the overview page
        });
        return prevButton;
    }

    private Button createNextButton() {
        Button nextButton = new Button(">");
        nextButton.getStyleClass().addAll("nav-button", "next-button", "border-neutral", "fill-neutral"); // Add both classes
        nextButton.setOnAction(e -> {
            app.nextTimeStamp(); // Call method to go to next timestamp
            reloadOverviewPage(); // Reload the overview page
        });
        return nextButton;
    }


    private void updateNavigationButtonsState(Button nextButton) {
        nextButton.setDisable(app.latestTime()); // Disable the next button if at the latest timestamp
    }

    private void reloadOverviewPage() {
        // Update the header, summary, and details without clearing navigation buttons
        loadHeader(); // Reload the header (it will not clear the buttons)
        loadSummaryPane(); // Reload the summary pane
        loadDetailsPane(); // Reload the details pane
        mainLayout.getChildren().clear(); // Clear current main layout
        mainLayout.getChildren().addAll(headerPane, summaryPane, detailsPane); // Add updated components
    }

    private VBox createMainLayout() {
        return new VBox(VBOX_SPACING);
    }

    private ScrollPane createScrollPane(VBox content) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setContent(content);
        scrollPane.getStyleClass().addAll("edge-to-edge", "no-fill");
        return scrollPane;
    }

    // Header Pane
    private void loadHeader() {
        headerPane = new VBox(10);
        headerPane.getStyleClass().add("header-pane");
        headerPane.getChildren().add(createPageTitle());
        loadNavigationButtons();
    }

    private Label createPageTitle() {
        String titleText = "Overview of " + getMonthYearString();
        Label pageTitle = new Label(titleText);
        pageTitle.getStyleClass().add("page-title");
        return pageTitle;
    }

    private String getMonthYearString() {
        Month monthEnum = Month.of(app.getMonth());
        return monthEnum.getDisplayName(TextStyle.FULL, Locale.ENGLISH) + ", " + app.getYear();
    }


    // Summary Pane
    private void loadSummaryPane() {
        summaryPane = new HBox(SUMMARY_PANE_SPACING);
        summaryPane.getStyleClass().add("summary-pane");
        setupSummaryPanes();
        summaryPane.getChildren().addAll(totalIncomePane, totalExpensePane, balancePane);
    }

    private void setupSummaryPanes() {
        setupTotalIncomePane();
        setupTotalExpensePane();
        setupBalancePane();
    }

    private void setupTotalIncomePane() {
        totalIncomePane = new RoundedPane("Total Income");
        BalanceLabel incomeAmount = new BalanceLabel(calculateTotalIncome());
        incomeAmount.getStyleClass().add("balance-label");
        totalIncomePane.getChildren().add(incomeAmount);
        HBox.setHgrow(totalIncomePane, Priority.ALWAYS);
    }

    private double calculateTotalIncome() {
        return app.getTransactionList().stream()
                .filter(t -> INCOME_TYPE.equals(t.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    private void setupTotalExpensePane() {
        totalExpensePane = new RoundedPane("Total Expense");
        BalanceLabel expenseAmount = new BalanceLabel(calculateTotalExpense());
        expenseAmount.getStyleClass().add("balance-label");
        totalExpensePane.getChildren().add(expenseAmount);
        HBox.setHgrow(totalExpensePane, Priority.ALWAYS);
    }

    private double calculateTotalExpense() {
        return -app.getTransactionList().stream()
                .filter(t -> EXPENSE_TYPE.equals(t.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    private void setupBalancePane() {
        balancePane = new RoundedPane("Total Balance");
        BalanceLabel balanceAmount = new BalanceLabel(calculateBalance());
        balanceAmount.update(calculateBalance(), true);
        balanceAmount.getStyleClass().add("balance-label");
        balancePane.getChildren().add(balanceAmount);
        HBox.setHgrow(balancePane, Priority.ALWAYS);
    }

    private double calculateBalance() {
        return app.getAccountList().stream()
                .mapToDouble(Account::getTimeRelatedBalance)
                .sum();
    }

    // Details Pane
    private void loadDetailsPane() {
        detailsPane = new HBox(DETAILS_PANE_SPACING);
        detailsPane.getStyleClass().add("details-pane");
        setupTransactionListPane();
        setupChartsPane();
        detailsPane.getChildren().addAll(transactionListPane, chartsPane);
        VBox.setVgrow(detailsPane, Priority.ALWAYS);
    }

    private void setupTransactionListPane() {
        transactionListPane = new RoundedPane("Transaction List");
        StackPane transactionListStackPane = createTransactionListStackPane();
        transactionListPane.getChildren().add(transactionListStackPane);
        HBox.setHgrow(transactionListPane, Priority.ALWAYS);
    }

    private StackPane createTransactionListStackPane() {
        TransactionListView transactionListView = createTransactionListView();
        StackPane stackPane = new StackPane(transactionListView, createAddTransactionButton(transactionListView));
        StackPane.setAlignment(stackPane.getChildren().get(1), Pos.BOTTOM_CENTER);
        VBox.setVgrow(stackPane, Priority.ALWAYS); // Ensure the stack pane expands vertically
        VBox.setVgrow(transactionListView, Priority.ALWAYS);
        return stackPane;
    }

    private TransactionListView createTransactionListView() {
        List<Transaction> transactionList = app.getTransactionList();
        TransactionListView transactionListView = new TransactionListView(transactionList);
        transactionListView.getStyleClass().add("transaction-list");
        return transactionListView;
    }

    private Button createAddTransactionButton(TransactionListView transactionListView) {
        Button addButton = new Button("+");
        addButton.getStyleClass().addAll("add-button", "border-blue", "fill-blue", "background-neutral");
        addButton.getStyleClass().remove("button");
        addButton.setOnAction(e -> showAddTransactionForm(transactionListView));
        return addButton;
    }

    private void showAddTransactionForm(TransactionListView ignoredTransactionListView) {
    	Modal modal = new Modal();
    	modal.setContent(new AddTransactionForm(modal));
        modal.show();
    }

    private void setupChartsPane() {
        chartsPane = new VBox(VBOX_SPACING);
        chartsPane.getStyleClass().add("charts-pane");
        setupIncomeChart();
        setupExpenseChart();
        chartsPane.getChildren().addAll(incomeChart, expenseChart);
        HBox.setHgrow(chartsPane, Priority.ALWAYS);
    }

    private void setupIncomeChart() {
        incomeChart = new RoundedPane("Income Chart");
        List<Transaction> transactionList = app.getTransactionList();
        List<GraphDataPoint> incomeData = GraphDataConverter.convertToIncome(transactionList);
        
        if (incomeData.isEmpty()) {
            Label noDataLabel = new Label("No data");
            noDataLabel.setTextFill(Color.GRAY);
            incomeChart.getChildren().add(noDataLabel);
        } else {
            SmoothedLineChart incomeLineChart = new SmoothedLineChart();
            incomeLineChart.addSeries(incomeData, INCOME_TYPE, Color.valueOf("#66c2a5"));
            incomeChart.getChildren().add(incomeLineChart);
            incomeLineChart.getStyleClass().add("line-chart");
        }
        VBox.setVgrow(incomeChart, Priority.ALWAYS);
    }

    private void setupExpenseChart() {
        expenseChart = new RoundedPane("Expense Chart");
        List<Transaction> transactionList = app.getTransactionList();
        List<GraphDataPoint> expenseData = GraphDataConverter.convertToExpense(transactionList);
        
        if (expenseData.isEmpty()) {
            Label noDataLabel = new Label("No data");
            noDataLabel.setTextFill(Color.GRAY);
            expenseChart.getChildren().add(noDataLabel);
        } else {
            SmoothedLineChart expenseLineChart = new SmoothedLineChart();
            expenseLineChart.addSeries(expenseData, EXPENSE_TYPE, Color.valueOf("#d53e4f"));
            expenseChart.getChildren().add(expenseLineChart);
            expenseLineChart.getStyleClass().add("line-chart");
        }
        VBox.setVgrow(expenseChart, Priority.ALWAYS);
    }

}
