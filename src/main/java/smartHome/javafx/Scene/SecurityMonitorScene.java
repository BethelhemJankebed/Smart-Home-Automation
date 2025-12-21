package smartHome.javafx.Scene;

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import smartHome.javafx.Controllers.SecurityMonitorController;

public class SecurityMonitorScene {
    public static Scene create() {
        SecurityMonitorController controller = new SecurityMonitorController();
        VBox root = controller.getView();
        return new Scene(root, 900, 600);
    }
}
