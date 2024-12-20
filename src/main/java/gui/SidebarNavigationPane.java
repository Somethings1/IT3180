package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import gui.pages.AccountsPage;
import gui.pages.AnalysisPage;
import gui.pages.OverviewPage;
import gui.pages.SavingsPage;
import gui.pages.SettingsPage;

public class SidebarNavigationPane extends HBox {
    private static final double FIXED_MENU_WIDTH = 250;
    private static final double COLLAPSED_MENU_WIDTH = 55;
    private static final double WIDTH_THRESHOLD = 1000;
    private static final double LOGO_IMAGE_SIZE = 70;
    private static final double NAV_PANE_SPACING = 10;
    private static final double NAV_PANE_LEFT_PADDING = 15;

    private VBox menu;
    private StackPane contentPane;
    private StackPane logoPane;
    private ToggleButton toggleButton;
    private HBox[] navigationPanes;
    private boolean iconOnlyMode = false;
    private Map<String, Supplier<Node>> contentPaneMap;

    public SidebarNavigationPane() {
        initializeContentPaneMap();
        setupLayout();
        setActivePane("Overview");
        getStyleClass().add("main");
    }

    private void initializeContentPaneMap() {
        contentPaneMap = new HashMap<>();
        contentPaneMap.put("Overview", OverviewPage::getInstance);
        contentPaneMap.put("Analysis", AnalysisPage::new);
        contentPaneMap.put("Accounts", AccountsPage::new);
        contentPaneMap.put("Savings", SavingsPage::new);
        contentPaneMap.put("Settings", SettingsPage::new);
    }

    private void setupLayout() {
        menu = createMenu();
        contentPane = createContentPane();
        this.getChildren().addAll(menu, contentPane);
        HBox.setHgrow(contentPane, Priority.ALWAYS);
        this.widthProperty().addListener((obs, oldWidth, newWidth) -> adjustMenuMode(newWidth.doubleValue()));
    }

    private VBox createMenu() {
        VBox menu = new VBox();
        menu.getStyleClass().add("sidebar-menu");
        menu.setPrefWidth(FIXED_MENU_WIDTH);
        menu.getChildren().add(createLogoToggleContainer());
        addNavigationPanes(menu);
        return menu;
    }

    private HBox createLogoToggleContainer() {
        HBox logoToggleContainer = new HBox();
        logoToggleContainer.setAlignment(Pos.CENTER_LEFT);
        logoToggleContainer.setPadding(new Insets(10));
        logoToggleContainer.setPrefHeight(90);

        logoPane = new StackPane(createLogoImageView());
        toggleButton = createToggleButton();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        logoToggleContainer.getChildren().addAll(logoPane, spacer, toggleButton);
        return logoToggleContainer;
    }

    private Label createIcon(String name) {
        Label icon = new Label();
        icon.getStyleClass().addAll("nav-icon", name + "-icon");
        return icon;
    }

    private ImageView createLogoImageView() {
        Image logoImage = new Image(Objects.requireNonNull(getClass().getResource("/logo.png")).toExternalForm());
        ImageView logoImageView = new ImageView(logoImage);
        logoImageView.setFitWidth(LOGO_IMAGE_SIZE);
        logoImageView.setFitHeight(LOGO_IMAGE_SIZE);
        logoImageView.setPreserveRatio(true);
        return logoImageView;
    }

    private void addNavigationPanes(VBox menu) {
        String[] paneNames = {"Overview", "Analysis", "Accounts", "Savings", "Settings"};
        navigationPanes = new HBox[paneNames.length];
        for (int i = 0; i < paneNames.length; i++) {
        	int j = i;
            navigationPanes[i] = createNavigationPane(paneNames[i]);
            navigationPanes[i].setOnMouseClicked(e -> setActivePane(paneNames[j]));
            navigationPanes[i].setCursor(javafx.scene.Cursor.HAND);
            menu.getChildren().add(navigationPanes[i]);
            if (i == 0) {
                VBox.setMargin(navigationPanes[i], new Insets(20, 0, 0, 0));
            }
        }
    }

    private HBox createNavigationPane(String name) {
        HBox navPane = new HBox(NAV_PANE_SPACING);
        navPane.getStyleClass().add("navigation-pane");
        navPane.setPadding(new Insets(0, 0, 0, NAV_PANE_LEFT_PADDING));

        StackPane iconContainer = new StackPane(createIcon(name));

        Label label = new Label(name);
        label.getStyleClass().add("nav-label");

        navPane.getChildren().addAll(iconContainer, label);
        navPane.setAlignment(Pos.CENTER_LEFT);
        return navPane;
    }

    private ToggleButton createToggleButton() {
        ToggleButton button = new ToggleButton("<");
        button.getStyleClass().add("toggle-sidebar-button");
        button.setOnMouseClicked(this::toggleMenuMode);
        return button;
    }

    private StackPane createContentPane() {
        StackPane contentPane = new StackPane();
        contentPane.getStyleClass().add("content-pane");
        return contentPane;
    }

    private void toggleMenuMode(MouseEvent event) {
        iconOnlyMode = !iconOnlyMode;
        updateMenuForToggle();
    }

    private void updateMenuForToggle() {
        if (iconOnlyMode) {
            toggleButton.setText(">");
            menu.setPrefWidth(COLLAPSED_MENU_WIDTH);
            logoPane.getChildren().clear();
            hideLabelsInNavigationPanes();
            menu.getStyleClass().add("collapsed-sidebar-menu");
        } else {
            toggleButton.setText("<");
            menu.setPrefWidth(FIXED_MENU_WIDTH);
            logoPane.getChildren().add(createLogoImageView());
            showLabelsInNavigationPanes();
            menu.getStyleClass().remove("collapsed-sidebar-menu");
        }
    }

    private void hideLabelsInNavigationPanes() {
        for (HBox navPane : navigationPanes) {
            navPane.getChildren().get(1).setVisible(false);
        }
    }

    private void showLabelsInNavigationPanes() {
        for (HBox navPane : navigationPanes) {
            navPane.getChildren().get(1).setVisible(true);
        }
    }

    private void setActivePane(String paneName) {
        contentPane.getChildren().clear();
        loadContentPane(paneName);
        updateActivePaneStyle(paneName);
    }

    private void loadContentPane(String paneName) {
        Supplier<Node> contentSupplier = contentPaneMap.get(paneName);
        if (contentSupplier != null) {
            contentPane.getChildren().add(contentSupplier.get());
        }
    }

    private void updateActivePaneStyle(String paneName) {
        for (HBox navPane : navigationPanes) {
            navPane.getStyleClass().remove("active-nav-pane");
            if (((Label) navPane.getChildren().get(1)).getText().equals(paneName)) {
                navPane.getStyleClass().add("active-nav-pane");
            }
        }
    }

    private void adjustMenuMode(double width) {
        if (width < WIDTH_THRESHOLD && !iconOnlyMode) {
            toggleMenuMode(null);
        } else if (width >= WIDTH_THRESHOLD && iconOnlyMode) {
            toggleMenuMode(null);
        }
    }
}
