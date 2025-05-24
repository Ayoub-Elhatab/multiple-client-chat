package com.ayoub;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ClientHandler implements Runnable{
    // A thread-safe list of all active client handlers
    public static final List<ClientHandler> clientHandlers = Collections.synchronizedList(new ArrayList<>());

    private Socket clientSocket;
    private BufferedReader in;
    private BufferedWriter out;
    private String clientName;

    public ClientHandler(Socket clientSocket) {
        try {
            this.clientSocket = clientSocket;
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            // Read the client's name
            this.clientName = in.readLine();
            clientHandlers.add(this);
            broadcastMessage("+ SERVER : " + clientName + " has entered the chat");
        } catch (IOException e) {
            System.err.println("Error initializing client handler: " + e.getMessage());
            closeEverything();
        }
    }

    @Override
    public void run() {
        String clientMessage;
        try {
            while ((clientMessage = in.readLine()) != null) {
                broadcastMessage(clientMessage);
            }
        } catch (IOException e) {
            System.err.println("Error reading message from " + clientName + " : " + e.getMessage());
            System.err.println("He probably left the chat");
        }finally {
            removeClient();
            closeEverything();
        }
    }

    public void broadcastMessage(String msg) {
        synchronized (clientHandlers) {
            Iterator<ClientHandler> iterator = clientHandlers.iterator();
            while (iterator.hasNext()) {
                ClientHandler handler = iterator.next();
                // Skip sending the message to the sender
                if (!handler.clientName.equals(this.clientName)) {
                    try {
                        handler.out.write(msg);
                        handler.out.newLine();
                        handler.out.flush();
                    } catch (IOException e) {
                        System.err.println("Error sending message to " + handler.clientName + ": " + e.getMessage());
                        handler.closeEverything();
                        iterator.remove();
                    }
                }
            }
        }
    }

    public void removeClient(){
        clientHandlers.remove(this);
        broadcastMessage(" + SERVER : " + clientName + " has left the chat");
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
}
