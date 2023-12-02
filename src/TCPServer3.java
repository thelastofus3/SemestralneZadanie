import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;

public class TCPServer3 {
    public static void main(String[] args) throws IOException {
        connectToServer();
    }
    private static void connectToServer() throws IOException {
        ServerSocket serverSocket;
        serverSocket = new ServerSocket(12346);
        System.out.println("The server is waiting for connection...");
        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("The client is connected.");

            // Создаем новый поток для каждого клиента
            Thread clientThread = new Thread(() -> {
                try {
                    handleClient(clientSocket,connectToDataBase());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            clientThread.start();

        }
    }
    private static Connection connectToDataBase() throws SQLException {
        String userName = "root";
        String password = "thelastofus";
        String connectionUrl = "jdbc:mysql://localhost:3306/librarys";
        Connection connection = DriverManager.getConnection(connectionUrl, userName, password);
        System.out.println("Successful connection.");
        return connection;
    }
    private static void handleClient(Socket clientSocket, Connection connection) throws SQLException, IOException {
        InputStream inputStream = clientSocket.getInputStream();
        OutputStream outputStream = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        PrintWriter writer = new PrintWriter(outputStream, true);

        startRegistrationMenu(reader,writer,connection,clientSocket);
    }
    private static void startRegistrationMenu(BufferedReader reader, PrintWriter writer, Connection connection, Socket clientSocket) throws IOException, SQLException {
        while (true){
            writer.println("1.LogIn 2.Register 3.Exit");
            String answer = reader.readLine();
            System.out.println("Response received:" + answer);
            switch (answer){
                case "1":
                    handleLogin(reader,writer, connection, clientSocket);
                    break;
                case "2":
                    handleRegistration(reader,writer,connection);
                    break;
                case "3":
                    closeConnection(writer);
                    break;
                default:
                    System.out.println("incorrect entered data");

                    break;

            }
        }
    }
    private static void handleLogin(BufferedReader reader, PrintWriter writer, Connection connection,Socket clientSocket) throws SQLException, IOException {
        // Prompt for entering the username
        writer.println("Enter username:");
        String loginCheck = reader.readLine();
        System.out.println("Username received: " + loginCheck);

// Prompt for entering the password
        writer.println("Enter password:");
        String passwordCheck = reader.readLine();
        System.out.println("Password received: " + passwordCheck);

// Check user logs with entered credentials
        ResultSet resultSet1 = userLogsCheck(loginCheck, passwordCheck, connection);
        if (resultSet1.next()) {
            System.out.println("Record found in the userlogs table.");
            writer.println("1. Add new book 2. Show all books 3. Exit 4. Back");
            String answer = reader.readLine();
            System.out.println("Response received: " + answer);
            startBookMenuUser(answer, reader, writer, connection, clientSocket, loginCheck);
        } else {
            System.out.println("Record not found in the userlogs table.");

            // Check admin logs with entered credentials
            ResultSet resultSet2 = adminLogsCheck(loginCheck, passwordCheck, connection);
            if (resultSet2.next()) {
                System.out.println("Record found in the admin table.");
                writer.println("1. Add new book to user 2. Show all books 3. Delete book 4. Add new user 5. Show all users 6. Exit 7. Back");
                String answer = reader.readLine();
                System.out.println("Response received: " + answer);
                startBookMenuAdmin(answer, reader, writer, connection, clientSocket);
            } else {
                System.out.println("Record not found in the admin table.");
            }
        }

    }
    private static void startBookMenuUser(String answer,BufferedReader reader, PrintWriter writer, Connection connection, Socket clientSocket,String loginCheck ) throws SQLException, IOException {
        boolean exit = false;
        while (!exit){
            switch (answer){
                case "1":
                    addNewBookUser(reader,writer,connection, loginCheck, clientSocket);
                    writer.println("1. Add new book 2. Show all books 3. Exit 4. Back");
                    startBookMenuUser(answer = reader.readLine(),reader,writer,connection,clientSocket,loginCheck);
                    break;
                case "2":
                    showAllBook(writer,connection);
                    startBookMenuUser(answer = reader.readLine(),reader,writer,connection,clientSocket,loginCheck);
                    break;
                case "3":
                    closeConnection(writer);
                    break;
                case "4":
                    startRegistrationMenu(reader,writer,connection,clientSocket);
                    break;
                default:
                    System.out.println("incorrect entered data");
                    writer.println("1. Add new book 2. Show all books 3. Exit 4. Back");
                    startBookMenuUser(answer = reader.readLine(),reader,writer,connection,clientSocket,loginCheck);
            }
        }
    }
    private static void addNewBookUser(BufferedReader reader, PrintWriter writer,Connection connection,String loginCheck ,Socket clientSocket) throws SQLException, IOException {
        int year;
// Prompt for entering the book name
        writer.println("Enter the book title:");
        String bookName = reader.readLine();
        System.out.println("Title received: " + bookName);

// Prompt for entering the author
        writer.println("Enter the author of the book:");
        String bookAuthor = reader.readLine();
        System.out.println("Author received: " + bookAuthor);

// Prompt for entering the release year
        writer.println("Enter the release year of the book:");
        String bookYear = reader.readLine();
        try {
            year = Integer.parseInt(bookYear);
            System.out.println("You entered a number: " + year);
        } catch (NumberFormatException e) {
            // Display an error message
            System.out.println("You did not enter a number. Please enter a valid number.");
            writer.println("1. Add new book 2. Show all books 3. Exit 4. Back");
            String answer;
            startBookMenuUser(answer = reader.readLine(), reader, writer, connection, clientSocket, loginCheck);
        }

        System.out.println("Year received: " + bookYear);
        String addBookQuery = "INSERT INTO books (nameOfBook, Author, year, login) VALUES (?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(addBookQuery);
        preparedStatement.setString(1, bookName);
        preparedStatement.setString(2, bookAuthor);
        preparedStatement.setString(3, bookYear);
        preparedStatement.setString(4, loginCheck);
        int rowsAffected = preparedStatement.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Book successfully added to the database.");
        } else {
            System.out.println("Error adding the book.");
        }

    }
    private static void showAllBook(PrintWriter writer,Connection connection) throws SQLException {
        String bookQuery = "SELECT * FROM books";
        PreparedStatement preparedStatement2 = connection.prepareStatement(bookQuery);
        ResultSet resultSet = preparedStatement2.executeQuery();
        StringBuilder booksData = new StringBuilder();
        while (resultSet.next()) {
            String column1 = resultSet.getString("nameOfBook");
            String column2 = resultSet.getString("Autuor");
            String column3 = resultSet.getString("year");

            String bookInfo = column1 + " " + column2 + " " + column3;
            booksData.append(bookInfo).append("|");
        }
        booksData.insert(0,"[").setCharAt(booksData.length()-1,']');
        writer.println(booksData.toString().trim());
    }
    private static void startBookMenuAdmin(String answer,BufferedReader reader, PrintWriter writer, Connection connection, Socket clientSocket ) throws SQLException, IOException {
        boolean exit = false;
        while (!exit){
            switch (answer){
                case "1":
                    addNewBookAdmin(reader,writer,connection,clientSocket);
                    writer.println("1. Add new book to user 2. Show all books 3. Delete book 4. Add new user 5. Show all users 6. Exit 7. Back");
                    startBookMenuAdmin(answer = reader.readLine(),reader,writer,connection,clientSocket);
                    break;
                case "2":
                    showAllBook(writer,connection);
                    startBookMenuAdmin(answer = reader.readLine(),reader,writer,connection,clientSocket);
                    break;
                case "3":
                    deleteBook(reader,writer,connection);
                    writer.println("1. Add new book to user 2. Show all books 3. Delete book 4. Add new user 5. Show all users 6. Exit 7. Back");
                    startBookMenuAdmin(answer = reader.readLine(),reader,writer,connection,clientSocket);
                    break;
                case "4":
                    handleRegistration(reader, writer, connection);
                    writer.println("1. Add new book to user 2. Show all books 3. Delete book 4. Add new user 5. Show all users 6. Exit 7. Back");
                    startBookMenuAdmin(answer = reader.readLine(),reader,writer,connection,clientSocket);
                    break;
                case "5":
                    showAllUsers(writer,connection);
                    startBookMenuAdmin(answer = reader.readLine(),reader,writer,connection,clientSocket);
                case "6":
                    closeConnection(writer);
                    break;
                case "7":
                    startRegistrationMenu(reader,writer,connection,clientSocket);
                    break;
                default:
                    System.out.println("incorrect entered data");
                    writer.println("1. Add new book to user 2. Show all books 3. Delete book 4. Add new user 5. Show all users 6. Exit 7. Back");
                    startBookMenuAdmin(answer = reader.readLine(),reader,writer,connection,clientSocket);
            }
        }
    }
    private static void showAllUsers(PrintWriter writer,Connection connection) throws SQLException {
        String bookQuery = "SELECT * FROM userlogs";
        PreparedStatement preparedStatement2 = connection.prepareStatement(bookQuery);
        ResultSet resultSet = preparedStatement2.executeQuery();
        StringBuilder booksData = new StringBuilder();
        while (resultSet.next()) {
            String column = resultSet.getString("login");

            String bookInfo = column + " ";
            booksData.append(bookInfo).append(" |");
        }
        booksData.insert(0,"[").setCharAt(booksData.length()-1,']');
        writer.println(booksData.toString().trim());
    }
    private static void addNewBookAdmin(BufferedReader reader,PrintWriter writer,Connection connection,Socket clientSocket) throws IOException, SQLException {
        int year;
// Prompt for entering the book name
        writer.println("Enter the book title:");
        String bookName = reader.readLine();
        System.out.println("Title received: " + bookName);

// Prompt for entering the author
        writer.println("Enter the author of the book:");
        String bookAuthor = reader.readLine();
        System.out.println("Author received: " + bookAuthor);

// Prompt for entering the release year
        writer.println("Enter the release year of the book:");
        String bookYear = reader.readLine();
        System.out.println("Year received: " + bookYear);
        try {
            year = Integer.parseInt(bookYear);
            System.out.println("You entered a number: " + year);
        } catch (NumberFormatException e) {
            // Display an error message
            System.out.println("You did not enter a number. Please enter a valid number.");
            writer.println("1. Add new book to user 2. Show all books 3. Delete book 4. Add new user 5. Show all users 6. Exit 7. Back");
            String answer;
            startBookMenuAdmin(answer = reader.readLine(), reader, writer, connection, clientSocket);
        }

// Prompt for entering the username
        writer.println("Enter the username:");
        String login = reader.readLine();
        System.out.println("Username received: " + login);

        String addBookQuery = "INSERT INTO books (nameOfBook, Author, year, login) VALUES (?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(addBookQuery);
        preparedStatement.setString(1, bookName);
        preparedStatement.setString(2, bookAuthor);
        preparedStatement.setString(3, bookYear);
        preparedStatement.setString(4, login);
        int rowsAffected = preparedStatement.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Book successfully added to the database.");
        } else {
            System.out.println("Error adding the book...");
        }

    }
    private static void deleteBook(BufferedReader reader,PrintWriter writer,Connection connection) throws SQLException, IOException {
// Prompt for entering the book name
        writer.println("Enter the book title:");
        String bookName = reader.readLine();
        System.out.println("Title received: " + bookName);

// SQL query to delete the book by its name
        String deleteBookQuery = "DELETE FROM books WHERE nameOfBook = ?";
        PreparedStatement deleteStatement = connection.prepareStatement(deleteBookQuery);
        deleteStatement.setString(1, bookName);
        int rowsAffected = deleteStatement.executeUpdate();

        if (rowsAffected > 0) {
            System.out.println("Book successfully deleted from the database.");
        } else {
            System.out.println("Book with the specified title not found in the database.");
        }

    }
    private static ResultSet userLogsCheck(String loginCheck, String passwordCheck, Connection connection) throws SQLException {
        String checkUserQuery = "SELECT * FROM userlogs WHERE login = ? AND password = ?";
        PreparedStatement preparedStatement1 = connection.prepareStatement(checkUserQuery);
        preparedStatement1.setString(1, loginCheck);
        preparedStatement1.setString(2, passwordCheck);
        ResultSet resultSet1 = preparedStatement1.executeQuery();
        return resultSet1;
    }
    private static ResultSet adminLogsCheck(String loginCheck,String passwordCheck,Connection connection) throws SQLException {
        String checkAdminQuery = "SELECT * FROM admin WHERE login = ? AND password = ?";
        PreparedStatement preparedStatement2 = connection.prepareStatement(checkAdminQuery);
        preparedStatement2.setString(1, loginCheck);
        preparedStatement2.setString(2, passwordCheck);
        ResultSet resultSet2 = preparedStatement2.executeQuery();
        return resultSet2;
    }
    private static void handleRegistration(BufferedReader reader,PrintWriter writer,Connection connection) throws IOException, SQLException {
        // Prompt for entering the username
        writer.println("Enter username:");
        String addLogin = reader.readLine();
        if (addLogin.isEmpty()) {
            System.out.println("Username cannot be empty.");
            return;
        }

// Prompt for entering the password
        writer.println("Enter password:");
        String addPassword = reader.readLine();
        if (addPassword.isEmpty()) {
            System.out.println("Password cannot be empty.");
            return;
        }

// Prompt for entering the first name
        writer.println("Enter first name:");
        String addName = reader.readLine();
        if (addName.isEmpty()) {
            System.out.println("First name cannot be empty.");
            return;
        }

// Prompt for entering the last name
        writer.println("Enter last name:");
        String addSurname = reader.readLine();
        if (addSurname.isEmpty()) {
            System.out.println("Last name cannot be empty.");
            return;
        }

// Prompt for entering the email
        writer.println("Enter email:");
        String addEmail = reader.readLine();
        if (addEmail.isEmpty()) {
            System.out.println("Email cannot be empty.");
            return;
        }

// Prompt for entering the gender
        writer.println("Enter gender:");
        String addGender = reader.readLine();
        if (addGender.isEmpty()) {
            System.out.println("Gender cannot be empty.");
            return;
        }

// SQL query to insert user information into the userlogs table
        String addUserQuery = "INSERT INTO userlogs (login, password, name, surname, email, gender) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(addUserQuery);
        preparedStatement.setString(1, addLogin);
        preparedStatement.setString(2, addPassword);
        preparedStatement.setString(3, addName);
        preparedStatement.setString(4, addSurname);
        preparedStatement.setString(5, addEmail);
        preparedStatement.setString(6, addGender);
        int rowsAffected = preparedStatement.executeUpdate();

        if (rowsAffected > 0) {
            System.out.println("User successfully added to the database.");
        } else {
            System.out.println("Error adding the user.");
        }
    }
    private static void closeConnection(PrintWriter writer) {
        writer.println("exit");
    }
}