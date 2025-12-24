package smartHome.javafx.Scene;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import smartHome.db.DatabaseManager;

public class SignupScene {

    public static Scene create() {

        // --- Card Container ---
        VBox card = new VBox(15);
        card.getStyleClass().add("signup-card");
        card.setAlignment(Pos.CENTER);

        // --- Icon ---
        Circle circle = new Circle(30);
        circle.getStyleClass().add("lock-icon-bg");
        Label lock = new Label("🔒"); // Using emoji as requested/implied by previous code, or could use font icon
        lock.getStyleClass().add("lock-icon-text");
        StackPane icon = new StackPane(circle, lock);
        icon.getStyleClass().add("icon-container");

        // --- Inputs ---
        TextField username = new TextField();
        username.setPromptText("Username");
        username.getStyleClass().add("input-field");

        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        password.getStyleClass().add("input-field");

        Label error = new Label();
        error.getStyleClass().add("error-label");

        // --- Buttons ---
        Button signup = new Button("Sign Up");
        signup.getStyleClass().add("primary-button");

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
        back.getStyleClass().add("link-button");
        back.setOnAction(e -> SceneManager.switchScene("Login"));

        // --- Assemble Card ---
        card.getChildren().addAll(
                icon,
                username,
                password,
                error,
                signup,
                back
        );

        // --- Root ---
        StackPane root = new StackPane(card);
        root.getStyleClass().add("root");

        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(
               SignupScene.class.getResource("/smartHome/javafx/Css/signup.css").toExternalForm()
        );
        
        return scene;
    }
}
