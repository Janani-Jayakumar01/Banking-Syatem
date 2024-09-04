package BankingSystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.Scanner;

public class User {
    private static String loggedInUsername;
    private static Connection conn;
    private static Scanner sc = new Scanner(System.in);

    public static void setConnection(Connection connection) {
        conn = connection;
    }

    public static void setLoggedInUsername(String username) {
        loggedInUsername = username;
    }

    public static void input(Connection conn2, String username) {
        while (true) {
            System.out.println("1. Debit Money");
            System.out.println("2. Credit Money");
            System.out.println("3. Transfer Money");
            System.out.println("4. Check Balance");
            System.out.println("5. Print Statement");
            System.out.println("6. Print Statement between Two Dates");
            System.out.println("7. Print All Statement");
            System.out.println("8. Exit");

            System.out.println("Enter your choice: ");
            int choice = sc.nextInt();

            switch (choice) {
                case 1:
                    debitMoney();
                    break;
                case 2:
                    creditMoney();
                    break;
                case 3:
                    transferMoney();
                    break;
                case 4:
                    checkBalance();
                    break;
                case 5:
                    printStatement();
                    break;
                case 6:
                	printBetweenStatements();
                	break;
                case 7:
                    printAllStatements();
                    break;
                case 8:
                    System.out.println("Exiting User Operations....");
                    System.out.println();
                    System.out.println("         THANK YOU         ");
                    System.out.println();
                    return;
                default:
                    System.out.println("Invalid choice. Please enter a valid option.");
            }
        }
    }


    private static void debitMoney() {
        System.out.println("Enter the amount to debit:");
        double amount = sc.nextDouble();

        try {
            if (userExists(loggedInUsername)) {
                if (hasSufficientFunds(loggedInUsername, amount)) {
                    updateBankBalance(loggedInUsername, -amount, true);
                    System.out.println("Debit successful. Amount debited: " + amount);
                    recordTransaction("Debit", amount, loggedInUsername, null);
                } else {
                    System.out.println("Debit unsuccessful. Insufficient funds.");
                }
            } else {
                System.out.println("Debit unsuccessful. User not found.");
            }
        } catch (SQLException ex) {
            handleSQLException(ex);
        }
    }
    private static boolean userExists(String username) throws SQLException {
        String query = "SELECT * FROM user WHERE username = ?";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static boolean hasSufficientFunds(String username, double amount) throws SQLException {
        String query = "SELECT bankbalance FROM user WHERE username = ?";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    double currentBalance = resultSet.getDouble("bankbalance");
                    return currentBalance >= amount;
                } else {
                    return false;
                }
            }
        }
    }

    private static void creditMoney() {
        try {
            System.out.println("Enter the amount to credit:");
            double amount = sc.nextDouble();

            if (amount <= 0) {
                System.out.println("Invalid amount. Please enter a positive value for credit.");
                return;
            }

            if (userExists(loggedInUsername)) {
                String creditQuery = "UPDATE user SET bankbalance = bankbalance + ? WHERE username = ?";
                try (PreparedStatement creditStatement = conn.prepareStatement(creditQuery)) {
                    creditStatement.setDouble(1, amount);
                    creditStatement.setString(2, loggedInUsername);

                    int rowsAffected = creditStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Credit successful. Amount credited: " + amount);
                        recordTransaction("Credit", amount, loggedInUsername, null); // Pass null for both sender and recipient
                    } else {
                        System.out.println("Credit unsuccessful. Please try again.");
                    }
                }
            } else {
                System.out.println("Credit unsuccessful. User not found.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid numeric value for the amount.");
            sc.nextLine(); // Consume the invalid input to avoid an infinite loop
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    private static void transferMoney() {
        System.out.println("Enter the recipient's account number and the amount to transfer (separated by a space):");
        sc.nextLine(); // Consume the newline character
        String inputLine = sc.nextLine();

        // Split the input line into an array of strings
        String[] inputParts = inputLine.split(" ");

        // Check if there are at least two parts (account number and amount)
        if (inputParts.length >= 2) {
            String recipientAccountNumber = inputParts[0];

            try {
                double amount = Double.parseDouble(inputParts[1]);

                // Check if both the sender and recipient exist
                if (userExists(loggedInUsername) && userExistsByAccountNumber(recipientAccountNumber)) {
                    // Continue with the transfer operation
                    if (hasSufficientFunds(loggedInUsername, amount)) {
                        // Update sender's balance
                        updateBankBalance(loggedInUsername, -amount,true);

                        // Update recipient's balance
                        String recipientUsername = getUsernameByAccountNumber(recipientAccountNumber);
                        updateBankBalance(recipientUsername, amount,false);

                        // Record Debit Transaction with recipient's username
                        recordTransaction("Debit", amount, loggedInUsername, recipientAccountNumber);
                        System.out.println("Transfer successful. Amount transferred: " + amount);
                    } else {
                        System.out.println("Transfer unsuccessful. Insufficient funds.");
                    }
                } else {
                    System.out.println("Transfer unsuccessful. Sender or recipient not found.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input for the amount. Please enter a valid number.");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } else {
            System.out.println("Invalid input format. Please enter both account number and amount.");
        }
    }

    private static String getUsernameByAccountNumber(String accountNumber) throws SQLException {
        String query = "SELECT username FROM user WHERE accountnumber = ?";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, accountNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("username");
                } else {
                    throw new SQLException("Recipient not found with account number: " + accountNumber);
                }
            }
        }
    }
    
    private static boolean userExistsByAccountNumber(String accountNumber) throws SQLException {
        String query = "SELECT * FROM user WHERE accountNumber = ?";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, accountNumber);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }
    

    private static void checkBalance() {
        try {
            String query = "SELECT bankbalance FROM user WHERE username = ?";
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, loggedInUsername);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        double currentBalance = resultSet.getDouble("bankbalance");
                        System.out.println("Current Balance: " + currentBalance);
                    } else {
                        System.out.println("User not found.");
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static void printStatement() {
        try {
            System.out.print("Enter the date (YYYY-MM-DD): ");
            String dateStr = sc.next();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date;

            try {
                date = sdf.parse(dateStr);
            } catch (ParseException e) {
                System.out.println("Invalid date format. Please enter a valid date in the format YYYY-MM-DD.");
                return;
            }

            String query = "SELECT * FROM transaction WHERE (sender_username = ? OR recipient_username = ?) AND DATE_FORMAT(transaction_date, '%Y-%m-%d') = ?";
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, loggedInUsername);
                statement.setString(2, loggedInUsername);
                statement.setString(3, sdf.format(date));

                try (ResultSet resultSet = statement.executeQuery()) {
                    displayTransactionResultSet(resultSet);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void printBetweenStatements() {
        try {
            System.out.print("Enter the start date (YYYY-MM-DD): ");
            String startDateStr = sc.next();

            System.out.print("Enter the end date (YYYY-MM-DD): ");
            String endDateStr = sc.next();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = sdf.parse(startDateStr);
            Date endDate = sdf.parse(endDateStr);

            String query = "SELECT * FROM transaction WHERE (sender_username = ? OR recipient_username = ?) AND transaction_date BETWEEN ? AND ?";
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, loggedInUsername);
                statement.setString(2, loggedInUsername);
                statement.setString(3, sdf.format(startDate));
                statement.setString(4, sdf.format(endDate));

                try (ResultSet resultSet = statement.executeQuery()) {
                    displayTransactionResultSet(resultSet);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private static void printAllStatements() {
        try {
            String query = "SELECT * FROM transaction WHERE sender_username = ? OR recipient_username = ?";
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, loggedInUsername);
                statement.setString(2, loggedInUsername);

                try (ResultSet resultSet = statement.executeQuery()) {
                    displayTransactionResultSet(resultSet);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static void displayTransactionResultSet(ResultSet resultSet) throws SQLException {
        if (resultSet.isBeforeFirst()) {
            System.out.println("=========================");
            System.out.println("Transaction Details");
            System.out.println("=========================");
            System.out.printf("%-20s %-20s %-20s %-20s %-20s%n",
                    "Sender", "Recipient", "Amount", "Type", "Transaction Date");

            while (resultSet.next()) {
                String sender = resultSet.getString("sender_username");
                String recipient = resultSet.getString("recipient_username");
                double amount = resultSet.getDouble("amount");
                String type = resultSet.getString("type");
                Date transactionDate = resultSet.getTimestamp("transaction_date");

                if (sender != null && sender.equals(loggedInUsername)) {
                    System.out.printf("%-20s %-20s %-20f %-20s %-20s%n",
                            sender, recipient, amount, "Debit", transactionDate);
                } else if (recipient != null && recipient.equals(loggedInUsername)) {
                    System.out.printf("%-20s %-20s %-20f %-20s %-20s%n",
                            sender, recipient, amount, "Credit", transactionDate);
                }
            }
        } else {
            System.out.println("No transactions found for the given dates.");
        }
    }



    private static void recordTransaction(String type, double amount, String sender, String recipientAccountNumber) {
        try {
            // Fetch recipient's username using the account number
            String recipientUsername = recipientAccountNumber != null
                    ? getUsernameByAccountNumber(recipientAccountNumber)
                    : null;

            String insertQuery = "INSERT INTO transaction (type, amount, sender_username, recipient_username) VALUES (?, ?, ?, ?)";
            try (PreparedStatement insertStatement = conn.prepareStatement(insertQuery)) {
                insertStatement.setString(1, type);
                insertStatement.setDouble(2, amount);

                // Set sender and recipient based on transaction type
                if ("Debit".equalsIgnoreCase(type)) {
                    insertStatement.setString(3, sender); // Sender for Debit
                    insertStatement.setString(4, recipientUsername); // Recipient for Debit
                } else if ("Credit".equalsIgnoreCase(type)) {
                    insertStatement.setString(3, recipientUsername); // Recipient for Credit
                    insertStatement.setString(4, sender); // Sender for Credit
                }
                int rowsAffected = insertStatement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Transaction recorded successfully.");
                } else {
                    System.out.println("Failed to record transaction.");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static void updateBankBalance(String username, double amount, boolean printUpdateMessage) {
        try {
            String updateQuery = "UPDATE user SET bankbalance = bankbalance + ? WHERE username = ?";
            try (PreparedStatement updateStatement = conn.prepareStatement(updateQuery)) {
                updateStatement.setDouble(1, amount);
                updateStatement.setString(2, username);

                int rowsAffected = updateStatement.executeUpdate();
                if (rowsAffected > 0 && printUpdateMessage) {
                    System.out.println("Bank balance updated successfully.");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    private static void handleSQLException(SQLException ex) {
        System.out.println("An unexpected error occurred. Please try again later.");
        ex.printStackTrace();  // Consider logging the exception instead
    }
}