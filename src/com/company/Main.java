package com.company;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class Main {

    private static Scanner scanner = null;
    private static Socket socket = null;
    private static InputStream inputStream = null;
    private static InputStreamReader isr = null;
    private static BufferedReader reader = null;
    private static PrintWriter writer = null;

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

    private static void cleanFiles(File f1, File f2, File f3) {
        if (f1.exists())
            f1.delete();

        if (f2.exists())
            f2.delete();

        if (f3.exists())
            f3.delete();
    }

    private static void receiveFile(InputStream inputStream, File file) throws IOException {
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

    private static boolean sendUsername(String username) throws IOException {
        username = fixTurkishLetters(username);
        writer.println("USER " + username);

        String response = reader.readLine();

        return response.contains("OK") && !response.contains("INVALID");
    }

    private static boolean sendPassword(String password) throws IOException {
        password = fixTurkishLetters(password);
        writer.println("PASS " + password);

        String response = reader.readLine();

        return response.contains("OK") && !response.contains("INVALID");
    }

    private static boolean sendLabels(String label1, String label2, String label3) throws IOException {
        label1 = fixTurkishLetters(label1);
        label2 = fixTurkishLetters(label2);
        label3 = fixTurkishLetters(label3);
        writer.println("ILBL " + label1 + "," + label2 + "," + label3);

        String response = reader.readLine();

        return response.contains("OK") && !response.contains("INVALID");
    }

    private static void exit() throws IOException {
        writer.println("EXIT");
        isr.close();
        reader.close();
        writer.close();
        inputStream.close();
        socket.close();
        scanner.close();
        System.exit(0);
    }

    public static void main(String[] args) throws IOException {
        // Parse program run arguments to get IP Address and Port number of the server
        String ip = args[0];
        int port = Integer.parseInt(args[1]);

        // Initialization of global objects
        scanner = new Scanner(System.in);
        socket = new Socket(ip, port);
        inputStream = socket.getInputStream();
        isr =  new InputStreamReader(inputStream);
        reader = new BufferedReader(isr);
        writer = new PrintWriter(socket.getOutputStream(), true);

        // Image files store locations
        File file1 = new File("Image-1.jpg");
        File file2 = new File("Image-2.jpg");
        File file3 = new File("Image-3.jpg");

        // Get username from the user and send it to the server
        System.out.print("Username: ");
        String username = scanner.nextLine();
        boolean username_result = sendUsername(username);

        if (!username_result)
            exit();

        // Get password from the user and send it to the server
        System.out.print("Password: ");
        String password = scanner.nextLine();
        boolean password_result = sendPassword(password);

        if (!password_result)
            exit();

        // The infinite loop which carries the whole activity of the program
        while (true) {
            cleanFiles(file1, file2, file3);

            writer.println("IGET");

            receiveFile(inputStream, file1);
            receiveFile(inputStream, file2);
            receiveFile(inputStream, file3);

            boolean labels_result = false;
            do {
                System.out.print("Label-1: ");
                String label1 = scanner.nextLine();

                System.out.print("Label-2: ");
                String label2 = scanner.nextLine();

                System.out.print("Label-3: ");
                String label3 = scanner.nextLine();

                labels_result = sendLabels(label1, label2, label3);
            } while (labels_result);
        }

        // bilkentstu
        // cs421f2019
    }
}
