package smartHome.javafx.Scene;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {

    private static Stage stage;
    private static Object sceneData;

    public static Object getData() {
        return sceneData;
    }

    public static void init(Stage s) {
        stage = s;
        stage.setTitle("Smart Home");
    }

    public static void switchScene(String name) {
        switchScene(name, null);
    }

    public static void switchScene(String name, Object data) {
        sceneData = data;

        Scene scene = switch (name) {
            case "Login" -> LoginScene.create();
            case "Signup" -> SignupScene.create();
            case "ControlRoomDevice" -> ControlRoomDeviceScene.create();
            case "Reports" -> ReportScene.create();
            case "Dashboard" -> DashboardScene.create();
            case "AppliancesModule" -> ControlHomeAppliancesScene.create();
            case "MonitorChild" -> MonitorChildScene.create();
            case "SecurityModule" -> SecurityMonitorScene.create();
            default -> throw new IllegalArgumentException("Unknown scene: " + name);
        };

        stage.setScene(scene);
        stage.show();
    }
}
