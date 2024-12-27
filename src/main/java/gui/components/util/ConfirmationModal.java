package gui.components.util;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.function.Consumer;

public class ConfirmationModal {

    private final StackPane overlay;
    private final Label titleLabel;
    private final Label contentLabel;
    private final HBox buttonRow;
    private boolean result;
    private Consumer<Boolean> callback; // Callback for result

    public ConfirmationModal() {
        this.overlay = new StackPane();
        VBox modalContent = new VBox(10);
        this.buttonRow = new HBox(10);

        // Style the overlay (semi-transparent background)
        overlay.getStyleClass().add("modal-overlay");
        overlay.setVisible(false);

        modalContent.setMaxWidth(Region.USE_PREF_SIZE);
        modalContent.setMaxHeight(Region.USE_PREF_SIZE);
        modalContent.getStyleClass().add("modal-content");

        // Title and content labels
        titleLabel = new Label();
        contentLabel = new Label();
        titleLabel.getStyleClass().add("header1");
        contentLabel.getStyleClass().add("modal-content-text");

        addButtons();

        // Add title, content, and buttons to modal content
        modalContent.getChildren().addAll(titleLabel, contentLabel, buttonRow);
        overlay.getChildren().add(modalContent);

        overlay.setOnMouseClicked(e -> close(false));
        modalContent.setOnMouseClicked(Event::consume);
    }

    public void setContent(String title, String content) {
        titleLabel.setText(title);
        contentLabel.setText(content);
    }

    private void addButtons() {
        Button noButton = new Button("No");
        Button yesButton = new Button("Yes");

        noButton.getStyleClass().addAll("button", "border-neutral", "fill-neutral");
        yesButton.getStyleClass().addAll("button", "border-red", "fill-red");

        noButton.setOnAction(e -> close(false));
        yesButton.setOnAction(e -> close(true));

        buttonRow.getChildren().addAll(noButton, yesButton);
        buttonRow.setAlignment(Pos.CENTER);
    }

    public void show(Consumer<Boolean> callback) {
        this.callback = callback; // Store the callback
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

    private void close(boolean result) {
        this.result = result;
        overlay.setVisible(false);

        if (callback != null) {
            callback.accept(result); // Invoke the callback with the result
        }
    }

    public boolean getResult() {
        return result;
    }

    private Stage getCurrentStage() {
        return javafx.stage.Window.getWindows().stream()
                .filter(window -> window instanceof Stage)
                .map(window -> (Stage) window)
                .findFirst()
                .orElse(null);
    }
}
