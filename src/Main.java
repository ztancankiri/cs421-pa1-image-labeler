import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class Main {

    // Global Objects
    private static Scanner scanner = null;
    private static Socket socket = null;
    private static InputStream inputStream = null;
    private static InputStreamReader isr = null;
    private static BufferedReader reader = null;
    private static PrintWriter writer = null;

    /**
     * This method gets rid of the turkish letters in a string by replacing them the latin equivalents.
     * @param text The string.
     * @return The fixed version of the given string.
     */
    private static String fixTurkishLetters(String text) {
        String result = text;

        result.replace("İ", "I");
        result.replace("Ğ", "G");
        result.replace("Ü", "U");
        result.replace("Ş", "S");
        result.replace("Ö", "O");
        result.replace("Ç", "C");

        result.replace("ı", "i");
        result.replace("ğ", "g");
        result.replace("ü", "u");
        result.replace("ş", "s");
        result.replace("ö", "o");
        result.replace("ç", "c");

        return result;
    }

    /**
     * This method removes the temporary storage files of the images.
     * @param f1 Image file 1.
     * @param f2 Image file 2.
     * @param f3 Image file 3.
     */
    private static void cleanFiles(File f1, File f2, File f3) {
        if (f1.exists())
            f1.delete();

        if (f2.exists())
            f2.delete();

        if (f3.exists())
            f3.delete();
    }

    /**
     * This method receives an image file from the server and saves it to a file.
     * @param inputStream The input stream of the socket communication.
     * @param file The destination file to save the image.
     */
    private static void receiveFile(InputStream inputStream, File file) {
        try {
            byte[] initBytes = inputStream.readNBytes(7);
            String code = new String(initBytes, 0, 4);

            if (code.equals("ISND")) {
                byte[] sizeArray = new byte[] { 0x00, initBytes[4], initBytes[5], initBytes[6] };
                int size = ByteBuffer.wrap(sizeArray).getInt();

                byte[] fileBytes = new byte[size];

                for (int i = 0; i < size; i++) {
                    fileBytes[i] = (byte) inputStream.read();
                }

                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                bos.write(fileBytes);
                bos.close();
                fos.close();
            }
            else {
                System.out.println("No File!");
            }
        }
        catch (IOException e) {
            System.out.println("Exception in receiveFile: " + e.getMessage());
        }
    }

    /**
     * This method sends the username to the server by using "USER" command.
     * @param username The username.
     * @return Returns true if the username is correct, otherwise returns false.
     */
    private static boolean sendUsername(String username) {
        username = fixTurkishLetters(username);
        writer.println("USER " + username);

        try {
            String response = reader.readLine();
            System.out.println(response);
            return response.contains("OK") && !response.contains("INVALID");
        }
        catch (IOException e) {
            System.out.println("Exception in sendUsername: " + e.getMessage());
        }
        return false;
    }

    /**
     * This method sends the password to the server by using "PASS" command.
     * @param password The password.
     * @return Returns true if the password is correct, otherwise returns false.
     */
    private static boolean sendPassword(String password) {
        password = fixTurkishLetters(password);
        writer.println("PASS " + password);

        try {
            String response = reader.readLine();
            System.out.println(response);
            return response.contains("OK") && !response.contains("INVALID");
        }
        catch (IOException e) {
            System.out.println("Exception in sendLabels: " + e.getMessage());
        }
        return false;
    }

    /**
     * This method sends the image labels to the server by using "ILBL" command.
     * @param label1 The label of the first image.
     * @param label2 The label of the second image.
     * @param label3 The label of the third image.
     * @return Returns true if all labels are correct, otherwise returns false.
     */
    private static boolean sendLabels(String label1, String label2, String label3) {
        label1 = fixTurkishLetters(label1);
        label2 = fixTurkishLetters(label2);
        label3 = fixTurkishLetters(label3);
        writer.println("ILBL " + label1 + "," + label2 + "," + label3);

        try {
            String response = reader.readLine();
            System.out.println(response);
            return response.contains("OK") && !response.contains("INVALID");
        }
        catch (IOException e) {
            System.out.println("Exception in sendLabels: " + e.getMessage());
        }
        return false;
    }

    /**
     * This method sends "EXIT" command to the server then closes application properly.
     */
    private static void exit() {
        try {
            writer.println("EXIT");
            isr.close();
            reader.close();
            writer.close();
            inputStream.close();
            socket.close();
        }
        catch (IOException e) {
            System.out.println("Exception in exit: " + e.getMessage());
        }

        scanner.close();
        System.exit(0);
    }

    /**
     * Main method to run the client program.
     * @param args Running arguments
     */
    public static void main(String[] args) {
        // Parse program running arguments to get IP address and port number of the server
        String ip = args[0];
        int port = Integer.parseInt(args[1]);

        // Initialization of global objects
        scanner = new Scanner(System.in);
        try {
            socket = new Socket(ip, port);
            inputStream = socket.getInputStream();
            isr = new InputStreamReader(inputStream);
            reader = new BufferedReader(isr);
            writer = new PrintWriter(socket.getOutputStream(), true);
        }
        catch (IOException e) {
            System.out.println("Exception in main: " + e.getMessage());
        }

        // Image files store locations
        File file1 = new File("Image-1.jpg");
        File file2 = new File("Image-2.jpg");
        File file3 = new File("Image-3.jpg");

        // Get username from the user and send it to the server
        System.out.print("Username: ");
        String username = scanner.nextLine();
        boolean username_result = sendUsername(username);

        // If username is wrong, exit!
        if (!username_result)
            exit();

        // Get password from the user and send it to the server
        System.out.print("Password: ");
        String password = scanner.nextLine();
        boolean password_result = sendPassword(password);

        // If password is wrong, exit!
        if (!password_result)
            exit();

        // The infinite loop which carries the whole activity of the program
        while (true) {
            cleanFiles(file1, file2, file3);

            // Ask for images
            writer.println("IGET");

            receiveFile(inputStream, file1);
            receiveFile(inputStream, file2);
            receiveFile(inputStream, file3);

            // Get labels from user and send them to the server until getting them correct
            boolean labels_result = false;
            do {
                System.out.print("Label-1: ");
                String label1 = scanner.nextLine();

                // Close the application properly, if the user types exit
                if (label1.equals("exit"))
                    exit();

                System.out.print("Label-2: ");
                String label2 = scanner.nextLine();

                // Close the application properly, if the user types exit
                if (label2.equals("exit"))
                    exit();

                System.out.print("Label-3: ");
                String label3 = scanner.nextLine();

                // Close the application properly, if the user types exit
                if (label3.equals("exit"))
                    exit();

                labels_result = sendLabels(label1, label2, label3);
            } while (!labels_result);

            System.out.println("All correct. Asking for next three images...");
        }

        /*
            The required username and password to connect the server:

            Username: bilkentstu
            Password: cs421f2019
        */
    }
}
