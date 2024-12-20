import gui.SidebarNavigationPane;
import gui.app.AppSettings;
import gui.components.chart.DoughnutChart;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {
	private void testing (Stage stage) {
		stage.setTitle("Imported Fruits");
        stage.setWidth(500);
        stage.setHeight(500);

        ObservableList<PieChart.Data> pieChartData = createData();

        final DoughnutChart chart = new DoughnutChart(pieChartData);
        chart.setTitle("Imported Fruits");

        Scene scene = new Scene(new StackPane(chart));
        stage.setScene(scene);
        stage.show();
	}
	
	private ObservableList<PieChart.Data> createData() {
        return FXCollections.observableArrayList(
                new PieChart.Data("Grapefruit", 13),
                new PieChart.Data("Oranges", 25),
                new PieChart.Data("Plums", 10),
                new PieChart.Data("Pears", 22),
                new PieChart.Data("Apples", 30));
    }
	
	private void real (Stage primaryStage) {
		SidebarNavigationPane page = new SidebarNavigationPane();
		StackPane root = new StackPane();
		root.getChildren().add(page);
        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("layout.css").toExternalForm());
        if (AppSettings.getInstance().isDarkMode())
        	scene.getStylesheets().add(getClass().getResource("color-dark.css").toExternalForm());
        else
        	scene.getStylesheets().add(getClass().getResource("color-light.css").toExternalForm());
        
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);


        // Set up the stage
        primaryStage.setTitle("Test");
        primaryStage.setScene(scene);
        primaryStage.show();
	}
	
    @Override
    public void start(Stage primaryStage) {
        real(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
