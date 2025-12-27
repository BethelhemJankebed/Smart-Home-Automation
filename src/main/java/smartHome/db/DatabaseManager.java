package smartHome.db;

import smartHome.app.Room;
import smartHome.app.Device;
import smartHome.app.Light;
import smartHome.app.Fan;
import smartHome.app.TV;
import smartHome.app.DoorLock;
import smartHome.app.Camera;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:users.db";

    private static void createLocationsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS locations (" +
                     "person TEXT PRIMARY KEY," +
                     "room_id INTEGER," +
                     "timestamp TEXT" +
                     ");";
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) { stmt.execute(sql); } catch (SQLException e) { e.printStackTrace(); }
    }
    

    public static void updateLocation(String person, int roomId) {
        String sql = "INSERT OR REPLACE INTO locations(person, room_id, timestamp) VALUES(?,?,?)";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, person);
            ps.setInt(2, roomId);
            ps.setString(3, java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static String[] getLastLocation(String person) {
        String sql = "SELECT l.timestamp, r.name FROM locations l JOIN rooms r ON l.room_id = r.id WHERE l.person = ?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, person);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new String[]{ rs.getString("name"), rs.getString("timestamp") };
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }



    public static Connection connect() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        // Set busy timeout to wait for locks to clear (crucial for concurrency)
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA busy_timeout = 5000;");
        }
        return conn;
    }

    private static void enableWALMode() {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL;");
        } catch (SQLException e) {
            // If WAL fails, it's usually because the DB is busy, but busy_timeout might help next time.
            System.err.println("DB_DEBUG: Could not set WAL mode (might already be active or DB is busy): " + e.getMessage());
        }
    }

    private static void createRoomsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS rooms (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                     "name TEXT NOT NULL," +
                     "icon TEXT" +
                     ");";
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) { stmt.execute(sql); } catch (SQLException e) { e.printStackTrace(); }
    }

    private static void createUsersTable() {
        String sql =
                "CREATE TABLE IF NOT EXISTS users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "username TEXT UNIQUE NOT NULL," +
                        "password TEXT NOT NULL" +
                        ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createDevicesTable() {
        String sql = "CREATE TABLE IF NOT EXISTS devices (" +
                     "id TEXT PRIMARY KEY," +
                     "type TEXT," +
                     "name TEXT," +
                     "room_id INTEGER," +
                     "state BOOLEAN," +
                     "source TEXT" +
                     ");";
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) { 
            stmt.execute(sql);
            // Migration: Add source column if table existed before
            try {
                stmt.execute("ALTER TABLE devices ADD COLUMN source TEXT;");
            } catch (SQLException e) { /* ignore */ }
            // Migration: Add linked_param column for linking devices (e.g. Light -> Camera ID)
            try {
                stmt.execute("ALTER TABLE devices ADD COLUMN linked_param TEXT;");
            } catch (SQLException e) { /* ignore */ }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Call this in static block
    static {
        enableWALMode(); // Set WAL mode first
        createUsersTable();
        createRoomsTable();
        createDevicesTable();
        createEventsTable();
        createLocationsTable();
        createAlertsTable();
        createFaceTable();
    }

    public static void logSystemActivity(String module, String event, String level) {
        String sql = "INSERT INTO events(timestamp, module, details, level) VALUES(?,?,?,?)";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ps.setString(2, module);
            ps.setString(3, event);
            ps.setString(4, level);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private static void createEventsTable() {
         String sql = "CREATE TABLE IF NOT EXISTS events (" +
                      "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                      "device_id TEXT," +
                      "type TEXT," +
                      "timestamp TEXT," +
                      "details TEXT," +
                      "module TEXT," +
                      "level TEXT" +
                      ");";
         try (Connection conn = connect(); Statement stmt = conn.createStatement()) { 
             stmt.execute(sql); 
             // Migrations: Add module and level if they don't exist
             try { stmt.execute("ALTER TABLE events ADD COLUMN module TEXT"); } catch (SQLException e) {}
             try { stmt.execute("ALTER TABLE events ADD COLUMN level TEXT"); } catch (SQLException e) {}
         } catch (SQLException e) { e.printStackTrace(); }
    }

    private static void createAlertsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS alerts (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                     "room_id INTEGER," +
                     "timestamp TEXT," +
                     "type TEXT," +
                     "snapshot_data BLOB," +
                     "message TEXT" +
                     ");";
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) { 
            stmt.execute(sql);
            // Add snapshot_data column if it doesn't exist (for existing databases)
            try {
                stmt.execute("ALTER TABLE alerts ADD COLUMN snapshot_data BLOB");
            } catch (SQLException e) { /* Column exists */ }
            
            // CLEANUP: Remove legacy alerts that have no blob data
            try {
                stmt.execute("DELETE FROM alerts WHERE snapshot_data IS NULL");
            } catch (SQLException e) { /* ignore */ }
            
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
    }    private static void createFaceTable() {
        String sql = "CREATE TABLE IF NOT EXISTS registered_faces (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                     "name TEXT UNIQUE NOT NULL," +
                     "category TEXT NOT NULL," +
                     "image_data BLOB" +
                     ");";
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) { 
            stmt.execute(sql);
            // Add image_data column if it doesn't exist (for existing databases)
            try {
                stmt.execute("ALTER TABLE registered_faces ADD COLUMN image_data BLOB");
            } catch (SQLException e) { /* Column exists */ }
            
            // CLEANUP: Remove legacy faces that have no blob data (like 'Bettychild' from user screenshot)
            try {
                stmt.execute("DELETE FROM registered_faces WHERE image_data IS NULL");
            } catch (SQLException e) { /* ignore */ }
            
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void registerFace(String name, String category, byte[] imageData) {
        String sql = "INSERT OR REPLACE INTO registered_faces(name, category, image_data) VALUES(?,?,?)";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, category);
            ps.setBytes(3, imageData);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static List<String[]> getAllRegisteredFaces() {
        List<String[]> faces = new ArrayList<>();
        String sql = "SELECT name, category FROM registered_faces";
        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                faces.add(new String[]{rs.getString("name"), rs.getString("category")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return faces;
    }
    


    public static void deleteFace(String name) {
        String sql = "DELETE FROM registered_faces WHERE name = ?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }


    
    // New method for BLOB storage
    public static void saveAlertWithBlob(int roomId, String type, byte[] snapshotData, String message) {
        String sql = "INSERT INTO alerts(room_id, timestamp, type, snapshot_data, message) VALUES(?,?,?,?,?)";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.setString(2, java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ps.setString(3, type);
            ps.setBytes(4, snapshotData);
            ps.setString(5, message);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }



    public static List<String[]> getRecentAlertsForRoomByType(int roomId, String type) {
        List<String[]> alerts = new ArrayList<>();
        String sql = "SELECT id, timestamp, message FROM alerts WHERE room_id = ? AND type = ? ORDER BY id DESC LIMIT 10";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.setString(2, type);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                alerts.add(new String[]{
                    rs.getString("timestamp"),
                    String.valueOf(rs.getInt("id")), // Return ID instead of path
                    rs.getString("message")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return alerts;
    }
    
    public static byte[] getAlertSnapshot(int alertId) {
        String sql = "SELECT snapshot_data FROM alerts WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, alertId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBytes("snapshot_data");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }



    public static void deleteAlertById(int id) {
        String sql = "DELETE FROM alerts WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            System.out.println("DB_DEBUG: Successfully deleted alert ID " + id + ". Rows: " + rows);
        } catch (SQLException e) { 
            System.err.println("DB_ERROR: deleteAlertById failed for ID " + id + ": " + e.getMessage());
        }
    }

    public static boolean registerUser(String username, String password) {
        String sql = "INSERT INTO users(username, password) VALUES(?, ?)";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            return false; // username exists
        }
    }

    public static boolean validateLogin(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ===== ROOMS =====

    public static int insertRoom(String name, String icon) {
        String sql = "INSERT INTO rooms(name, icon) VALUES(?, ?)";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, icon);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void addDevice(Device device, int roomId) {
        String sql = "INSERT OR REPLACE INTO devices(id, type, name, room_id, state, source, linked_param) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, device.getId());
            ps.setString(2, device.getType());
            ps.setString(3, device.getName());
            ps.setInt(4, roomId);
            ps.setBoolean(5, device.isOn());
            
            String source = null;
            if (device instanceof Camera cam) {
                source = cam.getStreamSource();
            }
            ps.setString(6, source);

            String linked = null;
            if (device instanceof Light l) {
                linked = l.getLinkedCameraId();
            }
            ps.setString(7, linked);
            
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static List<Device> getDevicesForRoom(int roomId) {
        List<Device> devices = new ArrayList<>();
        String sql = "SELECT * FROM devices WHERE room_id = ?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                String id = rs.getString("id");
                String type = rs.getString("type");
                String name = rs.getString("name");
                boolean state = rs.getBoolean("state"); // State is mostly runtime, but storing initial state? Or just restoring?
                
                String source = rs.getString("source");
                String linkedParam = rs.getString("linked_param");
                
                Device d = null;
                switch(type) {
                    case "Light" -> {
                        Light l = new Light(id, name);
                        if (linkedParam != null && !linkedParam.isEmpty()) {
                            l.setLinkedCameraId(linkedParam);
                        }
                        d = l;
                    }
                    case "Fan" -> d = new Fan(id, name);
                    case "DoorLock" -> d = new DoorLock(id, name);
                    case "TV" -> d = new TV(id, name);
                    case "Camera" -> {
                        Camera cam = new Camera(id, name, null);
                        cam.setStreamSource(source);
                        d = cam;
                    }
                }
                if (d != null) {
                    if (state) d.turnOn(); // Logic to restore state
                    devices.add(d);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return devices;
    }

    public static void deleteDevice(String deviceId) {
        String sql = "DELETE FROM devices WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, deviceId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void logEvent(String deviceId, String type, String details) {
        String sql = "INSERT INTO events(device_id, type, timestamp, details, module, level) VALUES(?,?,?,?,?,?)";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, deviceId);
            ps.setString(2, type);
            ps.setString(3, java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ps.setString(4, details);
            ps.setString(5, "Device Controls");
            ps.setString(6, "INFO");
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static List<String[]> getEventsForRoom(int roomId) {
        List<String[]> events = new ArrayList<>();
        // Join with devices to filter by room
        String sql = "SELECT e.type, d.name, e.timestamp, e.details FROM events e " +
                     "JOIN devices d ON e.device_id = d.id " +
                     "WHERE d.room_id = ? " +
                     "ORDER BY e.id DESC LIMIT 20";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                events.add(new String[]{
                    rs.getString("type"),
                    rs.getString("name"),
                    rs.getString("timestamp"),
                    rs.getString("details")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return events;
    }

    public static List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms";
        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rooms.add(new Room(rs.getInt("id"), rs.getString("name"), rs.getString("icon")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return rooms;
    }

    public static int getActiveDeviceCountForRoom(int roomId) {
        String sql = "SELECT COUNT(*) FROM devices WHERE room_id = ? AND state = 1";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roomId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

    public static int getTotalDeviceCountForRoom(int roomId) {
        String sql = "SELECT COUNT(*) FROM devices WHERE room_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roomId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

    
    public static void updateDeviceState(String deviceId, boolean state) {
        String sql = "UPDATE devices SET state = ? WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, state);
            ps.setString(2, deviceId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    public static List<Object[]> getAllRegisteredFacesWithData() {
        List<Object[]> faces = new ArrayList<>();
        String sql = "SELECT name, category, image_data FROM registered_faces";
        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                faces.add(new Object[]{
                    rs.getString("name"), 
                    rs.getString("category"),
                    rs.getBytes("image_data")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return faces;
    }

    public static void deleteRoom(int roomId) {
        String sql = "DELETE FROM rooms WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


