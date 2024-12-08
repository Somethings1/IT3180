package gui.components.chart;

import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

public class BudgetProgressBar extends StackPane {
    private final Pane progressPane;

    public BudgetProgressBar(double width, double height) {
        setMaxSize(width, height);
        getStyleClass().add("budget-progress-bar");
        
        Rectangle clip = new Rectangle(width, height);
        clip.setArcWidth(height);
        clip.setArcHeight(height);
        Rectangle background = new Rectangle(width, height);
        background.setArcHeight(height); 
        background.setArcWidth(height);
        background.getStyleClass().add("progress-background");

        progressPane = new Pane();
        progressPane.getStyleClass().add("progress-pane");
        progressPane.setClip(clip);

        StackPane.setAlignment(progressPane, Pos.CENTER_LEFT);

        getChildren().addAll(background, progressPane);
    }

    public void updateProgress(double spending, double budget, int currentDay, int totalDays) {
        double progressRatio = spending / budget;
        double expectedRatio = (double) currentDay / totalDays;

        double progressWidth = Math.min(progressRatio * getMaxWidth(), getMaxWidth());
        progressPane.setMaxWidth(progressWidth);

        updateProgressColor(progressRatio, expectedRatio);
    }

    private void updateProgressColor(double progress, double expected) {
        progressPane.getStyleClass().removeAll("background-green", "background-yellow", "background-red");
        if (progress <= expected) {
            progressPane.getStyleClass().add("background-green");
        } else if (progress <= 1) {
            progressPane.getStyleClass().add("background-yellow");
        } else {
            progressPane.getStyleClass().add("background-red");
        }
    }
}