package smartHome.javafx.Scene;

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import smartHome.javafx.Controllers.ChildMonitorController;

public class MonitorChildScene {
    public static Scene create() {
        ChildMonitorController controller = new ChildMonitorController();
        VBox root = controller.getView();
        return new Scene(root, 900, 600);
    }
}
