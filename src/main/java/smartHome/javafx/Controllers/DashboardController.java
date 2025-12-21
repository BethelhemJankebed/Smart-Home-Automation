package smartHome.javafx.Controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import smartHome.javafx.Scene.SceneManager;

public class DashboardController {

    private VBox root;

    public DashboardController() {
        root = new VBox(25);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #f8fafc, #f1f5f9);");

        // Header
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_RIGHT);
        
        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: white; -fx-text-fill: #64748b; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2); -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> SceneManager.switchScene("Login"));
        
        header.getChildren().add(logoutBtn);
        header.setMaxWidth(1000);

        // Title Section
        VBox titleBox = new VBox(8);
        titleBox.setAlignment(Pos.CENTER);
        Label title = new Label("Smart Home Dashboard");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: 900; -fx-text-fill: #1e293b;");
        Label subtitle = new Label("Everything is running smoothly today.");
        subtitle.setStyle("-fx-font-size: 15px; -fx-text-fill: #64748b;");
        titleBox.getChildren().addAll(title, subtitle);

        // Modules Container
        HBox modulesBox = new HBox(30);
        modulesBox.setAlignment(Pos.CENTER);
        modulesBox.setPadding(new Insets(50, 0, 0, 0));

        // 1. Monitor Child Module
        VBox childModule = createModuleCard("Monitor Child", "View live status of your child", "👶", "#f472b6", "MonitorChild");
        
        // 2. Control Appliances Module
        VBox homeModule = createModuleCard("Appliance Control", "Manage your home devices", "🏠", "#34d399", "AppliancesModule");
        
        // 3. Security Module
        VBox securityModule = createModuleCard("Security Center", "Monitor entry points & logs", "🛡️", "#60a5fa", "SecurityModule");
        
        // 4. Reports Module
        VBox reportModule = createModuleCard("System Reports", "View activity logs & analytics", "📊", "#fbbf24", "Reports");

        modulesBox.getChildren().addAll(childModule, homeModule, securityModule, reportModule);

        root.getChildren().addAll(header, titleBox, modulesBox);
    }

    public VBox getView() {
        return root;
    }

    private VBox createModuleCard(String title, String desc, String icon, String colorHex, String targetScene) {
        VBox card = new VBox(20);
        card.setPrefWidth(250);
        card.setPrefHeight(280);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 24; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 20, 0, 0, 10); -fx-cursor: hand;");

        StackPane iconPane = new StackPane();
        Circle bg = new Circle(35, Color.web(colorHex));
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 30px;");
        iconPane.getChildren().addAll(bg, iconLbl);

        VBox info = new VBox(6);
        info.setAlignment(Pos.CENTER);
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: #1e293b;");
        
        Label descLbl = new Label(desc);
        descLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-text-alignment: center;");
        descLbl.setWrapText(true);
        info.getChildren().addAll(titleLbl, descLbl);

        card.getChildren().addAll(iconPane, info);

        card.setOnMouseClicked(e -> SceneManager.switchScene(targetScene));

        // Hover Effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 24; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 30, 0, 0, 15); -fx-cursor: hand; -fx-translate-y: -5;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 24; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 20, 0, 0, 10); -fx-cursor: hand; -fx-translate-y: 0;"));

        return card;
    }
}
