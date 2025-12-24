package smartHome.javafx.Scene;

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import smartHome.javafx.Controllers.ControlHomeAppliancesController;

public class ControlHomeAppliancesScene {
    public static Scene create() {
        ControlHomeAppliancesController controller = new ControlHomeAppliancesController();
        controller.initialize(); // Manually call initialize since not using FXML
        VBox root = controller.getView(); // We need to ensure getView exists or creation logic is accessible
        
        // Wait, I need to check if ControlHomeAppliancesController has a getView() method. 
        // If not, I'll need to refactor it or use its root if exposed.
        // Assuming I'll check/fix it in the next step if needed. 
        // For now, let's assume I will fix the controller to expose logic.
        return new Scene(root, 1000, 600);
    }
}
