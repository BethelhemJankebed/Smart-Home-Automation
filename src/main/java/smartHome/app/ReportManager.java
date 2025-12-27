package smartHome.app;

import java.util.List;

public class ReportManager {

    public List<String> getSystemLogs(String period) {
        if ("Weekly".equalsIgnoreCase(period)) return getWeeklyLogs();
        List<String> logs = new java.util.ArrayList<>();
        String sql = "SELECT timestamp, module, type, details FROM events " +
                     "WHERE timestamp LIKE ? AND module IS NOT NULL " +
                     "AND (details IS NULL OR details NOT LIKE '%MOTION%') " +
                     "ORDER BY id DESC";
        
        try (java.sql.Connection conn = smartHome.db.DatabaseManager.connect();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, getPatternForPeriod(period));
            java.sql.ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String fullTs = rs.getString("timestamp");
                String time = (fullTs != null && fullTs.length() >= 16) ? fullTs.substring(11, 16) : "??:??";
                String type = rs.getString("type");
                String details = rs.getString("details");
                
                String eventText = (type != null && !type.isEmpty()) ? type : "";
                if (details != null && !details.isEmpty()) {
                    eventText = eventText.isEmpty() ? details : eventText + " | " + details;
                }
                
                logs.add(String.format("%s | %s | %s", time, rs.getString("module"), eventText));
            }
        } catch (java.sql.SQLException e) { e.printStackTrace(); }
        return logs;
    }

    public java.util.Map<String, Integer> getModuleActivityDistribution(String period) {
        if ("Weekly".equalsIgnoreCase(period)) return getWeeklyDistribution();
        java.util.Map<String, Integer> dist = new java.util.HashMap<>();
        String sql = "SELECT module, COUNT(*) as c FROM events " +
                     "WHERE timestamp LIKE ? AND module IS NOT NULL " +
                     "AND (details IS NULL OR details NOT LIKE '%MOTION%') " +
                     "GROUP BY module";
        
        try (java.sql.Connection conn = smartHome.db.DatabaseManager.connect();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, getPatternForPeriod(period));
            java.sql.ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                dist.put(rs.getString("module"), rs.getInt("c"));
            }
        } catch (java.sql.SQLException e) { e.printStackTrace(); }
        return dist;
    }

    private String getPatternForPeriod(String period) {
        java.time.LocalDate now = java.time.LocalDate.now();
        return switch (period.toLowerCase()) {
            case "daily" -> now.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "%";
            case "monthly" -> now.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")) + "%";
            case "yearly" -> now.format(java.time.format.DateTimeFormatter.ofPattern("yyyy")) + "%";
            default -> now.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "%"; // Default to daily
        };
    }

    // Special handling for weekly because it's a range, not a simple prefix
    private List<String> getWeeklyLogs() {
        List<String> logs = new java.util.ArrayList<>();
        String sql = "SELECT timestamp, module, type, details FROM events " +
                     "WHERE module IS NOT NULL AND (details IS NULL OR details NOT LIKE '%MOTION%') " +
                     "ORDER BY id DESC";
        
        java.time.LocalDateTime weekAgo = java.time.LocalDateTime.now().minusDays(7);
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (java.sql.Connection conn = smartHome.db.DatabaseManager.connect();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            java.sql.ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String fullTs = rs.getString("timestamp");
                if (fullTs == null || fullTs.length() < 19) continue;
                
                java.time.LocalDateTime eventTime = java.time.LocalDateTime.parse(fullTs, formatter);
                if (eventTime.isAfter(weekAgo)) {
                    String time = fullTs.substring(11, 16);
                    String type = rs.getString("type");
                    String details = rs.getString("details");
                    String eventText = (type != null && !type.isEmpty()) ? type : "";
                    if (details != null && !details.isEmpty()) {
                        eventText = eventText.isEmpty() ? details : eventText + " | " + details;
                    }
                    logs.add(String.format("%s | %s | %s", time, rs.getString("module"), eventText));
                }
            }
        } catch (java.sql.SQLException e) { e.printStackTrace(); }
        return logs;
    }

    private java.util.Map<String, Integer> getWeeklyDistribution() {
        java.util.Map<String, Integer> dist = new java.util.HashMap<>();
        String sql = "SELECT timestamp, module FROM events " +
                     "WHERE module IS NOT NULL AND (details IS NULL OR details NOT LIKE '%MOTION%')";
        
        java.time.LocalDateTime weekAgo = java.time.LocalDateTime.now().minusDays(7);
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (java.sql.Connection conn = smartHome.db.DatabaseManager.connect();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            java.sql.ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String fullTs = rs.getString("timestamp");
                if (fullTs == null || fullTs.length() < 19) continue;
                
                java.time.LocalDateTime eventTime = java.time.LocalDateTime.parse(fullTs, formatter);
                if (eventTime.isAfter(weekAgo)) {
                    String mod = rs.getString("module");
                    dist.put(mod, dist.getOrDefault(mod, 0) + 1);
                }
            }
        } catch (java.sql.SQLException e) { e.printStackTrace(); }
        return dist;
    }


}
