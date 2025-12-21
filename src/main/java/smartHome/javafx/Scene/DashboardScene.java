package smartHome.javafx.Scene;

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import smartHome.javafx.Controllers.DashboardController;

public class DashboardScene {
    public static Scene create() {
        DashboardController controller = new DashboardController();
        VBox root = controller.getView();
        return new Scene(root, 900, 600);
    }
}
