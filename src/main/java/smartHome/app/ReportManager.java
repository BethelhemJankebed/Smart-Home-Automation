package smartHome.app;

import java.util.List;

public class ReportManager {

    public List<String> getSystemLogs(String period) {
        List<String> logs = new java.util.ArrayList<>();
        String sql = "SELECT timestamp, module, event FROM system_logs WHERE timestamp LIKE ? ORDER BY id DESC";
        
        String pattern = getPatternForPeriod(period);
        
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection("jdbc:sqlite:users.db");
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            java.sql.ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String time = rs.getString("timestamp").substring(11, 16); // HH:mm
                logs.add(String.format("%s | %s | %s", time, rs.getString("module"), rs.getString("event")));
            }
        } catch (java.sql.SQLException e) { e.printStackTrace(); }
        return logs;
    }

    public java.util.Map<String, Integer> getModuleActivityDistribution(String period) {
        java.util.Map<String, Integer> dist = new java.util.HashMap<>();
        String sql = "SELECT module, COUNT(*) as c FROM system_logs WHERE timestamp LIKE ? GROUP BY module";
        
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection("jdbc:sqlite:users.db");
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
            default -> "%"; // All time
        };
    }

    public String generateSummaryReport() {
        List<String> logs = getSystemLogs("Daily");
        return "Total Activities Today: " + logs.size();
    }
}
