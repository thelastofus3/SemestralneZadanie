TCP Client-Server Application Documentation
This documentation provides an overview of a simple TCP client-server application written in Java. The application allows users to connect to a server, interact with a registration system, and perform various operations related to books in a library. The server connects to a MySQL database to store user information and book details.

TCPClient Class
The TCPClient class represents the client-side of the application. It establishes a connection with the server, reads prompts from the server, and sends user input back.

Methods
main(String[] args): The main entry point of the client application. Establishes a connection to the server and handles user input/output.
TCPServer3 Class
The TCPServer3 class represents the server-side of the application. It listens for incoming connections from clients, creates a new thread for each client, and handles client requests.

Methods
main(String[] args): The main entry point of the server application. Calls the connectToServer method.

connectToServer(): Establishes a server socket and listens for incoming connections. Creates a new thread for each connected client.

connectToDataBase(): Establishes a connection to the MySQL database.

handleClient(Socket clientSocket, Connection connection): Handles the communication with a connected client. Calls the startRegistrationMenu method.

startRegistrationMenu(BufferedReader reader, PrintWriter writer, Connection connection, Socket clientSocket): Displays a registration menu to the client and processes user choices.

handleLogin(BufferedReader reader, PrintWriter writer, Connection connection, Socket clientSocket): Handles the login process, checks user credentials, and redirects to appropriate menus for users and admins.

startBookMenuUser(String answer, BufferedReader reader, PrintWriter writer, Connection connection, Socket clientSocket, String loginCheck): Displays a menu for regular users to perform operations related to books.

addNewBookUser(BufferedReader reader, PrintWriter writer, Connection connection, String loginCheck, Socket clientSocket): Handles the process of adding a new book by a user.

showAllBook(PrintWriter writer, Connection connection): Retrieves and displays all books in the library.

startBookMenuAdmin(String answer, BufferedReader reader, PrintWriter writer, Connection connection, Socket clientSocket): Displays a menu for administrators to perform various operations.

showAllUsers(PrintWriter writer, Connection connection): Retrieves and displays all users in the database.

addNewBookAdmin(BufferedReader reader, PrintWriter writer, Connection connection, Socket clientSocket): Handles the process of adding a new book by an admin.

deleteBook(BufferedReader reader, PrintWriter writer, Connection connection): Handles the process of deleting a book from the library.

userLogsCheck(String loginCheck, String passwordCheck, Connection connection): Checks user credentials against the userlogs table.

adminLogsCheck(String loginCheck, String passwordCheck, Connection connection): Checks user credentials against the admin table.

handleRegistration(BufferedReader reader, PrintWriter writer, Connection connection): Handles the user registration process.

closeConnection(PrintWriter writer): Closes the connection to the client by sending an "exit" message.

Database
The application uses a MySQL database named "librarys" with tables "books," "userlogs," and "admin" to store information about books, user logs, and administrator credentials.

Usage
Compile the Java files using javac TCPClient.java TCPServer3.java.
Run the server using java TCPServer3.
Run the client using java TCPClient.
Follow the prompts to log in, register, and perform various operations on books based on the user's role.
Note: Ensure that a MySQL server is running, and the database and tables are set up as specified in the application code. Update the database connection details if necessary.

Dependencies
Java SE Development Kit (JDK)
MySQL Server
Disclaimer
This documentation assumes that the MySQL server is properly configured, and the required database and tables are set up. The application code may need adjustments based on specific MySQL configurations and security considerations.
