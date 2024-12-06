package gui.components.util;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;

public class RoundedPane extends VBox {
    public RoundedPane(String header) {
        setStyle("-fx-border-color: GREY; " +
                "-fx-border-width: 1px; " +
                "-fx-border-radius: 15px;");
        this.setPadding(new Insets(10));
        setHeader(header);
        setMaxWidth(Double.MAX_VALUE);
        setMaxHeight(Double.MAX_VALUE);
    }
    
    void setHeader(String header) {
        Label headerLabel = new Label(header);
        headerLabel.setStyle("-fx-font: bold 20px 'Montserrat';");

        // Add the header label
        this.getChildren().add(headerLabel);

        // Create and add a separator below the header
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0)); // Optional padding around the separator
        VBox.setMargin(separator, new Insets(0, -10, 0, -10));

        this.getChildren().add(separator); // Add separator after the label
    }
}
