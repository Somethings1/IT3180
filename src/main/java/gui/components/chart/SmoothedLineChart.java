package gui.components.chart;

import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import gui.components.chart.util.GraphDataPoint;
import gui.components.chart.util.hansolo.SmoothedChart;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SmoothedLineChart extends StackPane {
    private SmoothedChart<String, Number> smoothedChart;
    private Map<LocalDate, Double> balanceMap;
    private List<ChartSeries> seriesList;

    public SmoothedLineChart() {
        seriesList = new ArrayList<>();

        NumberAxis yAxis = new NumberAxis();
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
            @Override
            public String toString(Number object) {
                return formatNumber(object.doubleValue());
            }
        });

        smoothedChart = new SmoothedChart<>(new CategoryAxis(), yAxis);
        smoothedChart.setMinHeight(0);
        smoothedChart.setMinWidth(0);
        smoothedChart.setStyle("-fx-padding: 0 0 0 -40;");
        init();
    }

    public void addSeries(List<GraphDataPoint> graphDataPoints, String seriesName, Color color) {
        init();
        seriesList.add(new ChartSeries(graphDataPoints, seriesName, color));
        renderAllSeries();
    }

    private void init() {
        smoothedChart.getData().clear();
        this.getChildren().clear();
        this.getChildren().add(smoothedChart);
    }

    private void initializeMap() {
        balanceMap = new TreeMap<>();
    }

    private void processGraphDataPoints(List<GraphDataPoint> graphDataPoints) {
        for (GraphDataPoint point : graphDataPoints) {
            addDataPointToMap(point);
        }
    }

    private void addDataPointToMap(GraphDataPoint point) {
        LocalDate date = point.getDate();
        double amount = point.getValue();
        balanceMap.put(date, balanceMap.getOrDefault(date, 0.0) + amount);
    }

    private void renderAllSeries() {
        List<XYChart.Series<String, Number>> seriesToAdd = new ArrayList<>();

        for (ChartSeries chartSeries : seriesList) {
            initializeMap();
            processGraphDataPoints(chartSeries.getDataPoints());
            XYChart.Series<String, Number> balanceSeries = setChartData(chartSeries.getDataPoints(), chartSeries.getSeriesName());
            seriesToAdd.add(balanceSeries);
        }
        double minValue = balanceMap.values().stream().min(Double::compareTo).orElse(0.0);
        double maxValue = balanceMap.values().stream().max(Double::compareTo).orElse(0.0);

        adjustYAxis(minValue, maxValue);

        smoothedChart.getData().addAll(seriesToAdd);

        for (int i = 0; i < seriesToAdd.size(); i++) {
            setSeriesColor(seriesToAdd.get(i), seriesList.get(i).getColor());
        }
        
        
    }

    private XYChart.Series<String, Number> setChartData(List<GraphDataPoint> graphDataPoints, String seriesName) {
        XYChart.Series<String, Number> balanceSeries = new XYChart.Series<>();
        balanceSeries.setName(seriesName);

        if (isSingleMonth()) {
            setDailySeries(balanceSeries, graphDataPoints);
        } else {
            setMonthlySeries(balanceSeries, graphDataPoints);
        }

        return balanceSeries;
    }

    private void adjustYAxis(double minValue, double maxValue) {
        NumberAxis yAxis = (NumberAxis) smoothedChart.getYAxis();

        //System.out.println(minValue);
        yAxis.setLowerBound(minValue * 0.9); // Set lower bound just below the minimum value for better visual spacing

        yAxis.setUpperBound(Math.ceil(maxValue * 1.1)); // Add 10% margin above max
    }

    private boolean isSingleMonth() {
        return balanceMap.keySet().stream()
                .map(date -> date.withDayOfMonth(1))
                .distinct()
                .count() == 1;
    }

    private void setDailySeries(XYChart.Series<String, Number> balanceSeries, List<GraphDataPoint> graphDataPoints) {
        LocalDate firstDateInData = balanceMap.keySet().stream().min(LocalDate::compareTo).orElse(LocalDate.now());
        LocalDate lastDateInData = balanceMap.keySet().stream().max(LocalDate::compareTo).orElse(LocalDate.now());
        LocalDate firstDayOfMonth = firstDateInData.withDayOfMonth(1);
        LocalDate endDate = lastDateInData;

        for (LocalDate date = firstDayOfMonth; !date.isAfter(endDate); date = date.plusDays(1)) {
            double balanceAmount = balanceMap.getOrDefault(date, 0.0);
            XYChart.Data<String, Number> balanceData = new XYChart.Data<>(date.format(DateTimeFormatter.ofPattern("d")), balanceAmount);
            balanceSeries.getData().add(balanceData);
        }
    }

    private void setMonthlySeries(XYChart.Series<String, Number> balanceSeries, List<GraphDataPoint> graphDataPoints) {
        LocalDate firstDateInData = balanceMap.keySet().stream().min(LocalDate::compareTo).orElse(LocalDate.now());
        LocalDate lastDateInData = balanceMap.keySet().stream().max(LocalDate::compareTo).orElse(LocalDate.now());
        LocalDate firstMonth = firstDateInData.withDayOfMonth(1);
        LocalDate lastMonthInData = lastDateInData.withDayOfMonth(1);

        for (LocalDate date = firstMonth; !date.isAfter(lastMonthInData); date = date.plusMonths(1)) {
            double balanceAmount = 0.0;
            for (Map.Entry<LocalDate, Double> entry : balanceMap.entrySet()) {
                LocalDate entryDate = entry.getKey();
                if (entryDate.getMonth() == date.getMonth() && entryDate.getYear() == date.getYear()) {
                    balanceAmount += entry.getValue();
                }
            }

            XYChart.Data<String, Number> balanceData = new XYChart.Data<>(date.format(DateTimeFormatter.ofPattern("MMM")), balanceAmount);
            balanceSeries.getData().add(balanceData);
        }
    }

    private void setSeriesColor(XYChart.Series<String, Number> series, Color color) {
        smoothedChart.setSeriesColor(series, color);
    }

    private String formatNumber(double value) {
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        if (value >= 1e9) {
            return decimalFormat.format(value / 1e9) + "B";
        } else if (value >= 1e6) {
            return decimalFormat.format(value / 1e6) + "M";
        } else if (value >= 1e3) {
            return decimalFormat.format(value / 1e3) + "K";
        } else {
            return decimalFormat.format(value);
        }
    }

    private static class ChartSeries {
        private final List<GraphDataPoint> dataPoints;
        private final String seriesName;
        private final Color color;

        public ChartSeries(List<GraphDataPoint> dataPoints, String seriesName, Color color) {
            this.dataPoints = dataPoints;
            this.seriesName = seriesName;
            this.color = color;
        }

        public List<GraphDataPoint> getDataPoints() {
            return dataPoints;
        }

        public String getSeriesName() {
            return seriesName;
        }

        public Color getColor() {
            return color;
        }
    }
}
