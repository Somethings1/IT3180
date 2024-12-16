package gui.pages;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import gui.app.App;
import gui.components.chart.BudgetProgressBar;
import gui.components.chart.DoughnutChart;
import gui.components.chart.util.GraphDataPoint;
import gui.components.util.BalanceLabel;
import gui.components.util.Modal;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import gui.components.chart.SmoothedLineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import server.model.Transaction;
import server.model.Category;

public class AnalysisPage extends VBox {
    private static final String INCOME_MODE = "Income";
    private static final String EXPENSE_MODE = "Expense";

    private final App app;
    private VBox categoryListPane;
    private String currentMode;

    // Toggle buttons moved to private fields
    private ToggleButton incomeButton;
    private ToggleButton expenseButton;

    public AnalysisPage() {
        app = App.getInstance();
        currentMode = EXPENSE_MODE; // Default mode
        setupLayout();
    }

    private void setupLayout() {
        getStyleClass().add("main-layout");
        refreshPage();
    }

    private VBox createHeaderPane() {
        VBox headerPane = new VBox();
        headerPane.getStyleClass().add("analysis-header");

        // Page title
        Label titleLabel = new Label("Analysis for " + getMonthYearString());
        titleLabel.getStyleClass().add("page-title");
        headerPane.getChildren().add(titleLabel);

        // Combined row for navigation and mode toggle buttons
        HBox buttonRow = new HBox();
        buttonRow.getStyleClass().add("button-row");

        // Initialize toggle buttons
        ToggleGroup modeToggleGroup = new ToggleGroup();
        incomeButton = new ToggleButton(INCOME_MODE);
        incomeButton.setToggleGroup(modeToggleGroup);
        incomeButton.getStyleClass().addAll("analysis-toggle-button", "income-toggle", "fill-neutral", "border-neutral");

        expenseButton = new ToggleButton(EXPENSE_MODE);
        expenseButton.setToggleGroup(modeToggleGroup);
        expenseButton.getStyleClass().addAll("analysis-toggle-button", "expense-toggle", "fill-neutral", "border-neutral");

        incomeButton.setOnAction(e -> changeMode(INCOME_MODE));
        expenseButton.setOnAction(e -> changeMode(EXPENSE_MODE));
        updateToggleButtonStyle(currentMode);

        // Add toggle buttons to the row
        buttonRow.getChildren().addAll(incomeButton, expenseButton);

        // Spacer to push navigation buttons to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        buttonRow.getChildren().add(spacer);

        // Navigation buttons
        Button prevButton = createPrevButton();
        Button nextButton = createNextButton();
        buttonRow.getChildren().addAll(prevButton, nextButton);
        updateNavigationButtonsState(nextButton);

        // Add the combined row to the header pane
        headerPane.getChildren().add(buttonRow);

        return headerPane;
    }

    private Button createPrevButton() {
        Button prevButton = new Button("<");
        prevButton.getStyleClass().addAll("nav-button", "prev-button", "fill-neutral", "border-neutral");
        prevButton.setOnAction(e -> navigateToPreviousMonth());
        return prevButton;
    }

    private Button createNextButton() {
        Button nextButton = new Button(">");
        nextButton.getStyleClass().addAll("nav-button", "next-button", "fill-neutral", "border-neutral");
        nextButton.setOnAction(e -> navigateToNextMonth());
        return nextButton;
    }

    private void updateNavigationButtonsState(Button nextButton) {
        nextButton.setDisable(app.latestTime());
    }

    private void navigateToPreviousMonth() {
        app.prevTimeStamp();
        refreshPage();
    }

    private void navigateToNextMonth() {
        app.nextTimeStamp();
        refreshPage();
    }

    private String getMonthYearString() {
        Month monthEnum = Month.of(app.getMonth());
        return monthEnum.getDisplayName(TextStyle.FULL, Locale.ENGLISH) + ", " + app.getYear();
    }

    private void changeMode(String newMode) {
        if (!currentMode.equals(newMode)) {
            currentMode = newMode;
            refreshPage();
            updateToggleButtonStyle(currentMode);
        }
    }

    private void updateToggleButtonStyle(String activeMode) {
        incomeButton.getStyleClass().remove("active");
        expenseButton.getStyleClass().remove("active");

        if (INCOME_MODE.equals(activeMode)) {
            incomeButton.getStyleClass().add("active");
        } else if (EXPENSE_MODE.equals(activeMode)) {
            expenseButton.getStyleClass().add("active");
        }
    }

    private HBox createContentPane(ObservableList<PieChart.Data> chartData) {
        HBox contentPane = new HBox();
        contentPane.getStyleClass().add("analysis-content");

        if (chartData.isEmpty()) {
            // Create a label to display "No data"
            Label noDataLabel = new Label("No data");
            noDataLabel.getStyleClass().add("no-data-label"); // Optional: add a style class for better styling
            noDataLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: gray;"); // Style the label as needed
            Pane pane = new Pane(noDataLabel);
            HBox.setHgrow(pane, Priority.ALWAYS);

            // Center the label within the content pane
            contentPane.getChildren().add(pane);
        } else {

            DoughnutChart categoryChart = new DoughnutChart(chartData);
            
            HBox.setHgrow(categoryChart, Priority.ALWAYS);

            contentPane.getChildren().add(categoryChart);
        }
        
     // Create category list pane with a CSS-defined fixed width
        categoryListPane = createCategoryListPane();
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(categoryListPane);
        scrollPane.getStyleClass().addAll("scroll-pane", "edge-to-edge", "category-list-scroll", "no-fill");
        contentPane.getChildren().add(scrollPane);
        return contentPane;
    }

    private ObservableList<PieChart.Data> prepareChartData() {
        ObservableList<PieChart.Data> chartData = FXCollections.observableArrayList();
        List<Transaction> transactions = app.getTransactionList().stream()
            .filter(t -> currentMode.equals(t.getType()) && !t.getType().equals("Transfer"))
            .collect(Collectors.toList());

        Map<String, Double> categoryTotals = new HashMap<>();

        List<Category> categories = currentMode.equals(INCOME_MODE)
            ? app.getIncomeCategoryList()
            : app.getExpenseCategoryList();

        for (Transaction transaction : transactions) {
            int categoryId = transaction.getCategory();
            String categoryName = categories.stream()
                .filter(cat -> cat.getId() == categoryId)
                .map(Category::getName)
                .findFirst()
                .orElse("Unknown");

            categoryTotals.merge(categoryName, transaction.getAmount(), Double::sum);
        }

        categoryTotals.forEach((name, total) -> 
            chartData.add(new PieChart.Data(name, total))
        );

        return chartData;
    }

    private VBox createCategoryListPane() {
        // Create the main VBox for the category list
        categoryListPane = new VBox();
        categoryListPane.getStyleClass().add("category-list");

        // Title for the category list
        Label categoryLabel = new Label("Categories");
        categoryLabel.getStyleClass().add("category-list-title");
        categoryListPane.getChildren().add(categoryLabel);

        // Get categories based on the current mode
        List<Category> categories = currentMode.equals(INCOME_MODE)
            ? app.getIncomeCategoryList()
            : app.getExpenseCategoryList();

        // Tổng số cho mỗi categories
        Map<Integer, Double> categoryTotals = app.getTransactionList().stream()
            .filter(t -> currentMode.equals(t.getType()) && !t.getType().equals("Transfer"))
            .collect(Collectors.groupingBy(Transaction::getCategory, 
                Collectors.summingDouble(Transaction::getAmount)));

        // Phân loại các categories có và không có ngân sách
        List<Category> categoriesWithBudget = categories.stream()
            .filter(category -> category.getBudget() != 0)
            .collect(Collectors.toList());
        List<Category> categoriesWithoutBudget = categories.stream()
            .filter(category -> category.getBudget() == 0)
            .collect(Collectors.toList());

        // Thêm categories có ngân sách trước
        for (Category category : categoriesWithBudget) {
            addCategoryItem(category, categoryTotals.getOrDefault(category.getId(), 0.0));
        }

        // Thêm categories k có ngân sách sau
        for (Category category : categoriesWithoutBudget) {
            addCategoryItem(category, categoryTotals.getOrDefault(category.getId(), 0.0));
        }
        
        return categoryListPane;
    }

    private Popup createCategoryTooltip(Category category, double total) {
        Popup popup = new Popup();
        LocalDate today = LocalDate.now();
        int currentDay = today.getDayOfMonth();
        int totalDaysInMonth = today.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();

        double dailyBudget = category.getBudget() / totalDaysInMonth;

        // If you need to track actual spending separately, do it here

        // Create the content for the popup
        VBox popupContent = new VBox();

        // Show budget, total days in month, daily spending, total spent
        popupContent.getChildren().add(new Label("Budget: " + Math.round(category.getBudget())));
        popupContent.getChildren().add(new Label("Amount spent per day: " + Math.round(dailyBudget)));
        popupContent.getChildren().add(new Label("Total amount spent: " + Math.round(total)));

        String status;
        Label statusLabel = new Label("Expensed: ");

        if (total > dailyBudget * currentDay) {
            status = "Exceeded target";
            statusLabel.setTextFill(Color.RED); // Đặt màu chữ là đỏ
        } else if (total == dailyBudget * currentDay) {
            status = "Enough";
            statusLabel.setTextFill(Color.YELLOW); // Đặt màu chữ là vàng
        } else {
            status = "Can spend more";
            statusLabel.setTextFill(Color.GREEN); // Đặt màu chữ là xanh
        }

        statusLabel.setText("Expensed: " + status); // Cập nhật nội dung label

        popupContent.getChildren().add(statusLabel);

        popupContent.getStyleClass().add("popup-content");
        popup.getContent().add(popupContent);

        return popup;
    }

    private void addCategoryItem(Category category, double total) {
        HBox categoryItem = new HBox();
        categoryItem.getStyleClass().addAll("item", "category-item");

        // Label for category name
        Label nameLabel = new Label(category.getName());
        nameLabel.getStyleClass().add("category-name");

        // Tạo tooltips dạng popup chỉ cho tháng hiện tại
        if (category.getBudget() != 0 && app.latestTime()) {
            Popup popup = createCategoryTooltip(category, total);
            categoryItem.setOnMouseEntered(event -> popup.show(categoryItem, event.getScreenX(), event.getScreenY()));
            categoryItem.setOnMouseExited(event -> popup.hide());
        }

        // Spacer to push the total label to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        categoryItem.getChildren().addAll(nameLabel, spacer);

        // When category is clicked, show modal with line chart
        categoryItem.setOnMouseClicked(e -> showCategoryModal(category));

        if (category.getBudget() != 0) {
            BudgetProgressBar progressBar = new BudgetProgressBar(200, 30);
            progressBar.getStyleClass().add("budget-progress");

            // Check if we're at the latest time to set progress correctly
            if (app.latestTime()) {
                LocalDate today = LocalDate.now();
                int currentDay = today.getDayOfMonth();
                int totalDaysInMonth = today.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();
                progressBar.updateProgress(total, category.getBudget(), currentDay, totalDaysInMonth);
            } else {
                progressBar.updateProgress(total, category.getBudget(), 1, 1);
            }

            categoryItem.getChildren().addAll(progressBar);
        } else {
            BalanceLabel totalLabel = new BalanceLabel(total, true);
            totalLabel.getStyleClass().add("category-total");
            categoryItem.getChildren().add(totalLabel);
        }

        categoryListPane.getChildren().add(categoryItem);
    }

    private void showCategoryModal(Category category) {
        Modal modal = new Modal();

        // Khởi tạo SmoothedLineChart thay vì LineChart
        SmoothedLineChart smoothedLineChart = new SmoothedLineChart();

        // Lấy danh sách các giao dịch theo category và mode hiện tại
        List<Transaction> transactions = app.getTransactionList().stream()
                .filter(t -> t.getCategory() == category.getId() && t.getType().equals(currentMode))
                .collect(Collectors.toList());

        // Lưu trữ dữ liệu hàng ngày
        Map<Integer, Double> dailySpending = new HashMap<>();
        long totalSpent = 0;
        for (Transaction transaction : transactions) {
            int dayOfMonth = transaction.getDateTime().getDayOfMonth();
            double amount = transaction.getAmount();
            dailySpending.put(dayOfMonth, dailySpending.getOrDefault(dayOfMonth, 0.0) + amount);
            totalSpent += (long) amount;
        }

        // Chuẩn bị dữ liệu cho biểu đồ
        List<GraphDataPoint> graphDataPoints = new ArrayList<>();
        for (int day = 1; day <= LocalDate.now().getDayOfMonth(); day++) {
            double totalForDay = dailySpending.getOrDefault(day, 0.0);
            graphDataPoints.add(new GraphDataPoint(LocalDate.now().withDayOfMonth(day), totalForDay));
        }

        // Thêm dữ liệu vào biểu đồ
        smoothedLineChart.addSeries(graphDataPoints,category.getName(), Color.BLUE);

        // Tạo label để hiển thị tổng số tiền đã tiêu
        Label totalSpentLabel = new Label("Total: " + totalSpent);
        totalSpentLabel.setAlignment(Pos.CENTER);
        totalSpentLabel.setMaxWidth(Double.MAX_VALUE);

        // Tạo VBox để chứa cả biểu đồ và label
        VBox vbox = new VBox(smoothedLineChart, totalSpentLabel);
        modal.setContent(vbox);
        modal.show();
    }

    private void refreshPage() {
        getChildren().clear();
        getChildren().add(createHeaderPane());
        ObservableList<PieChart.Data> chartData = prepareChartData();
        getChildren().add(createContentPane(chartData));
        OverviewPage.getInstance().requestReloading();
    }
}