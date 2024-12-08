package gui.components.chart.util.hansolo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import static gui.components.chart.util.hansolo.SmoothedChart.TRANSPARENT_BACKGROUND;

import java.util.Random;

import gui.components.chart.util.hansolo.SmoothedChart.ChartType;


/**
 * User: hansolo
 * Date: 03.11.17
 * Time: 04:42
 */
public class OriginalDemo extends Application {
    private XYChart.Series<String, Number> series1;
    private SmoothedChart<String, Number>  lineChartNotSmoothed;


    @Override public void init() {
        series1 = new XYChart.Series<String, Number>();
        series1.setName("Series 1");
        series1.getData().add(new XYChart.Data("MO", 24));
        series1.getData().add(new XYChart.Data("TU", 20));
        series1.getData().add(new XYChart.Data("WE", 23));
        series1.getData().add(new XYChart.Data("TH", 25));
        series1.getData().add(new XYChart.Data("FR", 21));
        series1.getData().add(new XYChart.Data("SA", 18));
        series1.getData().add(new XYChart.Data("SU", 20));

        CategoryAxis xAxis1 = new CategoryAxis();
        NumberAxis   yAxis1 = new NumberAxis();

        lineChartNotSmoothed = new SmoothedChart<>(xAxis1, yAxis1);
        lineChartNotSmoothed.getData().addAll(series1);
        lineChartNotSmoothed.setSymbolsVisible(series1, false);
        lineChartNotSmoothed.setSeriesColor(series1, Color.MAGENTA);
        lineChartNotSmoothed.setSymbolsVisible(false);
        lineChartNotSmoothed.setSmoothed(false);
        lineChartNotSmoothed.setChartType(ChartType.LINE);
        lineChartNotSmoothed.setInteractive(true);
        lineChartNotSmoothed.setSubDivisions(8);
        lineChartNotSmoothed.setSnapToTicks(false);
        lineChartNotSmoothed.setLegendVisible(false);
        lineChartNotSmoothed.getChartPlotBackground().setBackground(TRANSPARENT_BACKGROUND);
        lineChartNotSmoothed.getHorizontalGridLines().setStroke(Color.rgb(0, 0, 0, 0.5));
        lineChartNotSmoothed.getVerticalGridLines().setStroke(Color.rgb(0, 0, 0, 0.5));
        lineChartNotSmoothed.getHorizontalZeroLine().setStroke(Color.RED);
        lineChartNotSmoothed.getVerticalZeroLine().setStroke(Color.BLUE);
        lineChartNotSmoothed.setAxisTickMarkFill(Color.rgb(255, 255, 0, 0.5));
        lineChartNotSmoothed.setTickLabelFill(Color.MAGENTA);
        lineChartNotSmoothed.setXAxisBorderColor(Color.CYAN);     // set to Color.TRANSPARENT to see the horizontalZeroLine
        lineChartNotSmoothed.setYAxisBorderColor(Color.DARKBLUE); // set to Color.TRANSPARENT to see the verticalZeroLine


        lineChartNotSmoothed.addEventHandler(SmoothedChartEvent.DATA_SELECTED, e -> System.out.println("Selected value: " + e.getyValue()));
    }

    @Override public void start(Stage stage) {
        GridPane pane = new GridPane();
        pane.setBackground(new Background(new BackgroundFill(Color.DARKGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        pane.setPadding(new Insets(10));
        pane.setHgap(10);
        pane.setVgap(10);
        pane.add(lineChartNotSmoothed, 0, 0);


        Scene scene = new Scene(pane);

        stage.setTitle("Smooth Charts");
        stage.setScene(scene);
        stage.show();
    }

    @Override public void stop() {
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}