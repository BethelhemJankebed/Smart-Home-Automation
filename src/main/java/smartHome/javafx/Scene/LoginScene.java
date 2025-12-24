package smartHome.javafx.Scene;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import smartHome.javafx.Controllers.LoginController;

public class LoginScene {

    public static Scene create() {

        // --- Card Container ---
        VBox card = new VBox(15);
        card.getStyleClass().add("login-card");
        card.setAlignment(Pos.CENTER);

        // --- Icon ---
        Circle circle = new Circle(30);
        circle.getStyleClass().add("lock-icon-bg");
        Label lock = new Label("🔒");
        lock.getStyleClass().add("lock-icon-text");
        StackPane icon = new StackPane(circle, lock);
        icon.getStyleClass().add("icon-container");

        // --- Title ---
        Label title = new Label("Smart Home System");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Enter your credentials to access the control panel");
        subtitle.getStyleClass().add("subtitle");
        subtitle.setWrapText(true);

        // --- Inputs ---
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.getStyleClass().add("input-field");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("input-field");

        Label messageLabel = new Label();
        messageLabel.getStyleClass().add("error-label");

        // --- Buttons ---
        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("primary-button");

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

        HBox footer = new HBox(5);
        footer.setAlignment(Pos.CENTER);
        Label noAccount = new Label("Don't have an account?");
        noAccount.getStyleClass().add("link-label");
        
        Button signupButton = new Button("Sign up");
        signupButton.getStyleClass().add("link-button");
        // Lambda expression
        signupButton.setOnAction(e -> SceneManager.switchScene("Signup"));

        footer.getChildren().addAll(noAccount, signupButton);

        // --- Assemble Card ---
        card.getChildren().addAll(
                icon,
                title,
                subtitle,
                usernameField,
                passwordField,
                messageLabel, // Error message above button usually better, or below
                loginButton,
                footer
        );

        // --- Root ---
        VBox root = new VBox(card);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("root");

        Scene scene = new Scene(root, 1000, 600); // Larger default size for desktop feel
        scene.getStylesheets().add(
                LoginScene.class.getResource("/smartHome/javafx/Css/login.css").toExternalForm()
        );

        return scene;
    }
}
