package smartHome.javafx.Controllers;

import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import smartHome.app.ReportManager;
import smartHome.db.DatabaseManager;
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

    private VBox createLogView() {
        VBox box = new VBox(10);
        box.getStyleClass().add("log-view-box");
        
        Button refresh = new Button("Refresh Logs");
        refresh.setOnAction(e -> showAllEvents());
        
        reportArea = new TextArea();
        reportArea.setEditable(false);
        reportArea.setPrefHeight(400);
        reportArea.getStyleClass().add("log-text-area");
        
        box.getChildren().addAll(refresh, reportArea);
        
        // Initial load
        showAllEvents();
        return box;
    }

    private VBox createAnalyticsView() {
        VBox box = new VBox(15);
        box.getStyleClass().add("analytics-box");
        
        HBox periodSelector = new HBox(10);
        periodSelector.getStyleClass().add("period-selector");
        ToggleGroup group = new ToggleGroup();
        
        ToggleButton btnDaily = new ToggleButton("Daily");
        btnDaily.setToggleGroup(group);
        btnDaily.setSelected(true);
        
        ToggleButton btnWeekly = new ToggleButton("Weekly");
        btnWeekly.setToggleGroup(group);
        
        ToggleButton btnMonthly = new ToggleButton("Monthly");
        btnMonthly.setToggleGroup(group);
        
        Label pLabel = new Label("Period:");
        pLabel.getStyleClass().add("period-label");
        
        periodSelector.getChildren().addAll(pLabel, btnDaily, btnWeekly, btnMonthly);
        
        // Chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Event Type");
        yAxis.setLabel("Count");
        
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("System Events");
        barChart.setLegendVisible(false);
        
        Runnable updateChart = () -> {
            String period = "Daily";
            if (btnWeekly.isSelected()) period = "Weekly";
            if (btnMonthly.isSelected()) period = "Monthly";
            
            barChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            
            Map<String, Integer> data = DatabaseManager.getEventCounts(period);
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
            
            barChart.getData().add(series);
        };
        
        btnDaily.setOnAction(e -> updateChart.run());
        btnWeekly.setOnAction(e -> updateChart.run());
        btnMonthly.setOnAction(e -> updateChart.run());
        
        // Initial load
        updateChart.run();
        
        box.getChildren().addAll(periodSelector, barChart);
        return box;
    }

    public VBox getView() {
        return root;
    }

    private void showAllEvents() {
        List<String> events = reportManager.generateAllEventsReport();
        StringBuilder sb = new StringBuilder();
        sb.append("--- All Events ---\n");
        for(String e : events) {
            sb.append(e).append("\n");
        }
        reportArea.setText(sb.toString());
    }
}
