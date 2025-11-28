package smartHome;

import java.util.Scanner;

public class Menu {
    private String username;
    private Scanner sc = new Scanner(System.in);
    private ApplicationModule appModule = new ApplicationModule();

    public Menu(String username) {
        this.username = username;
    }

    public void showMenu() {
        String choice = "";

        while (!choice.equals("5")) {
            System.out.println("=========== MAIN MENU ===========");
            System.out.println("1. Monitor Child Module");
            System.out.println("2. Application Module");
            System.out.println("3. Security Module");
            System.out.println("4. Show User Info");
            System.out.println("5. Exit");
            System.out.print("Enter choice: ");

            choice = sc.nextLine();

            switch (choice) {
                case "1":
                    new MonitorChildModule().run();
                    break;
                case "2":
                    showApplicationModuleMenu();
                    break;
                case "3":
                    new SecurityModule().run();
                    break;
                case "4":
                    System.out.println("User: " + username);
                    break;
                case "5":
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid input!");
            }

            System.out.println();
        }
    }

    private void showApplicationModuleMenu() {
        String choice = "";
        while (!choice.equals("5")) {
            System.out.println("=== Application Module ===");
            System.out.println("1. Add Room");
            System.out.println("2. Add Device to Room");
            System.out.println("3. Show Rooms and Devices");
            System.out.println("4. Control Device");
            System.out.println("5. Back to Main Menu");
            System.out.print("Enter choice: ");

            choice = sc.nextLine();

            switch (choice) {
                case "1": appModule.addRoom(); break;
                case "2": appModule.addDevice(); break;
                case "3": appModule.showRoomsAndDevices(); break;
                case "4": appModule.controlDevice(); break;
                case "5": System.out.println("Returning to Main Menu..."); break;
                default: System.out.println("Invalid choice!");
            }

            System.out.println();
        }
    }
}