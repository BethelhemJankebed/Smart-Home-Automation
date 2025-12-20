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

        Scene scene = switch (name) {
            case "Login" -> LoginScene.create();
            case "Signup" -> SignupScene.create();
            case "Dashboard" -> DashboardScene.create();
            case "DashboardScene" -> DashboardScene.create();
            default -> throw new IllegalArgumentException("Unknown scene");
        };

        stage.setScene(scene);
        stage.show();
    }
}
