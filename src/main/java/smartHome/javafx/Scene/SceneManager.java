package smartHome.app.utils;

import javafx.scene.Scene;
import javafx.stage.Stage;
import smartHome.app.scenes.LoginScene;
import smartHome.app.scenes.SignupScene;

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
            default -> throw new IllegalArgumentException("Unknown scene");
        };

        stage.setScene(scene);
        stage.show();
    }
}
