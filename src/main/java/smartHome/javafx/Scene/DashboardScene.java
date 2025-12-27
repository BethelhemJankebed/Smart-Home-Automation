package smartHome.javafx.Scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import smartHome.javafx.Controllers.DashboardController;

public class DashboardScene {

    public static Scene create() {

        VBox root = new VBox(25);
        root.setAlignment(Pos.TOP_CENTER);
        root.getStyleClass().add("root-dashboard");

        // --- Header ---
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_RIGHT);

        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().add("logout-button");
        header.getChildren().add(logoutBtn);
        header.setMaxWidth(1000);

        // --- Title Section ---
        VBox titleBox = new VBox(8);
        titleBox.setAlignment(Pos.CENTER);
        Label title = new Label("Smart Home Dashboard");
        title.getStyleClass().add("dashboard-title");

        Label subtitle = new Label("Everything is running smoothly today.");
        subtitle.getStyleClass().add("dashboard-subtitle");

        titleBox.getChildren().addAll(title, subtitle);

        // --- Modules ---
        HBox modulesBox = new HBox(30);
        modulesBox.setAlignment(Pos.CENTER);
        modulesBox.setPadding(new Insets(50, 0, 0, 0));

        // Create module cards (UI only)
        VBox childModule = createModuleCard("Monitor Child", "View live status of your child", "👶", "#f472b6");
        VBox homeModule = createModuleCard("Appliance Control", "Manage your home devices", "🏠", "#34d399");
        VBox securityModule = createModuleCard("Security Center", "Monitor entry points & logs", "🛡️", "#60a5fa");
        VBox reportModule = createModuleCard("System Reports", "View activity logs & analytics", "📊", "#fbbf24");

        modulesBox.getChildren().addAll(childModule, homeModule, securityModule, reportModule);

        // --- Assemble root ---
        root.getChildren().addAll(header, titleBox, modulesBox);

        // --- Scene ---
        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(DashboardScene.class.getResource("/smartHome/javafx/Css/dashboard.css").toExternalForm());

        // --- Controller ---
        DashboardController controller = new DashboardController();
        controller.bindUI(logoutBtn, childModule, homeModule, securityModule, reportModule);

        return scene;
    }

    private static VBox createModuleCard(String title, String desc, String icon, String colorHex) {
        VBox card = new VBox(20);
        card.setPrefWidth(250);
        card.setPrefHeight(280);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("module-card");

        StackPane iconPane = new StackPane();
        Circle bg = new Circle(35, Color.web(colorHex));
        Label iconLbl = new Label(icon);
        iconLbl.getStyleClass().add("module-icon-text");
        iconPane.getChildren().addAll(bg, iconLbl);

        VBox info = new VBox(6);
        info.setAlignment(Pos.CENTER);
        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("module-title");

        Label descLbl = new Label(desc);
        descLbl.getStyleClass().add("module-desc");
        descLbl.setWrapText(true);

        info.getChildren().addAll(titleLbl, descLbl);

        card.getChildren().addAll(iconPane, info);

        return card;
    }
}
