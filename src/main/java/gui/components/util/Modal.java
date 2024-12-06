package gui.components.util;

import javafx.event.Event;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.application.Platform;

public class Modal {

    private final StackPane overlay;
    private final VBox modalContent;

    public Modal() {
        this.overlay = new StackPane();
        this.modalContent = new VBox(10);

        // Style the overlay (semi-transparent background)
        overlay.getStyleClass().add("modal-overlay");
        overlay.setVisible(false); // Initially hidden

        // Set up the layout for the modal content
        modalContent.setMaxWidth(Region.USE_PREF_SIZE);  // Prevent content from growing too wide
        modalContent.setMaxHeight(Region.USE_PREF_SIZE); // Prevent content from growing too tall
        modalContent.getStyleClass().add("modal-content");

        overlay.getChildren().add(modalContent);

        // Close the modal when clicking the overlay
        overlay.setOnMouseClicked(e -> close());

        // Prevent the modal from closing when clicking inside the modal content
        modalContent.setOnMouseClicked(Event::consume);  // Consume the event so it doesn't propagate to the overlay
    }

    // Set content for the modal, allowing any JavaFX Node to be added (e.g., form)
    public void setContent(Node content) {
        modalContent.getChildren().clear();
        modalContent.getChildren().add(content);
    }

    // Show the modal by adding it to the current scene
    public void show() {
        Platform.runLater(() -> {
            // Find the current stage by checking the open windows
            Stage ownerStage = getCurrentStage();
            if (ownerStage == null) {
                throw new IllegalStateException("No open stage found.");
            }

            // Ensure the overlay is added to the owner's scene
            StackPane root = (StackPane) ownerStage.getScene().getRoot();
            if (!root.getChildren().contains(overlay)) {
                root.getChildren().add(overlay);
            }
            overlay.setVisible(true);
        });
    }

    // Close the modal (hide the overlay)
    public void close() {
        overlay.setVisible(false);
    }

    // Add a close button inside the modal
    public void addCloseButton() {
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> close());
        modalContent.getChildren().add(closeButton);
    }

    // Helper method to find the current Stage
    private Stage getCurrentStage() {
        // Get the primary Stage from all open windows
        return javafx.stage.Window.getWindows().stream()
                .filter(window -> window instanceof Stage)
                .map(window -> (Stage) window)
                .findFirst()
                .orElse(null);
    }
}
