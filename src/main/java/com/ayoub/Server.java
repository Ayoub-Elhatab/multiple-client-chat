package com.ayoub;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    // Volatile ensures changes to serverSocket are visible across threads
    private volatile ServerSocket serverSocket;
    // The port on which the server listens for incoming connections
    private static final int PORT = 1234;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {
        try {
            // Continue accepting client connections until the server socket is closed
            while (!serverSocket.isClosed()) {
                // Accept an incoming client connection (this call blocks until a connection is made)
                Socket clientSocket = serverSocket.accept();
                System.out.println("--------------------------");
                System.out.println("| A new client connected |");
                System.out.println("--------------------------");
                // Create and start a new thread to handle the connected client
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            if (!serverSocket.isClosed()) {
                System.err.println("Server error: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args){
        // Create a new ServerSocket bound to the specified port
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);

            System.out.println("-----------------------------------");
            System.out.print("| ");
            System.out.println("+ Server running on port : " + PORT + " |" );
            System.out.println("-----------------------------------");
            System.out.println(" Waiting for connections...");
            System.out.println();

            // Create a new Server instance and start the server
            new Server(serverSocket).startServer();

        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
}