package smartHome.javafx.Scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class ChildMonitorScene {
    public static Scene create() {
        Label title = new Label("Monitor Child");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button back = new Button("Back");
        back.setOnAction(e -> SceneManager.switchScene("Dashboard"));

        BorderPane header = new BorderPane();
        header.setLeft(title);
        header.setRight(back);

        Label placeholder = new Label("Child monitoring UI coming soon");

        VBox root = new VBox(16, header, placeholder);
        root.setPadding(new Insets(18));
        root.setAlignment(Pos.TOP_LEFT);

        Scene scene = new Scene(root, 1100, 650);
        scene.getStylesheets().add(ChildMonitorScene.class.getResource("/css/dashboard.css").toExternalForm());
        return scene;
    }
}
