package smartHome.app;

import javafx.application.Application;
import javafx.stage.Stage;
import smartHome.app.utils.SceneManager;

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
