package smartHome.javafx.Scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;

public class DashboardScene {

    private static VBox card(Node iconNode, String title, String subtitle, Runnable onClick) {
        VBox iconBox = new VBox(iconNode);
        iconBox.setAlignment(Pos.CENTER);

        Label t = new Label(title);
        t.getStyleClass().add("card-title");
        t.setWrapText(true);
        t.setMaxWidth(220);
        t.setAlignment(Pos.CENTER);

        Label s = new Label(subtitle);
        s.getStyleClass().add("card-subtitle");
        s.setWrapText(true);
        s.setMaxWidth(220);
        s.setAlignment(Pos.CENTER);

        VBox card = new VBox(iconBox, t, s);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(260);
        card.setOnMouseClicked(e -> onClick.run());
        card.setCursor(Cursor.HAND);
        return card;
    }

    public static Scene create() {

        // Header
        Label title = new Label("Smart Home Control Panel");
        title.getStyleClass().add("card-title");

        Label welcome = new Label("Welcome back! Select a module to continue.");
        welcome.getStyleClass().add("card-subtitle");

        VBox headerLeft = new VBox(4, title, welcome);

        Button logout = new Button("Logout");
        logout.setOnAction(e -> SceneManager.switchScene("Login"));

        BorderPane header = new BorderPane();
        header.setLeft(headerLeft);
        header.setRight(logout);
        BorderPane.setMargin(logout, new Insets(0, 8, 0, 0));

        // Cards
        // Child: larger pink circle + baby face emoji
        Circle childCircle = new Circle(44);
        childCircle.getStyleClass().add("icon-circle-pink");
        Label childEmoji = new Label("👶");
        childEmoji.getStyleClass().add("icon-emoji");
        StackPane childIcon = new StackPane(childCircle, childEmoji);
        childIcon.getStyleClass().add("card-icon");

        // Appliances: larger green circle + home emoji
        Circle appCircle = new Circle(44);
        appCircle.getStyleClass().add("icon-circle-green");
        Label homeEmoji = new Label("🏠");
        homeEmoji.getStyleClass().add("icon-emoji");
        StackPane appIcon = new StackPane(appCircle, homeEmoji);
        appIcon.getStyleClass().add("card-icon");

        // Security: larger blue circle + SVG shield
        Circle secCircle = new Circle(44);
        secCircle.getStyleClass().add("icon-circle-blue");
        SVGPath shield = new SVGPath();
        shield.setContent("M12 2 L20 6 V12 C20 17.25 16.48 22.5 12 24 C7.52 22.5 4 17.25 4 12 V6 L12 2 Z");
        shield.getStyleClass().add("icon-shield");
        // Scale the shield to fit the circle
        shield.setScaleX(1.2);
        shield.setScaleY(1.2);
        StackPane secIcon = new StackPane(secCircle, shield);
        secIcon.getStyleClass().add("card-icon");

        VBox childCard = card(childIcon, "Monitor Child", "View live status and location of your child in the home",
            () -> SceneManager.switchScene("ChildMonitor"));
        VBox appliancesCard = card(appIcon, "Control Home Appliances", "Manage lights, fans, and other devices in each room",
            () -> SceneManager.switchScene("Appliances"));
        VBox securityCard = card(secIcon, "Security", "Monitor doors, windows, and room security status",
            () -> SceneManager.switchScene("Security"));

        HBox cards = new HBox(18, childCard, appliancesCard, securityCard);
        cards.setAlignment(Pos.CENTER);

        StackPane centerPane = new StackPane(cards);
        centerPane.setAlignment(Pos.CENTER);

        VBox container = new VBox(18, header, centerPane);
        VBox.setVgrow(centerPane, Priority.ALWAYS);
        container.getStyleClass().add("dashboard-root");
        header.getStyleClass().add("dashboard-header");

        Scene scene = new Scene(container, 1100, 650);
        scene.getStylesheets().add(DashboardScene.class.getResource("/css/dashboard.css").toExternalForm());
        return scene;
    }
}
