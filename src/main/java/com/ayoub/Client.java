package com.ayoub;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private Socket clientSocket;
    private BufferedReader in;
    private BufferedWriter out;
    private String clientName;

    public Client(Socket clientSocket, String clientName) {
        try {
            this.clientSocket = clientSocket;
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            this.clientName = clientName;
        } catch (IOException e) {
            System.err.println("Error initializing client: " + e.getMessage());
            closeEverything();
        }
    }

    public void readMessage() {
        Thread readerThread = new Thread(() -> {
            String msgFromChat;
            try {
                // Continuously read messages until the connection is closed
                while ((msgFromChat = in.readLine()) != null) {
                    System.out.println(msgFromChat);
                }
            } catch (IOException e) {
                System.err.println("Error reading message: " + e.getMessage());
                System.err.println("The server is probable shutdown ");

            } finally {
                closeEverything();
            }
        });
        readerThread.start();
    }

    public void sendMessage() {
        try {
            // Send the client name to the server
            out.write(clientName);
            out.newLine();
            out.flush();

            // Use Scanner to read messages from the console
            Scanner scanner = new Scanner(System.in);
            while (!clientSocket.isClosed()) {
                String msg = scanner.nextLine();
                // Send the message with the client name prefixed
                out.write(clientName + " : " + msg);
                out.newLine();
                out.flush();
            }
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
            System.err.println("Try to reconnect again");
        } finally {
            closeEverything();
        }
    }

    private void closeEverything() {
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing resources for " + clientName + ": " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("+ Enter your name : ");
        String clientName = scanner.nextLine();
        try {
            // Connect to the server on localhost at port 1234
            Socket clientSocket = new Socket("localhost", 1234);
            Client client = new Client(clientSocket, clientName);
            client.readMessage();
            client.sendMessage();
        } catch (IOException e) {
            System.err.println("Error connecting to the server: " + e.getMessage());
        }
    }
}
