package gui.pages;

import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import org.controlsfx.control.ToggleSwitch;
import server.model.Account;
import server.model.Category;
import server.service.AccountService;
import server.service.CategoryService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;

import gui.app.App;
import gui.app.AppSettings;
import gui.components.form.ExportDataForm;
import gui.components.form.account.AddAccountForm;
import gui.components.form.account.AddSavingForm;
import gui.components.form.account.EditAccountForm;
import gui.components.form.account.EditSavingForm;
import gui.components.form.category.AddCategoryForm;
import gui.components.form.category.EditCategoryForm;
import gui.components.util.ConfirmationModal;
import gui.components.util.ErrorModal;
import gui.components.util.Modal;

public class SettingsPage extends ScrollPane {

    private VBox content;

    public SettingsPage() {
        initialize();
    }

    /*
     * General Functionality
    */

    public void refresh () {
    	createContent();
    	setContent(content);
    }

    private void initialize() {
        setFitToWidth(true);
        getStyleClass().addAll("main-layout", "edge-to-edge");

        createContent();
        setContent(content);
    }

    private void createContent () {
    	content = new VBox(20);
        content.getChildren().addAll(createTitle(),
                                     createGeneralSection(),
                                     createAccountsAndSavingsSection(),
                                     createCategorySettingsSection(),
                                     createExportSection(),
                                     createDatabaseManagementSection());
    }

    private Label createTitle() {
        Label title = new Label("Settings");
        title.getStyleClass().addAll("page-title");
        return title;
    }

    private Label createHeader(String headerText) {
        Label header = new Label(headerText);
        header.getStyleClass().add("header1");
        return header;
    }

    /*
     * Section: General
    */

    private VBox createGeneralSection() {
        VBox generalSection = new VBox();
        generalSection.getChildren().addAll(createHeader("General"),
                                            createThemeRow(),
                                            createCurrencyRow(),
                                            createCurrencySelectionRow(),
                                            createDecimalPlacesRow());
        return generalSection;
    }

    private HBox createThemeRow() {
        ToggleSwitch themeToggle = createThemeToggle();
        HBox themeRow = new HBox(10, new Label("Dark mode:"), themeToggle);
        themeRow.getStyleClass().add("general-row");
        return themeRow;
    }

    private ToggleSwitch createThemeToggle() {
        ToggleSwitch themeToggle = new ToggleSwitch();
        themeToggle.setSelected(AppSettings.getInstance().isDarkMode());
        themeToggle.selectedProperty().addListener((observable, oldValue, newValue) -> toggleTheme(themeToggle));
        themeToggle.getStyleClass().add("toggle-switch");
        return themeToggle;
    }

    private HBox createCurrencyRow() {
        ToggleSwitch currencyBeforeAmountToggle = createCurrencyBeforeAmountToggle();
        HBox currencyRow = new HBox(10, new Label("Currency before amount:"), currencyBeforeAmountToggle);
        currencyRow.getStyleClass().add("general-row");
        return currencyRow;
    }

    private ToggleSwitch createCurrencyBeforeAmountToggle() {
        ToggleSwitch currencyBeforeAmountToggle = new ToggleSwitch();
        currencyBeforeAmountToggle.setSelected(AppSettings.getInstance().isCurrencyBeforeAmount());
        currencyBeforeAmountToggle.selectedProperty().addListener((observable, oldValue, newValue) -> toggleCurrencyBeforeAmount(currencyBeforeAmountToggle));
        currencyBeforeAmountToggle.getStyleClass().add("toggle-switch");
        return currencyBeforeAmountToggle;
    }

    private HBox createCurrencySelectionRow() {
        ComboBox<String> currencyComboBox = createCurrencyComboBox();
        HBox currencySelectionRow = new HBox(10, new Label("Currency:"), currencyComboBox);
        currencySelectionRow.getStyleClass().add("general-row");
        return currencySelectionRow;
    }

    private ComboBox<String> createCurrencyComboBox() {
        ComboBox<String> currencyComboBox = new ComboBox<>();
        currencyComboBox.getItems().addAll("$", "€", "¥", "£", "A$", "C$", "Fr.", "¥", "kr", "$", "đ");
        currencyComboBox.setValue(AppSettings.getInstance().getCurrency());
        currencyComboBox.valueProperty().addListener((observable, oldValue, newValue) -> updateCurrency(newValue));
        return currencyComboBox;
    }

    private HBox createDecimalPlacesRow() {
        ComboBox<Integer> decimalPlacesComboBox = createDecimalPlacesComboBox();
        HBox decimalPlacesRow = new HBox(10, new Label("Decimal Places:"), decimalPlacesComboBox);
        decimalPlacesRow.getStyleClass().add("general-row");
        return decimalPlacesRow;
    }

    private ComboBox<Integer> createDecimalPlacesComboBox() {
        ComboBox<Integer> decimalPlacesComboBox = new ComboBox<>();
        decimalPlacesComboBox.getItems().addAll(0, 1, 2, 3, 4, 5);
        decimalPlacesComboBox.setValue(AppSettings.getInstance().getNumberOfDecimalPlaces());
        decimalPlacesComboBox.valueProperty().addListener((observable, oldValue, newValue) -> updateDecimalPlaces(newValue));
        return decimalPlacesComboBox;
    }

    private void toggleTheme(ToggleSwitch toggleSwitch) {
        boolean isDarkMode = toggleSwitch.isSelected();
        AppSettings.getInstance().setDarkMode(isDarkMode);

        Scene scene = getScene();
        Node root = scene.getRoot();
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), root);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        fadeOut.setOnFinished(e -> {
            // Remove current stylesheet and apply the new one
            if (isDarkMode) {
                scene.getStylesheets().remove(Objects.requireNonNull(getClass().getClassLoader().getResource("color-light.css")).toExternalForm());
                scene.getStylesheets().add(Objects.requireNonNull(getClass().getClassLoader().getResource("color-dark.css")).toExternalForm());
            } else {
                scene.getStylesheets().remove(Objects.requireNonNull(getClass().getClassLoader().getResource("color-dark.css")).toExternalForm());
                scene.getStylesheets().add(Objects.requireNonNull(getClass().getClassLoader().getResource("color-light.css")).toExternalForm());
            }
            fadeIn.play();
        });
        fadeOut.play();
    }

    private void toggleCurrencyBeforeAmount(ToggleSwitch toggleSwitch) {
        boolean isCurrencyBeforeAmount = toggleSwitch.isSelected();
        AppSettings.getInstance().setCurrencyBeforeAmount(isCurrencyBeforeAmount);
    }

    private void updateCurrency(String newCurrency) {
        AppSettings.getInstance().setCurrency(newCurrency);
    }

    private void updateDecimalPlaces(Integer newDecimalPlaces) {
        AppSettings.getInstance().setNumberOfDecimalPlaces(newDecimalPlaces);
    }

    /*
     * Section: Accounts and Savings
    */

    private HBox createAccountsAndSavingsSection() {
        VBox accountsColumn = createAccountOrSavingColumn("Accounts", account -> account.getGoal() == 0, this::addAccount);
        VBox savingsColumn = createAccountOrSavingColumn("Savings", account -> account.getGoal() != 0, this::addSaving);

        HBox section = new HBox(40, accountsColumn, savingsColumn);
        HBox.setHgrow(accountsColumn, Priority.ALWAYS);
        HBox.setHgrow(savingsColumn, Priority.ALWAYS);
        return section;
    }


    private VBox createAccountOrSavingColumn(String titleText, java.util.function.Predicate<Account> filter, Runnable addAction) {
        VBox column = new VBox(0);
        column.getChildren().add(createHeaderWithAddButton(titleText, addAction));

        List<Account> accounts = App.getInstance().getAccountList();
        accounts.stream().filter(filter).map(this::createAccountBox).forEach(column.getChildren()::add);
        return column;
    }


    private HBox createAccountBox(Account account) {
        Label accountName = createAccountNameLabel(account);
        Label editIcon = createEditIcon(account);
        Label trashIcon = createTrashIcon(() -> removeAccount(account.getId()));
        HBox accountBox = new HBox(10, accountName, createSpacer(), editIcon, trashIcon);
        accountBox.getStyleClass().add("item");
        HBox.setHgrow(accountName, Priority.ALWAYS);
        return accountBox;
    }

    private Label createAccountNameLabel(Account account) {
        Label accountName = new Label(account.getName());
        accountName.getStyleClass().add("form-label");
        return accountName;
    }

    private Label createEditIcon(Object item) {
        Label editIcon = new Label();
        editIcon.getStyleClass().add("edit-icon");

        if (item instanceof Account && ((Account) item).getGoal() == 0) {
            editIcon.setOnMouseClicked(e -> editAccount((Account) item));
        } else if (item instanceof Account) {
            editIcon.setOnMouseClicked(e -> editSaving((Account) item));
        } else if (item instanceof Category) {
            editIcon.setOnMouseClicked(e -> editCategory((Category) item));
        }

        return editIcon;
    }

    private Label createTrashIcon(Runnable deleteAction) {
        Label trashIcon = new Label();
        trashIcon.getStyleClass().add("trash-icon");
        trashIcon.setOnMouseClicked(e -> deleteAction.run());
        return trashIcon;
    }

    /*
     * Section: Categories
    */

    private HBox createCategorySettingsSection() {
        VBox incomeCategoryColumn = createCategoryColumn("Income Categories", "income", this::addIncomeCategory);
        VBox expenseCategoryColumn = createCategoryColumn("Expense Categories", "expense", this::addExpenseCategory);

        HBox section = new HBox(40, incomeCategoryColumn, expenseCategoryColumn);
        HBox.setHgrow(incomeCategoryColumn, Priority.ALWAYS);
        HBox.setHgrow(expenseCategoryColumn, Priority.ALWAYS);
        return section;
    }

    private VBox createCategoryColumn(String titleText, String categoryType, Runnable addAction) {
        VBox column = new VBox(0);
        column.getChildren().add(createHeaderWithAddButton(titleText, addAction));

        List<Category> categories = App.getInstance().getCategoryList();
        categories.stream().filter(category -> category.getType().equals(categoryType))
                .map(this::createCategoryBox).forEach(column.getChildren()::add);
        return column;
    }


    private HBox createCategoryBox(Category category) {
        Label categoryName = createCategoryNameLabel(category);
        Label editIcon = createEditIcon(category);
        Label trashIcon = createTrashIcon(() -> removeCategory(category.getId()));
        HBox categoryBox = new HBox(10, categoryName, createSpacer(), editIcon, trashIcon);
        categoryBox.getStyleClass().add("item");
        return categoryBox;
    }

    private Label createCategoryNameLabel(Category category) {
        Label categoryName = new Label(category.getName());
        categoryName.getStyleClass().add("form-label");
        return categoryName;
    }

    /*
     * Section: Export
    */

    private VBox createExportSection() {
        Label header = createHeader("Export");
        Button exportButton = new Button("Export Data");
        exportButton.getStyleClass().addAll("fill-blue", "border-blue");
        exportButton.setOnAction(e -> handleExport());
        return new VBox(10, header, exportButton);
    }

    /*
     * Section: Database management
    */
    private VBox createDatabaseManagementSection() {
        Label header = createHeader("Database Management");

        Button exportButton = new Button("Export Database");
        exportButton.getStyleClass().addAll("border-blue", "fill-blue");
        exportButton.setOnAction(e -> handleExportDatabase());

        Button importButton = new Button("Import Database");
        importButton.getStyleClass().addAll("border-blue", "fill-blue");
        importButton.setOnAction(e -> handleImportDatabase());

        return new VBox(10, header, exportButton, importButton);
    }

    private void handleExportDatabase() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Database");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQLite Database", "*.db"));
        File file = fileChooser.showSaveDialog(getScene().getWindow());

        if (file != null) {
            try {
                Path source = Paths.get("money_management.db");  // Replace with actual path
                Files.copy(source, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                new Alert(Alert.AlertType.INFORMATION, "Database exported successfully.").showAndWait();
            } catch (IOException e) {
                new ErrorModal("Failed to export database: " + e.getMessage()).show();
            }
        }
    }

    private void handleImportDatabase() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Database");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQLite Database", "*.db"));
        File file = fileChooser.showOpenDialog(getScene().getWindow());

        if (file != null) {
            try {
                Path destination = Paths.get("money_management.db");  // Replace with actual path
                Files.copy(file.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                new Alert(Alert.AlertType.INFORMATION, "Database imported successfully.").showAndWait();
                App.getInstance().reload();
                refresh();
                OverviewPage.getInstance().requestReloading();
            } catch (IOException e) {
                new ErrorModal("Failed to import database: " + e.getMessage()).show();
            }
        }
    }


    private Region createSpacer() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }

    private void addAccount() {
        Modal modal = new Modal();
        modal.setContent(new AddAccountForm(modal, this));
        modal.show();
    }

    private void addSaving() {
        Modal modal = new Modal();
        modal.setContent(new AddSavingForm(modal, this)); // Assuming you have a `SavingsPage` instance
        modal.show();
    }

    private void addIncomeCategory() {
        Modal modal = new Modal();
        modal.setContent(new AddCategoryForm(modal, "Income", this));  // Pass "Income" as the type
        modal.show();
    }

    private void addExpenseCategory() {
        Modal modal = new Modal();
        modal.setContent(new AddCategoryForm(modal, "Expense", this));  // Pass "Expense" as the type
        modal.show();
    }


    private void removeAccount(int accountId) {
    	ConfirmationModal modal = new ConfirmationModal();
    	modal.setContent("Remove this account?", "Removing this account will remove all transactions related to this account. Are you sure?");
    	modal.show(result -> {
            if (result) {
                new AccountService().removeAccount(accountId);
                refresh();

            }
        });
    }

    private void removeCategory(int categoryId) {
    	ConfirmationModal modal = new ConfirmationModal();
    	modal.setContent("Remove this category?", "Removing this category will remove all transactions with this category. Are you sure?");
    	modal.show(result -> {
            if (result) {
                new CategoryService().removeCategory(categoryId);
                refresh();
            }
        });
    }

    private void handleExport () {
    	Modal modal = new Modal();
    	modal.setContent(new ExportDataForm(modal));
    	modal.show();
    }

    private void editAccount(Account account) {
        Modal modal = new Modal();
        modal.setContent(new EditAccountForm(modal, this, account));
        modal.show();
    }

    private void editSaving(Account saving) {
        Modal modal = new Modal();
        modal.setContent(new EditSavingForm(modal, this, saving));
        modal.show();
    }

    private void editCategory(Category category) {
        Modal modal = new Modal();
        modal.setContent(new EditCategoryForm(modal, this, category));
        modal.show();
    }

    private HBox createHeaderWithAddButton(String titleText, Runnable addAction) {
        Label header = createHeader(titleText);
        Button addButton = new Button("Add");
        addButton.getStyleClass().addAll("button", "border-blue", "fill-blue");
        addButton.setOnAction(e -> addAction.run());
        HBox headerWithButton = new HBox(10, header, createSpacer(), addButton);
        headerWithButton.getStyleClass().add("header-row");
        return headerWithButton;
    }
}

