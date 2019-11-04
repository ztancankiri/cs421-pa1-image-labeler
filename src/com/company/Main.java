package com.company;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {
        String ip = args[0];
        int port = Integer.parseInt(args[1]);

        Scanner scanner = new Scanner(System.in);

        File file1 = new File("Image-1.jpg");
        File file2 = new File("Image-2.jpg");
        File file3 = new File("Image-3.jpg");

        Socket socket = new Socket(ip, port);

        InputStreamReader inputStreamReader =  new InputStreamReader(socket.getInputStream());
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);

        System.out.print("Username: ");
        String username = scanner.nextLine();
        printWriter.println("USER " + username);

        String userResponse = fixTurkishLetters(bufferedReader.readLine());
        System.out.println(userResponse);

        if (!userResponse.contains("OK") && userResponse.contains("INVALID")) {
            printWriter.println("EXIT");
            inputStreamReader.close();
            bufferedReader.close();
            printWriter.close();
            socket.close();
            scanner.close();
            return;
        }

        System.out.print("Password: ");
        String password = fixTurkishLetters(scanner.nextLine());
        printWriter.println("PASS " + password);

        String passResponse = bufferedReader.readLine();
        System.out.println(passResponse);

        if (!passResponse.contains("OK") && passResponse.contains("INVALID")) {
            printWriter.println("EXIT");
            inputStreamReader.close();
            bufferedReader.close();
            printWriter.close();
            socket.close();
            scanner.close();
            return;
        }

        boolean isActive = true;
        InputStream inputStream = socket.getInputStream();

        while (isActive) {
            if (file1.exists())
                file1.delete();

            if (file2.exists())
                file2.delete();

            if (file3.exists())
                file3.delete();

            printWriter.println("IGET");

            byte[] initBytes = inputStream.readNBytes(7);
            String code = new String(initBytes, 0, 4);

            if (code.equals("ISND")) {
                byte[] sizeArray = new byte[] { 0x00, initBytes[4], initBytes[5], initBytes[6] };
                int size = ByteBuffer.wrap(sizeArray).getInt();

                byte[] file = new byte[size];

                for (int i = 0; i < size; i++) {
                    file[i] = (byte) inputStream.read();
                }

                FileOutputStream fos = new FileOutputStream(file1);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                bos.write(file);
                bos.close();
                fos.close();
            }

            // -------------------------------------------------------------------------------------

            initBytes = inputStream.readNBytes(7);
            code = new String(initBytes, 0, 4);

            if (code.equals("ISND")) {
                byte[] sizeArray = new byte[] { 0x00, initBytes[4], initBytes[5], initBytes[6] };
                int size = ByteBuffer.wrap(sizeArray).getInt();

                byte[] file = new byte[size];

                for (int i = 0; i < size; i++) {
                    file[i] = (byte) inputStream.read();
                }

                FileOutputStream fos = new FileOutputStream(file2);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                bos.write(file);
                bos.close();
                fos.close();
            }

            // -------------------------------------------------------------------------------------

            initBytes = inputStream.readNBytes(7);
            code = new String(initBytes, 0, 4);

            if (code.equals("ISND")) {
                byte[] sizeArray = new byte[] { 0x00, initBytes[4], initBytes[5], initBytes[6] };
                int size = ByteBuffer.wrap(sizeArray).getInt();

                byte[] file = new byte[size];

                for (int i = 0; i < size; i++) {
                    file[i] = (byte) inputStream.read();
                }

                FileOutputStream fos = new FileOutputStream(file3);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                bos.write(file);
                bos.close();
                fos.close();
            }

            // -------------------------------------------------------------------------------------

            String labelResponse = null;
            do {
                System.out.print("Label-1: ");
                String label1 = fixTurkishLetters(scanner.nextLine());

                System.out.print("Label-2: ");
                String label2 = fixTurkishLetters(scanner.nextLine());

                System.out.print("Label-3: ");
                String label3 = fixTurkishLetters(scanner.nextLine());

                printWriter.println("ILBL " + label1 + "," + label2 + "," + label3);
                labelResponse = bufferedReader.readLine();
                System.out.println(labelResponse);
            } while (labelResponse != null && !labelResponse.contains("OK") && labelResponse.contains("INVALID"));
        }

        printWriter.println("EXIT");
        inputStreamReader.close();
        bufferedReader.close();
        printWriter.close();
        socket.close();
        scanner.close();

        // bilkentstu
        // cs421f2019
    }

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
}
