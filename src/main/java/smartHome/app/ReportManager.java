package smartHome.app;

import smartHome.db.DatabaseManager;
import java.util.List;

public class ReportManager {

    public List<String> generateAllEventsReport() {
        return DatabaseManager.getEvents(null);
    }
    
    public String generateSummaryReport() {
        List<String> events = DatabaseManager.getEvents(null);
        int motionCount = 0;
        for(String e : events) {
            if (e.contains("MOTION")) motionCount++;
        }
        return "Total Events: " + events.size() + "\nMotion Events: " + motionCount;
    }
}
