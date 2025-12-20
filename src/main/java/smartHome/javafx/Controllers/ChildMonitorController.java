package smartHome.javafx.Controllers;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class ChildMonitorController {

  private final IntegerProperty notificationCount = new SimpleIntegerProperty(4);

  public ReadOnlyIntegerProperty notificationCountProperty() {
    return notificationCount;
  }

  public void decrementNotifications() {
    int v = Math.max(0, notificationCount.get() - 1);
    notificationCount.set(v);
  }
}