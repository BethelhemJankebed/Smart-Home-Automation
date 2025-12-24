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
        // Padding moved to CSS or kept here for layout logic, kept in CSS for consistency if possible, but padding is often structural. 
        // CSS padding does distinct things for Regions. Let's rely on CSS as much as possible.
        // root.setPadding(new Insets(40)); 
        root.getStyleClass().add("root-dashboard");

        // Header
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_RIGHT);
        
        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().add("logout-button");
        logoutBtn.setOnAction(e -> SceneManager.switchScene("Login"));
        
        header.getChildren().add(logoutBtn);
        header.setMaxWidth(1000);

        // Title Section
        VBox titleBox = new VBox(8);
        titleBox.setAlignment(Pos.CENTER);
        Label title = new Label("Smart Home Dashboard");
        title.getStyleClass().add("dashboard-title");
        
        Label subtitle = new Label("Everything is running smoothly today.");
        subtitle.getStyleClass().add("dashboard-subtitle");
        
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
        
        // Load CSS
        root.getStylesheets().add(getClass().getResource("/smartHome/javafx/Css/dashboard.css").toExternalForm());
    }

    public VBox getView() {
        return root;
    }

    private VBox createModuleCard(String title, String desc, String icon, String colorHex, String targetScene) {
        VBox card = new VBox(20);
        card.setPrefWidth(250);
        card.setPrefHeight(280);
        card.setAlignment(Pos.CENTER);
        // Padding for card content
        // card.setPadding(new Insets(30)); // Moved to CSS
        
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

        card.setOnMouseClicked(e -> SceneManager.switchScene(targetScene));

        // Hover Effect - Handled by CSS :hover pseudo-class 
        // Note: Translation effect in CSS might not work on VBox without JavaFX 8u40+ or specific CSS tweaks.
        // Standard JavaFX CSS :hover supports scale/translate but let's keep it simple.
        // If simple CSS :hover doesn't work for layout changes (translate), we can keep the listeners but remove the inline style string construction.
        // However, CSS is cleaner. "translate-y" is standard in newer JavaFX. 
        // If it fails, we fall back to listeners toggling a class.
        // For now, relying on CSS.

        return card;
    }
}
