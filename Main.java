import java.util.Scanner;
import smartHome.Menu;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String username;
        String password;

        System.out.println("======================================");
        System.out.println("       SMART HOME CONTROL SYSTEM      ");
        System.out.println("======================================");

        // LOGIN
        while (true) {
            System.out.print("Enter your username: ");
            username = sc.nextLine();

            System.out.print("Enter password (1234): ");
            password = sc.nextLine();

            if (password.equals("1234")) {
                System.out.println("\nLogin Successful! Welcome " + username + "!\n");
                break;
            } else {
                System.out.println("Incorrect password! Try again.\n");
            }
        }

        // SHOW MAIN MENU
        Menu menu = new Menu(username);
        menu.showMenu();

        sc.close();
    }
}