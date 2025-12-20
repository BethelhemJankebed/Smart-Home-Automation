package smartHome.javafx.Scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class AppliancesScene {
    public static Scene create() {
        Label title = new Label("Control Home Appliances");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button back = new Button("Back");
        back.setOnAction(e -> SceneManager.switchScene("Dashboard"));

        BorderPane header = new BorderPane();
        header.setLeft(title);
        header.setRight(back);

        FlowPane placeholder = new FlowPane();
        placeholder.setHgap(12);
        placeholder.setVgap(12);

        VBox root = new VBox(16, header, placeholder);
        root.setPadding(new Insets(18));
        root.setAlignment(Pos.TOP_LEFT);

        Scene scene = new Scene(root, 1100, 650);
        scene.getStylesheets().add(AppliancesScene.class.getResource("/css/dashboard.css").toExternalForm());
        return scene;
    }
}
