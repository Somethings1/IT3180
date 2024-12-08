package gui.components.chart;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;

public class CircularProgress extends StackPane {
    private Circle backgroundCircle;
    private Circle progressCircle;
    private Label progressText;
    private double progress;

    public CircularProgress(double progress, String title) {
        setupCircles();
        setupText(title);
        this.progress = progress;
        setProgress();
        getStyleClass().add("circular-progress");
    }

    private void setupCircles() {
        backgroundCircle = new Circle();
        backgroundCircle.setStrokeWidth(40);
        backgroundCircle.setFill(Color.TRANSPARENT);
        backgroundCircle.getStyleClass().add("background-circle");

        progressCircle = new Circle();
        progressCircle.setStrokeWidth(40);
        progressCircle.setFill(Color.TRANSPARENT);
        progressCircle.setStrokeLineCap(StrokeLineCap.ROUND);
        progressCircle.getStyleClass().add("progress-circle");
        progressCircle.setRotate(-90);

        getChildren().addAll(backgroundCircle, progressCircle);
        
        setMinSize(200, 200); // Ensure it can grow to fill available space
        setMaxSize(300, 300); // Ensure it can grow to fill available space
    }

    private void setupText(String title) {
        Label titleText = new Label(title);
        titleText.getStyleClass().add("header2");

        progressText = new Label();
        progressText.getStyleClass().add("header1");

        VBox textBox = new VBox(5);
        textBox.getChildren().addAll(titleText, progressText);
        textBox.setAlignment(javafx.geometry.Pos.CENTER);
        getChildren().add(textBox);
    }

    public void setProgress() {
        progress = Math.max(0.0, Math.min(1.0, progress));

        double radius = Math.min(getWidth(), getHeight()) / 2.0;

        backgroundCircle.setRadius(radius);
        progressCircle.setRadius(radius);

        double circumference = 2 * Math.PI * radius;
        progressCircle.getStrokeDashArray().clear();
        progressCircle.getStrokeDashArray().addAll(progress * circumference, circumference);

        updateProgressText();
        
        if (progress == 1) {
        	progressCircle.getStyleClass().add("progress-complete");
        }
    }

    private void updateProgressText() {
        int percentage = (int) (progress * 100);
        progressText.setText(percentage + "%");
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        if (getWidth() > 0 && getHeight() > 0) {
            setProgress();
        }
    }
}
