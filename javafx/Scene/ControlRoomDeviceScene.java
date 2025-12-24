package smartHome.javafx.Scene;

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import smartHome.javafx.Controllers.ControlRoomDeviceController;

public class ControlRoomDeviceScene {
    public static Scene create() {
        ControlRoomDeviceController controller = new ControlRoomDeviceController();
        VBox root = controller.getView();
        
        Scene scene = new Scene(root, 800, 600);
        return scene;
    }
}
