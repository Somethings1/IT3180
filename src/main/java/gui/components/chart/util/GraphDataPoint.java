package gui.components.chart.util;

import java.time.LocalDate;

public class GraphDataPoint {
	private LocalDate date;
    private double value;

    public GraphDataPoint(LocalDate date, double value) {
        this.date = date;
        this.value = value;
    }

    public LocalDate getDate() {
        return date;
    }

    public double getValue() {
        return value;
    }
}
