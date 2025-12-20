package smartHome.javafx.Scene;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import smartHome.db.DatabaseManager;
import smartHome.javafx.Scene.*;;;

public class LoginScene {

    public static Scene create() {

        Label title = new Label("Smart Home System");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label subtitle = new Label("Sign in to continue");
        subtitle.setStyle("-fx-text-fill: #6b7280;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.web("#dc2626"));

        Button loginBtn = new Button("Login");
        loginBtn.setPrefWidth(260);
        loginBtn.setStyle("""
            -fx-background-color: black;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 8;
        """);

        Button signupBtn = new Button("Don't have an account? Sign up");
        signupBtn.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #2563eb;
        """);

        loginBtn.setOnAction(e -> {
            if (DatabaseManager.validateLogin(
                    usernameField.getText(),
                    passwordField.getText())) {

                SceneManager.switchScene("Dashboard");
            } else {
                errorLabel.setText("Invalid username or password");
            }
        });

        signupBtn.setOnAction(e ->
                SceneManager.switchScene("Signup")
        );

        Circle circle = new Circle(30, Color.web("#2563eb"));
        Label lock = new Label("🔒");
        lock.setStyle("-fx-font-size: 44px; -fx-text-fill: white;");
        StackPane icon = new StackPane(circle, lock);

        VBox card = new VBox(14,
                icon,
                title,
                subtitle,
                usernameField,
                passwordField,
                errorLabel,
                loginBtn,
                signupBtn
        );

        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(320);
        card.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 16;
            -fx-padding: 30;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0, 0, 6);
        """);

        StackPane root = new StackPane(card);
        root.setPrefSize(1000, 600);

        return new Scene(root, 1000, 600);
    }
}
