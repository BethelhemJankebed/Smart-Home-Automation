package smartHome.javafx.Scene;

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import smartHome.javafx.Controllers.ReportController;

public class ReportScene {
    public static Scene create() {
        ReportController controller = new ReportController();
        VBox root = controller.getView();
        return new Scene(root, 800, 600);
    }
}
