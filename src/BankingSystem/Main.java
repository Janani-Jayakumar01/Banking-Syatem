package BankingSystem;

import java.sql.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        System.out.println("Welcome to Banking System");
        System.out.println("=========================");

        try (Connection conn = DbConnector.getConnection()) {
            //TransactionManager.setConnection(conn);

            while (!exit) {
                System.out.println("Select your option:");
                System.out.println("1. Login");
                System.out.println("2. Signup");
                System.out.println("3. Exit");
                System.out.println("Enter your choice: ");

                int choice = 0;

                try {
                    choice = scanner.nextInt();
                    scanner.nextLine(); // Consume the newline character
                } catch (Exception e) {
                    System.out.println("Invalid input. Please enter a valid number.");
                    scanner.nextLine(); // Consume the invalid input
                    continue;
                }

                switch (choice) {
                    case 1:
                        Login.login();
                        break;
                    case 2:
                        Login.signup();
                        break;
                    case 3:
                    	System.out.println("Thank you for using Banking System. ");
                    	System.out.println("EXITING....");
                        exit = true;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
