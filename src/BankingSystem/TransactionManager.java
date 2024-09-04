/*package BankingSystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class TransactionManager {
    static Scanner scanner = new Scanner(System.in);
    static Connection conn;
    static String loggedInUsername;

    // Set the connection for the TransactionManager class
    public static void setConnection(Connection connection) {
        conn = connection;
    }

    // Input method for TransactionManager class
    public static void input() {
        System.out.println("Transaction Operations");
        System.out.println("1. View Transactions");
        System.out.println("2. Add Transaction");
        System.out.println("3. Update Transaction");
        System.out.println("4. Delete Transaction");
        System.out.println("5. Back to Admin Menu");
        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                viewTransactions(loggedInUsername);
                break;
            case 2:
                addTransaction(loggedInUsername);
                break;
            case 3:
                updateTransaction();
                break;
            case 4:
                deleteTransaction();
                break;
            case 5:
                Admin.input(); // Navigate back to the Admin menu
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    // View transactions method
 // View transactions method for a specific user
    private static void viewTransactions(String username) {
        try {
            String query = "SELECT * FROM transaction WHERE username = ?";
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, username);

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        int transactionId = resultSet.getInt("transaction_id");
                        String type = resultSet.getString("type");
                        double amount = resultSet.getDouble("amount");
                        String description = resultSet.getString("description");
                        String date = resultSet.getString("transaction_date");

                        System.out.println("--------------------------");
                        System.out.println("Transaction ID: " + transactionId);
                        System.out.println("Username: " + username);
                        System.out.println("Type: " + type);
                        System.out.println("Amount: " + amount);
                        System.out.println("Description: " + description);
                        System.out.println("Date: " + date);
                        System.out.println("--------------------------");
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Modify the original viewTransactions method to call the new method with the logged-in username
 // Add transaction method
 // Add transaction method
    private static void addTransaction(String username) {
        try {
            scanner.nextLine(); // Consume the newline character
            System.out.println("Enter User Name:");
            String username1=scanner.nextLine();
            System.out.println("Enter Transaction Type (Credit/Debit): ");
            String type = scanner.nextLine();
            System.out.println("Enter Amount: ");
            double amount = scanner.nextDouble();
            scanner.nextLine(); // Consume the newline character
            System.out.println("Enter Description: ");
            String description = scanner.nextLine();

            // Retrieve the user's current balance
            double currentBalance = getCurrentBalance(username1);

            // Update the user's balance based on the transaction type
            double newBalance = calculateNewBalance(currentBalance, type, amount);

            // Prepare and execute the insert query
            String query = "INSERT INTO transaction (username, type, amount, description) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, username1);
                statement.setString(2, type);
                statement.setDouble(3, amount);
                statement.setString(4, description);

                // Execute the insert query
                statement.executeUpdate();

                // Update the user's balance in the user table
                updateBalanceInUser(username1, newBalance);

                System.out.println("Transaction added successfully.");
                System.out.println("User's balance updated successfully.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    // Sample method to update the user's balance in the user table
    
    // Update transaction method
    private static void updateTransaction() {
        try {
            System.out.println("Enter Transaction ID to Update: ");
            int transactionId = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character
            System.out.println("Enter New UserName: ");
            String newUsername = scanner.nextLine();
            System.out.println("Enter New Type: ");
            String newType = scanner.nextLine();
            System.out.println("Enter New Description: ");
            String newDescription = scanner.nextLine();

            // Prepare and execute the update query
            String query = "UPDATE transaction SET username=?,type=?,description = ? WHERE transaction_id = ?";
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, newUsername);
                statement.setString(2, newType);
                statement.setString(3, newDescription);
                statement.setInt(4, transactionId);

                statement.executeUpdate();
                System.out.println("Transaction updated successfully.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Delete transaction method
    private static void deleteTransaction() {
        try {
            System.out.println("Enter Transaction ID to Delete: ");
            int transactionId = scanner.nextInt();

            // Prepare and execute the delete query
            String query = "DELETE FROM transaction WHERE transaction_id = ?";
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setInt(1, transactionId);

                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Transaction Deleted Successfully.");
                } else {
                    System.out.println("Transaction not found or deletion unsuccessful.");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Sample method to get the user's current balance from the user table
    private static double getCurrentBalance(String username) {
        double currentBalance = 0.0;
        try {
            String query = "SELECT bankbalance FROM user WHERE username = ?";
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, username);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        currentBalance = resultSet.getDouble("bankbalance");
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return currentBalance;
    }

    // Sample method to calculate the new balance based on transaction type
    private static double calculateNewBalance(double currentBalance, String type, double amount) {
        if ("Credit".equalsIgnoreCase(type)) {
            return currentBalance + amount;
        } else if ("Debit".equalsIgnoreCase(type)) {
            return currentBalance - amount;
        } else {
            // Handle invalid transaction type
            System.out.println("Invalid transaction type. Balance remains unchanged.");
            return currentBalance;
        }
    }

    // Sample method to update the user's balance in the user table
    private static void updateBalanceInUser(String username, double newBalance) {
        try {
            String query = "UPDATE user SET bankbalance = ? WHERE username = ?";
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setDouble(1, newBalance);
                statement.setString(2, username);

                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("User's balance updated successfully.");
                } else {
                    System.out.println("User not found or balance update unsuccessful.");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}*/
