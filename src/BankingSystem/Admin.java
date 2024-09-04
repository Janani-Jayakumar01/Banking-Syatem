package BankingSystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Admin {
    static Scanner sc = new Scanner(System.in);
    static Connection conn;
    static String loggedInUsername;

    public static void initializeConnection() {
        // Initialize the connection here
        conn = DbConnector.getConnection();
    }

    public static void input() throws SQLException {
        initializeConnection();

        while (true) {
            System.out.println("1. Add User");
            System.out.println("2. View User");
            System.out.println("3. Update User");
            System.out.println("4. Delete User");
            System.out.println("5. View Entire User Details");
            System.out.println("6. Exit");

            System.out.println("Enter your choice:");
            int choice = sc.nextInt();

            switch (choice) {
                case 1:
                    addUser();
                    break;
                case 2:
                    viewUser();
                    break;
                case 3:
                    updateUser();
                    break;
                case 4:
                    deleteUser();
                    break;
                case 5:
                    viewEntireUserDetails();
                    break;
                case 6:
                    System.out.println("Exiting admin Operations...");
                    System.out.println("         THANK YOU         ");
                    return;
                default:
                    System.out.println("Incorrect Input");
            }
        }
    }

    public static void addUser() {
        try {
            System.out.println("Enter Username:");
            String username = sc.next();
            System.out.println("Enter Password:");
            String password = sc.next();
            System.out.println("Enter Name:");
            String name = sc.next();
            System.out.println("Enter Email:");
            String email = sc.next();
            String phoneNumber;
            do {
                System.out.println("Enter Phone Number:");
                phoneNumber = sc.next();
                if (!isValidPhoneNumber(phoneNumber)) {
                    System.out.println("Invalid phone number. Please enter a 10-digit number.");
                }
            } while (!isValidPhoneNumber(phoneNumber));
            System.out.println("Enter Amount:");
            String balanceInput = sc.next();

            try {
                double balance = Double.parseDouble(balanceInput);

                // Generate a unique account number (you can customize this generation logic)
                String accountNumber = generateAccountNumber();

                System.out.println("Enter Aadhar Number:");
                String aadharNumber = sc.next();
                System.out.println("Enter Account Type:");
                String accountType = sc.next();
                System.out.println("Enter Date of Birth (YYYY-MM-DD):");
                String dob = sc.next();
                System.out.println("Enter PIN Number:");
                String pin = sc.next();

                String query = "INSERT INTO user (username, password, name, email, phoneNumber, bankbalance, accountNumber, aadharNumber, accountType, dob, pin) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement statement = conn.prepareStatement(query)) {
                    statement.setString(1, username);
                    statement.setString(2, password);
                    statement.setString(3, name);
                    statement.setString(4, email);
                    statement.setString(5, phoneNumber);
                    statement.setDouble(6, balance);
                    statement.setString(7, accountNumber);
                    statement.setString(8, aadharNumber);
                    statement.setString(9, accountType);
                    statement.setString(10, dob);
                    statement.setString(11, pin);

                    statement.executeUpdate();
                    System.out.println("User added successfully with accountnumber: " + accountNumber);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input for balance. Please enter a valid number.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    private static boolean isValidPhoneNumber(String phoneNumber) {
        String regex = "^[0-9]{10}$";
        return phoneNumber.matches(regex);
    }
    private static String generateAccountNumber() {
        return "ACC" + System.currentTimeMillis();
    }
    
    public static void viewUser() {
        boolean userFound = false;

        while (!userFound) {
            try {
                System.out.print("Enter User Name:");
                String name = sc.next();
                System.out.print("Enter User Account Number:");
                String accountNumber = sc.next();
                sc.nextLine(); // consume the newline character

                String query = "SELECT * FROM user WHERE name = ? AND accountNumber = ?";
                try (PreparedStatement statement = conn.prepareStatement(query)) {
                    statement.setString(1, name);
                    statement.setString(2, accountNumber);

                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            userFound = true;

                            String userName = resultSet.getString("username");
                            String password = resultSet.getString("password");
                            String email = resultSet.getString("email");
                            String phoneNumber = resultSet.getString("phoneNumber");
                            double bankBalance = resultSet.getDouble("bankBalance");
                            String aadharNumber = resultSet.getString("aadharNumber");
                            String accountType = resultSet.getString("accountType");
                            String dob = resultSet.getString("dob");
                            String pin = resultSet.getString("pin");

                            System.out.println("--------------------------");
                            System.out.println("Username: " + userName);
                            System.out.println("Password: " + password);
                            System.out.println("Email: " + email);
                            System.out.println("Phone Number: " + phoneNumber);
                            System.out.println("Bank Balance: " + bankBalance);
                            System.out.println("Aadhar Number: " + aadharNumber);
                            System.out.println("Account Type: " + accountType);
                            System.out.println("Date Of Birth: " + dob);
                            System.out.println("Pin: " + pin);
                            System.out.println("--------------------------");
                        } else {
                            System.out.println("User not found. Please enter valid values.");
                        }
                    }
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input for userId. Please enter a valid number.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }


    private static void updateUser() throws SQLException {
        try {
            System.out.println("Enter User Account Number:");
            String accountNumber = sc.next();
            System.out.println("Enter User Username:");
            String username = sc.next();

            // Loop until a valid combination of account number and username is provided
            while (!userExistsByAccountNumberAndUsername(accountNumber, username)) {
                System.out.println("Invalid account number or username. Please enter valid account number and username.");
                System.out.println("Enter User Account Number:");
                accountNumber = sc.next();
                System.out.println("Enter User Username:");
                username = sc.next();
            }

            updateUser(accountNumber, username);
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid number.");
        }
    }


    private static void updateUser(String accountNumber, String username) {
        try {
            // Check if the provided account number and username match in the database
            if (userExistsByAccountNumberAndUsername(accountNumber, username)) {
                System.out.println("Enter new name (or press Enter to keep the existing value):");
                sc.nextLine();
                String newName = sc.nextLine().trim();
                if (!newName.isEmpty()) {
                    updateField(username, "name", newName);
                }

                System.out.println("Enter new email (or press Enter to keep the existing value):");
                String newEmail = sc.nextLine().trim();
                if (!newEmail.isEmpty()) {
                    updateField(username, "email", newEmail);
                }

                System.out.println("Enter new phone number (or press Enter to keep the existing value):");
                String newPhoneNumber = sc.nextLine().trim();
                if (!newPhoneNumber.isEmpty()) {
                    updateField(username, "phoneNumber", newPhoneNumber);
                }

                System.out.println("Enter new account type (or press Enter to keep the existing value):");
                String newAccountType = sc.nextLine().trim();
                if (!newAccountType.isEmpty()) {
                    updateField(username, "accountType", newAccountType);
                }

                System.out.println("User details updated successfully.");
            } else {
                System.out.println("Invalid account number or username. Please enter valid account number and username.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static boolean userExistsByAccountNumberAndUsername(String accountNumber, String username) throws SQLException {
        String query = "SELECT * FROM user WHERE accountNumber = ? AND username = ?";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, accountNumber);
            statement.setString(2, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static void updateField(String username, String fieldName, String newValue) throws SQLException {
        String updateQuery = "UPDATE user SET " + fieldName + " = ? WHERE accountNumber = ?";
        try (PreparedStatement updateStatement = conn.prepareStatement(updateQuery)) {
            updateStatement.setString(1, newValue);
            updateStatement.setString(2, username);

            int rowsAffected = updateStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println(fieldName + " updated successfully to: " + newValue);
            } else {
                System.out.println("Failed to update " + fieldName + ".");
            }
        }
    }
    public static void deleteUser() throws SQLException {
        boolean validInput = false;

        while (!validInput) {
            try {
                System.out.println("Enter User Account Number:");
                String accountNumberToDelete = sc.next();

                System.out.println("Enter User Username:");
                String usernameToDelete = sc.next();

                if (userExists(accountNumberToDelete, usernameToDelete)) {
                    String query = "DELETE FROM user WHERE accountNumber = ? AND username = ?";
                    try (PreparedStatement statement = conn.prepareStatement(query)) {
                        statement.setString(1, accountNumberToDelete);
                        statement.setString(2, usernameToDelete);

                        int rowsAffected = statement.executeUpdate();
                        if (rowsAffected > 0) {
                            System.out.println("User Details Deleted Successfully.");
                            validInput = true; // Set the flag to exit the loop
                        } else {
                            System.out.println("Deletion unsuccessful. Please try again.");
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        System.out.println("Error deleting user details. Please try again.");
                    }
                } else {
                    System.out.println("Invalid account number or username. Please enter valid account number and username.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid value.");
            }
        }
    }


    private static boolean userExists(String accountNumber, String username) throws SQLException {
        String query = "SELECT * FROM user WHERE accountNumber = ? AND username = ?";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, accountNumber);
            statement.setString(2, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }


    public static void viewEntireUserDetails() {
        try {
            String query = "SELECT * FROM user";
            try (PreparedStatement statement = conn.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String username = resultSet.getString("username");
                    String password = resultSet.getString("password");
                    String empName = resultSet.getString("name");
                    String email = resultSet.getString("email");
                    String phoneNumber = resultSet.getString("phoneNumber");
                    double bankBalance = resultSet.getDouble("bankBalance");
                    String accNumber = resultSet.getString("accountNumber");
                    String aadharNumber = resultSet.getString("aadharNumber");
                    String accountType = resultSet.getString("accountType");
                    String dob = resultSet.getString("dob");
                    String pin = resultSet.getString("pin");

                    System.out.println("--------------------------");
                    System.out.println("Employee Username: " + username);
                    System.out.println("Employee Password: " + password);
                    System.out.println("Employee Name: " + empName);
                    System.out.println("Email: " + email);
                    System.out.println("Phone Number: " + phoneNumber);
                    System.out.println("Bank Balance: " + bankBalance);
                    System.out.println("Account Number: " + accNumber);
                    System.out.println("Aadhar Number: " + aadharNumber);
                    System.out.println("Account Type: " + accountType);
                    System.out.println("DOB: " + dob);
                    System.out.println("Pin: " + pin);
                    System.out.println("--------------------------");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
