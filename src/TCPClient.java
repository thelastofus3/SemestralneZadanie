import java.io.*;
import java.net.*;
import java.util.Objects;
import java.util.Scanner;

public class TCPClient {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        Socket socket = new Socket("localhost", 12346);

        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        PrintWriter writer = new PrintWriter(outputStream, true);

        String prompt;

        while (true){
            prompt = reader.readLine();
            if (Objects.equals(prompt, "exit")) {
                break;
            }
            System.out.println(prompt);
            String password = scanner.nextLine();
            writer.println(password);
        }
    }
}
