package smartHome.javafx.Controllers;


import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.chart.*;
import javafx.scene.Node;
import javafx.application.Platform;
import smartHome.app.ReportManager;
import smartHome.javafx.Scene.SceneManager;
import java.util.List;
import java.util.Map;

public class ReportController {

    private VBox root;
    private TextArea reportArea;
    private ReportManager reportManager;

    public ReportController() {
        reportManager = new ReportManager();
        
        root = new VBox(20);
        root.getStyleClass().add("root-report");

        // Header
        HBox header = new HBox(20);
        header.getStyleClass().add("header-container");
        
        Button backBtn = new Button("← Back");
        backBtn.getStyleClass().add("back-button");
        backBtn.setOnAction(e -> SceneManager.switchScene("Dashboard"));

        Label title = new Label("Admin Reports & Analytics");
        title.getStyleClass().add("report-title");

        header.getChildren().addAll(backBtn, title);
        
        TabPane tabs = new TabPane();
        tabs.getStyleClass().add("report-tab-pane");
        
        Tab tabLogs = new Tab("Event Logs");
        tabLogs.setClosable(false);
        tabLogs.setContent(createLogView());
        
        Tab tabCharts = new Tab("Analytics Graphs");
        tabCharts.setClosable(false);
        tabCharts.setContent(createAnalyticsView());
        
        tabs.getTabs().addAll(tabCharts, tabLogs);

        root.getChildren().addAll(header, tabs);
        
        // Load CSS
        root.getStylesheets().add(getClass().getResource("/smartHome/javafx/Css/report.css").toExternalForm());
    }

    private ComboBox<String> periodCombo;
    private BarChart<String, Number> barChart;

    private VBox createLogView() {
        VBox box = new VBox(10);
        box.getStyleClass().add("log-view-box");
        
        HBox top = new HBox(10);
        periodCombo = new ComboBox<>();
        periodCombo.getItems().addAll("Daily", "Weekly", "Monthly", "Yearly");
        periodCombo.setValue("Daily");
        periodCombo.setOnAction(e -> refreshAll());

        Button refresh = new Button("Refresh Data");
        refresh.getStyleClass().add("refresh-button");
        refresh.setOnAction(e -> refreshAll());
        
        top.getChildren().addAll(new Label("Period:"), periodCombo, refresh);

        reportArea = new TextArea();
        reportArea.setEditable(false);
        reportArea.setPrefHeight(350);
        reportArea.getStyleClass().add("log-text-area");
        
        box.getChildren().addAll(top, reportArea);
        
        return box;
    }

    private VBox createAnalyticsView() {
        VBox box = new VBox(15);
        box.getStyleClass().add("analytics-box");
        
        Label chartTitle = new Label("Activity Metrics by Module");
        chartTitle.getStyleClass().add("chart-title-label");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Modules");
        yAxis.setLabel("Event Count");

        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setLegendVisible(false);
        barChart.setAnimated(false); 
        
        box.getChildren().addAll(chartTitle, barChart);
        return box;
    }

    private void refreshAll() {
        updateLogText();
        updateChart();
    }

    private void updateLogText() {
        String period = periodCombo.getValue();
        List<String> logs = reportManager.getSystemLogs(period);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("--- System Activity (%s) ---\n", period));
        sb.append("TIME  | MODULE         | EVENT\n");
        sb.append("--------------------------------------------------\n");
        for(String log : logs) {
            sb.append(log).append("\n");
        }
        reportArea.setText(sb.toString());
    }

    private void updateChart() {
        String period = periodCombo.getValue();
        Map<String, Integer> dist = reportManager.getModuleActivityDistribution(period);
        
        barChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        
        String[] modules = {"Security", "Child Monitor", "Device Controls"};
        String[] colors = {"#ef4444", "#3b82f6", "#10b981"}; // Red, Blue, Green

        for (int i = 0; i < modules.length; i++) {
            String mod = modules[i];
            int count = dist.getOrDefault(mod, 0);
            XYChart.Data<String, Number> data = new XYChart.Data<>(mod, count);
            series.getData().add(data);
        }

        barChart.getData().add(series);

        Platform.runLater(() -> {
            for (int i = 0; i < modules.length; i++) {
                if (series.getData().size() > i) {
                    Node node = series.getData().get(i).getNode();
                    if (node != null) {
                        node.setStyle("-fx-bar-fill: " + colors[i] + ";");
                    }
                }
            }
        });
    }

    public VBox getView() {
        refreshAll(); // Ensure data is loaded
        return root;
    }
}
