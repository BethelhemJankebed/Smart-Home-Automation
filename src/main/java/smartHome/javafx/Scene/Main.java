package smartHome.javafx.Scene;

import javafx.application.Application;
import javafx.stage.Stage;
import smartHome.javafx.Scene.SceneManager;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        SceneManager.init(stage);
        SceneManager.switchScene("Login");
    }


    public static void main(String[] args) {
        launch(args);
    }
}
