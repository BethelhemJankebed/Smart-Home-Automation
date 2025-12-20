package smartHome;

import smartHome.Room;

import java.util.ArrayList;
import java.util.List;

public class HomeManager {

    private List<Room> rooms;

    public HomeManager() {
        rooms = new ArrayList<>();
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void addRoom(Room room) {
        rooms.add(room);
    }

    public void removeRoom(Room room) {
        rooms.remove(room);
    }
}
