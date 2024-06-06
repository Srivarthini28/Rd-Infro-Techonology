Server.java

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    private static Set<ClientHandler> clientHandlers = new HashSet<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started. Waiting for clients to connect...");
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandlers.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void broadcastMessage(String message, ClientHandler excludeUser) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler != excludeUser) {
                clientHandler.sendMessage(message);
            }
        }
    }

    static void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received: " + message);
                    Server.broadcastMessage(message, this);

                    // Send acknowledgment back to the client
                    sendMessage("Server received: " + message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Server.removeClient(this);
            }
        }

        void sendMessage(String message) {
            out.println(message);
        }
    }
}
