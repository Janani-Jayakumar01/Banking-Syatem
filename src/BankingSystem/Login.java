package BankingSystem;

import java.sql.Connection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Login {
    static Scanner scanner = new Scanner(System.in);

    public static void login() throws SQLException {
        boolean validCredentials = false;

        do {
            System.out.println("Login");
            System.out.println("Enter username: ");
            String username = scanner.nextLine();
            System.out.println("Enter password: ");
            String password = scanner.nextLine();

            String role = checkCredentials(username, password);

            switch (role) {
                case "admin":
                    System.out.println("Welcome to Admin Portal");
                    System.out.println("=======================");
                    Admin.input();
                    validCredentials = true;
                    break;
                case "user":
                    System.out.println("Welcome to User Portal");
                    System.out.println("======================");
                    try (Connection conn = DbConnector.getConnection()) {
                        User.setConnection(conn); // Pass the connection to User class
                        User.setLoggedInUsername(username); // Set the logged-in username
                        User.input(conn, username);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    validCredentials = true;
                    break;
                default:
                    System.out.println("Invalid credentials. Please try again.");
                    System.out.println("Enter valid username and password.");
            }
        } while (!validCredentials);
    }

    public static void signup() {
        try (Connection conn = DbConnector.getConnection()) {
            // Prompt user for registration details
            System.out.println("Enter username:");
            String username = scanner.nextLine();

            System.out.println("Enter password:");
            String password = scanner.nextLine();

            System.out.println("Enter name:");
            String name = scanner.nextLine();

            System.out.println("Enter email:");
            String email = scanner.nextLine();

            //System.out.println("Enter phone number:");
            String phoneNumber; //= scanner.nextLine();
            do {
            	System.out.println("Enter phone number:");
            	phoneNumber=scanner.nextLine();
            	if(!isValidPhoneNumber(phoneNumber)) {
            		 System.out.println("Not a valid phone number. Please enter a 10-digit number.");
            		// System.out.println("=========================================================");
            	}
            }while (!isValidPhoneNumber(phoneNumber));
            System.out.println("Enter initial balance:");
            String balanceInput = scanner.nextLine();
            
            System.out.println("Enter aadhar number:");
            String aadharNumber=scanner.nextLine();
            
            System.out.println("Enter account type:");
            String accountType=scanner.nextLine();
            
            System.out.println("Enter your DOB (yyyy-mm-dd):");
            String dob=scanner.nextLine();
            
            System.out.println("Enter your pin number:");
            String pin=scanner.nextLine();
            

            try {
                // Parse balance input as a double
                double bankbalance = Double.parseDouble(balanceInput);

                // Generate a unique account number
                String accountNumber = generateAccountNumber();

                // Check if the username already exists
                if (usernameExists(conn, username)) {
                    System.out.println("Username already exists. Please choose a different username.");
                    //System.out.println("============================================================");
                    return;
                }

                // Prepare the SQL query to insert a new user into the 'user' table
                String query = "INSERT INTO user (username, password, name, email, phoneNumber, bankbalance, accountNumber,aadharNumber,accountType,dob,pin) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement statement = conn.prepareStatement(query)) {
                    // Use PreparedStatement to avoid SQL injection

                    // Set values for each parameter in the prepared statement
                    statement.setString(1, username);
                    statement.setString(2, password);
                    statement.setString(3, name);
                    statement.setString(4, email);
                    statement.setString(5, phoneNumber);
                    statement.setDouble(6, bankbalance);
                    statement.setString(7, accountNumber); // Set the generated account number
                    statement.setString(8, aadharNumber);
                    statement.setString(9, accountType);
                    statement.setString(10, dob);
                    statement.setString(11, pin);
                    

                    // Execute the update, which inserts the new user into the 'user' table
                    statement.executeUpdate();

                    System.out.println("Signed In Successfully!. Your Account Number is: " + accountNumber);
                    //System.out.println("=================================================================");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input for balance. Please enter a valid number.");
                //System.out.println("=======================================================");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    public static boolean isValidPhoneNumber(String phoneNumber) {
    	
        String regex = "^[0-9]{10}$";

        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }

    
   

    private static String generateAccountNumber() {
        return "ACC" + System.currentTimeMillis();
    }


    private static boolean usernameExists(Connection conn, String username) throws SQLException {
    	try {
            String query = "SELECT * FROM user WHERE username = ?";
            
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, username);

                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next(); // Returns true if the username exists
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false; // Return false in case of an exception
        }    
    }





private static String checkCredentials(String username, String password) {
    try (Connection conn = DbConnector.getConnection()) {
        String query = "SELECT * FROM admin WHERE username = ? AND password = ?";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, password);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return "admin";
                }
            }
        }

        query = "SELECT * FROM user WHERE username = ? AND password = ?";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, password);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return "user";
                }
            }
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
    return "invalid";
}
}