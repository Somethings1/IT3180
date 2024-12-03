package gui.components.util;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.Region;

public class ErrorModal {

    private final StackPane overlay;

    public ErrorModal(String errorMessage) {
        this.overlay = new StackPane();
        VBox modalContent = new VBox(10);
        HBox buttonRow = new HBox(10);

        // Style the overlay
        overlay.getStyleClass().add("modal-overlay");
        overlay.setVisible(false);

        // Set up the modal content layout
        modalContent.setMaxWidth(Region.USE_PREF_SIZE);
        modalContent.setMaxHeight(Region.USE_PREF_SIZE);
        modalContent.getStyleClass().add("modal-content");

        // Title and message labels
        Label titleLabel = new Label("Error");
        Label messageLabel = new Label(errorMessage);
        titleLabel.getStyleClass().add("header1");
        messageLabel.getStyleClass().add("modal-content-text");

        // Add OK button
        Button okButton = new Button("OK");
        okButton.getStyleClass().addAll("button", "border-neutral", "fill-neutral");
        okButton.setOnAction(e -> close());

        buttonRow.getChildren().add(okButton);
        buttonRow.setAlignment(Pos.CENTER);

        // Add title, message, and button to modal content
        modalContent.getChildren().addAll(titleLabel, messageLabel, buttonRow);
        overlay.getChildren().add(modalContent);

        // Close modal when clicking outside the content
        overlay.setOnMouseClicked(e -> close());
        modalContent.setOnMouseClicked(Event::consume);
    }

    // Show the modal
    public void show() {
        Platform.runLater(() -> {
            Stage ownerStage = getCurrentStage();
            if (ownerStage == null) {
                throw new IllegalStateException("No open stage found.");
            }

            StackPane root = (StackPane) ownerStage.getScene().getRoot();
            if (!root.getChildren().contains(overlay)) {
                root.getChildren().add(overlay);
            }
            overlay.setVisible(true);
        });
    }

    // Close the modal
    public void close() {
        overlay.setVisible(false);
    }

    // Helper to find current stage
    private Stage getCurrentStage() {
        return javafx.stage.Window.getWindows().stream()
                .filter(window -> window instanceof Stage)
                .map(window -> (Stage) window)
                .findFirst()
                .orElse(null);
    }
}
