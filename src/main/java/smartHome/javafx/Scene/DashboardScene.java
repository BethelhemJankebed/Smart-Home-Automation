package smartHome.javafx.Scene;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class DashboardScene {

    public static Scene create() {
        Label title = new Label("Smart Home Dashboard");
        Button logout = new Button("Logout");
        logout.setOnAction(e -> SceneManager.switchScene("Login"));

        VBox root = new VBox(12, title, logout);
        root.setAlignment(Pos.CENTER);
        return new Scene(root, 800, 600);
    }
}
