package smartHome.javafx.Controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import smartHome.app.ReportManager;
import smartHome.javafx.Scene.SceneManager;
import java.util.List;

public class ReportController {

    private VBox root;
    private TextArea reportArea;
    private ReportManager reportManager;

    public ReportController() {
        reportManager = new ReportManager();
        
        root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f3f4f6;");

        // Header
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Button backBtn = new Button("← Back");
        backBtn.setOnAction(e -> SceneManager.switchScene("Dashboard"));

        Label title = new Label("Admin Reports");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        header.getChildren().addAll(backBtn, title);

        // Buttons
        HBox actions = new HBox(15);
        Button btnAllEvents = new Button("All Events");
        btnAllEvents.setOnAction(e -> showAllEvents());
        
        Button btnSummary = new Button("Summary");
        btnSummary.setOnAction(e -> showSummary());

        actions.getChildren().addAll(btnAllEvents, btnSummary);

        // Report Area
        reportArea = new TextArea();
        reportArea.setEditable(false);
        reportArea.setPrefHeight(400);
        reportArea.setStyle("-fx-font-family: 'Consolas', monospace;");

        root.getChildren().addAll(header, actions, reportArea);
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

    private void showSummary() {
        String summary = reportManager.generateSummaryReport();
        reportArea.setText("--- Summary Report ---\n" + summary);
    }
}
