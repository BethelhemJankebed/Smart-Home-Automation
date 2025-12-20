package smartHome.javafx.Scene;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import smartHome.db.DatabaseManager;
import smartHome.javafx.*;

public class SignupScene {

    public static Scene create() {

        TextField username = new TextField();
        username.setPromptText("Username");

        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        Label error = new Label();
        error.setTextFill(Color.RED);

        Button signup = new Button("Sign Up");
        signup.setPrefWidth(260);

        signup.setOnAction(e -> {
            if (DatabaseManager.registerUser(
                    username.getText(),
                    password.getText())) {

                SceneManager.switchScene("Login");
            } else {
                error.setText("Username already exists");
            }
        });

        Button back = new Button("Already have an account? Login");
        back.setOnAction(e -> SceneManager.switchScene("Login"));

        Circle circle = new Circle(30, Color.web("#2563eb"));
        Label lock = new Label("🔒");
        lock.setStyle("-fx-font-size: 44px; -fx-text-fill: white;");
        StackPane icon = new StackPane(circle, lock);

        VBox box = new VBox(14,
                icon,
                username,
                password,
                error,
                signup,
                back
        );

        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(320);
        box.setStyle("-fx-background-color: white; -fx-padding: 30;");

        StackPane root = new StackPane(box);
        return new Scene(root, 1000, 600);
    }
}
