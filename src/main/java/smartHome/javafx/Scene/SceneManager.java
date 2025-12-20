package smartHome.javafx.Scene;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {

    private static Stage stage;

    public static void init(Stage s) {
        stage = s;
        stage.setTitle("Smart Home");
    }

    public static void switchScene(String name) {

        double w = stage.getWidth();
        double h = stage.getHeight();

        Scene scene = switch (name) {
            case "Login" -> LoginScene.create();
            case "Signup" -> SignupScene.create();
            case "Dashboard" -> DashboardScene.create();
            case "DashboardScene" -> DashboardScene.create();
            case "ChildMonitor" -> ChildMonitorScene.create();
            case "Appliances" -> AppliancesScene.create();
            case "Security" -> SecurityScene.create();
            default -> throw new IllegalArgumentException("Unknown scene");
        };

        stage.setScene(scene);
        // Keep previous stage size across scenes
        if (w > 0 && h > 0) {
            stage.setWidth(w);
            stage.setHeight(h);
        }

        if (!stage.isShowing()) {
            stage.show();
        }

    }
}
