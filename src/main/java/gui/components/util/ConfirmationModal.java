package gui.components.util;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
public class ConfirmationModal {

    private final StackPane overlay;
    private final Label titleLabel;
    private final Label contentLabel;
    private final HBox buttonRow;
    private boolean result;  // To store the result: true = Yes, false = No

    public ConfirmationModal() {
        this.overlay = new StackPane();
        VBox modalContent = new VBox(10);
        this.buttonRow = new HBox(10);  // Row for buttons

        // Style the overlay (semi-transparent background)
        overlay.getStyleClass().add("modal-overlay");
        overlay.setVisible(false); // Initially hidden

        // Set up the layout for the modal content
        modalContent.setMaxWidth(Region.USE_PREF_SIZE);  // Prevent content from growing too wide
        modalContent.setMaxHeight(Region.USE_PREF_SIZE); // Prevent content from growing too tall
        modalContent.getStyleClass().add("modal-content");

        // Title and content labels
        titleLabel = new Label();
        contentLabel = new Label();
        titleLabel.getStyleClass().add("header1");
        contentLabel.getStyleClass().add("modal-content-text");
        
        addButtons();

        // Add title and content labels to modal content
        modalContent.getChildren().addAll(titleLabel, contentLabel, buttonRow);
        overlay.getChildren().add(modalContent);

        // Close the modal when clicking the overlay
        overlay.setOnMouseClicked(e -> close());

        // Prevent the modal from closing when clicking inside the modal content
        modalContent.setOnMouseClicked(Event::consume);  // Consume the event so it doesn't propagate to the overlay
    }

    // Set content for the modal, including title and content labels
    public void setContent(String title, String content) {
        titleLabel.setText(title);
        contentLabel.setText(content);
    }

    // Add the Yes and No buttons and their actions
    private void addButtons() {
        Button noButton = new Button("No");
        Button yesButton = new Button("Yes");
        
        noButton.getStyleClass().addAll("button", "border-neutral", "fill-neutral");
        yesButton.getStyleClass().addAll("button", "border-red", "fill-red");

        // Set the actions for the buttons
        noButton.setOnAction(e -> {
            result = false;
            close();
        });

        yesButton.setOnAction(e -> {
            result = true;
            close();
        });

        // Add buttons to the button row
        buttonRow.getChildren().addAll(noButton, yesButton);
        buttonRow.setAlignment(Pos.CENTER);
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

    // Get the result of the modal dialog
    public boolean getResult() {
        return result;
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

