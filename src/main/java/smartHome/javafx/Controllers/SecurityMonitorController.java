package smartHome.javafx.Controllers;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import smartHome.db.DatabaseManager;
import smartHome.javafx.Scene.SceneManager;

public class SecurityMonitorController {

    private VBox root;
    private Label doorStatusLbl, doorTimeLbl;
    private Label shopStatusLbl, shopTimeLbl;
    private Label roomStatusLbl, roomTimeLbl;
    private AnimationTimer timer;

    public SecurityMonitorController() {
        root = new VBox(30);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #1e293b, #0f172a);");

        // Header
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Button backBtn = new Button("←");
        backBtn.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-font-size: 18px; -fx-background-radius: 12; -fx-cursor: hand;");
        backBtn.setOnAction(e -> {
            stopMonitoring();
            SceneManager.switchScene("Dashboard");
        });

        VBox titleBox = new VBox(2);
        Label mainTitle = new Label("Security Command Center");
        mainTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: 900; -fx-text-fill: white;");
        Label subTitle = new Label("Real-time monitoring and system status");
        subTitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px;");
        titleBox.getChildren().addAll(mainTitle, subTitle);
        
        header.getChildren().addAll(backBtn, titleBox);

        // Status grid
        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(30);
        grid.setAlignment(Pos.CENTER);

        doorStatusLbl = new Label("Loading...");
        doorTimeLbl = new Label("--:--");
        VBox doorCard = createStatusCard("Main Entry", "🔐", doorStatusLbl, doorTimeLbl, "#3b82f6");

        shopStatusLbl = new Label("Loading...");
        shopTimeLbl = new Label("--:--");
        VBox shopCard = createStatusCard("Garage & Shop", "🏢", shopStatusLbl, shopTimeLbl, "#8b5cf6");

        roomStatusLbl = new Label("Loading...");
        roomTimeLbl = new Label("--:--");
        VBox roomCard = createStatusCard("Internal Network", "🛡️", roomStatusLbl, roomTimeLbl, "#10b981");

        grid.add(doorCard, 0, 0);
        grid.add(shopCard, 1, 0);
        grid.add(roomCard, 2, 0);

        root.getChildren().addAll(header, grid);
        startMonitoring();
    }

    public VBox getView() {
        return root;
    }

    private VBox createStatusCard(String name, String icon, Label statusLbl, Label timeLbl, String accentColor) {
        VBox card = new VBox(15);
        card.setPrefSize(280, 200);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: rgba(30, 41, 59, 0.7); -fx-background-radius: 25; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 25; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 30, 0, 0, 15);");

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 40px;");

        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: white;");

        statusLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: " + accentColor + ";");
        
        timeLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        card.getChildren().addAll(iconLbl, nameLbl, statusLbl, timeLbl);
        return card;
    }

    private void startMonitoring() {
        timer = new AnimationTimer() {
            private long lastUpdate = 0;
            @Override
            public void handle(long now) {
                if (now - lastUpdate > 1_000_000_000L) {
                    updateStatus();
                    lastUpdate = now;
                }
            }
        };
        timer.start();
    }

    private void stopMonitoring() {
        if (timer != null) timer.stop();
    }

    private void updateStatus() {
        String doorData = DatabaseManager.getOverallStatus("DoorLock");
        String shopData = DatabaseManager.getOverallStatus("Shop");
        String roomSummary = DatabaseManager.getSecuritySummary();

        Platform.runLater(() -> {
            updateCard(doorStatusLbl, doorTimeLbl, doorData);
            updateCard(shopStatusLbl, shopTimeLbl, shopData);
            updateCard(roomStatusLbl, roomTimeLbl, roomSummary);
        });
    }

    private void updateCard(Label statusLbl, Label timeLbl, String status) {
        statusLbl.setText(status.toUpperCase());
        timeLbl.setText("LAST UPDATED: " + java.time.LocalTime.now().toString().substring(0, 5));
        
        if (status.equalsIgnoreCase("Secure") || status.equalsIgnoreCase("Locked")) {
            statusLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: #10b981;");
        } else {
            statusLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: #ef4444;");
        }
    }
}
