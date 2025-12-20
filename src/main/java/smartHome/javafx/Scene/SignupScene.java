package smartHome.javafx.Scene;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import smartHome.db.DatabaseManager;

public class SignupScene {

    public static Scene create() {

        // Icon
        Circle circle = new Circle(28, Color.web("#2563eb"));
        Label lock = new Label("🔒");
        lock.setStyle("-fx-font-size: 40px; -fx-text-fill: white;");
        StackPane icon = new StackPane(circle, lock);

        Label title = new Label("Create Account");
        title.getStyleClass().add("title");
        Label subtitle = new Label("Create your account to access the control panel");
        subtitle.getStyleClass().add("subtitle");

        // Username row (inline label + emoji + field)
        TextField username = new TextField();
        username.setPromptText("");
        username.setPrefWidth(280);
        Label userInline = new Label("Username");
        userInline.getStyleClass().add("inline-label");
        Label userIcon = new Label("👤");
        HBox userRow = new HBox(8, userInline, userIcon, username);
        userRow.getStyleClass().add("input-row");
        userRow.setMaxWidth(320);
        userRow.setAlignment(Pos.CENTER_LEFT);

        // Password row
        PasswordField password = new PasswordField();
        password.setPromptText("");
        password.setPrefWidth(280);
        Label passInline = new Label("Password");
        passInline.getStyleClass().add("inline-label");
        Label passIcon = new Label("🔑");
        HBox passRow = new HBox(8, passInline, passIcon, password);
        passRow.getStyleClass().add("input-row");
        passRow.setMaxWidth(320);
        passRow.setAlignment(Pos.CENTER_LEFT);

        Label message = new Label();
        message.getStyleClass().add("message");

        Button signup = new Button("Sign Up");
        signup.getStyleClass().add("primary-btn");
        signup.setPrefWidth(320);

        signup.setOnAction(e -> {
            boolean ok = DatabaseManager.registerUser(
                    username.getText(),
                    password.getText()
            );

            if (ok) {
                message.setText("");
                SceneManager.switchScene("Login");
            } else {
                String err = DatabaseManager.getLastError();
                if (err != null && err.toLowerCase().contains("unique")) {
                    message.setText("Username already exists");
                } else if (err != null && err.equals("All fields are required")) {
                    message.setText(err);
                } else {
                    message.setText("Database error: " + (err == null ? "unknown" : err));
                }
            }
        });

        // Back to login
        Label haveAccount = new Label("Already have an account?");
        Hyperlink back = new Hyperlink("Login");
        back.getStyleClass().add("link");
        HBox backRow = new HBox(6, haveAccount, back);
        backRow.getStyleClass().add("signup-row");
        backRow.setAlignment(Pos.CENTER);
        back.setOnAction(e -> SceneManager.switchScene("Login"));

        VBox card = new VBox(14,
                icon,
                title,
                subtitle,
                userRow,
                passRow,
                signup,
                backRow,
                message
        );
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("card");
        card.setMaxWidth(420);

        StackPane root = new StackPane(card);
        root.getStyleClass().add("root");

        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(
                SignupScene.class.getResource("/css/login.css").toExternalForm()
        );

        return scene;
    }
}
