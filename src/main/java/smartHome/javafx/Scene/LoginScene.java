package smartHome.javafx.Scene;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import smartHome.javafx.Controllers.LoginController;

public class LoginScene {

    public static Scene createScene() {

        // Icon (blue circle with lock)
        Circle circle = new Circle(28, Color.web("#2563eb"));
        Label lock = new Label("🔒");
        lock.setStyle("-fx-font-size: 40px; -fx-text-fill: white;");
        StackPane icon = new StackPane(circle, lock);

        Label title = new Label("Smart Home System");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Enter your credentials to access the control panel");
        subtitle.getStyleClass().add("subtitle");

        TextField usernameField = new TextField();
        usernameField.setPromptText("");
        usernameField.setPrefWidth(280);
        Label userIcon = new Label("👤");
        Label userInline = new Label("Username");
        userInline.getStyleClass().add("inline-label");
        HBox userRow = new HBox(8, userInline, userIcon, usernameField);
        userRow.getStyleClass().add("input-row");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("");
        passwordField.setPrefWidth(280);
        Label passIcon = new Label("🔑");
        Label passInline = new Label("Password");
        passInline.getStyleClass().add("inline-label");
        HBox passRow = new HBox(8, passInline, passIcon, passwordField);
        passRow.getStyleClass().add("input-row");
    userRow.setMaxWidth(320);
    userRow.setAlignment(Pos.CENTER_LEFT);

        Label messageLabel = new Label();
        messageLabel.getStyleClass().add("message");

            passRow.setMaxWidth(320);
            passRow.setAlignment(Pos.CENTER_LEFT);
        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("primary-btn");
            loginButton.setPrefWidth(320);

            // Signup link
            Label noAccount = new Label("Don't have an account?");
            Hyperlink signupLink = new Hyperlink("Sign up");
            signupLink.getStyleClass().add("link");
            HBox signupRow = new HBox(6, noAccount, signupLink);
            signupRow.getStyleClass().add("signup-row");
            signupRow.setAlignment(Pos.CENTER);

        // Controller object
        LoginController controller = new LoginController();

        // Anonymous class + override (teacher requirement)
        loginButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                controller.handleLogin(
                        usernameField.getText(),
                        passwordField.getText(),
                        messageLabel
                );
            }
        });

            signupLink.setOnAction(e -> SceneManager.switchScene("Signup"));
        VBox card = new VBox(14,
                icon,
                title,
                subtitle,
                userRow,
                passRow,
                loginButton,
                signupRow,
                messageLabel
        );
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("card");
            card.setMaxWidth(420);

        StackPane root = new StackPane(card);
        root.getStyleClass().add("root");

        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(
                LoginScene.class.getResource("/css/login.css").toExternalForm()
        );

        return scene;
    }

    // Compatibility alias expected by SceneManager
    public static Scene create() {
        return createScene();
    }
}
