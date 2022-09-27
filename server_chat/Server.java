import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.InputStreamReader;


public class Server implements Runnable {

    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool; // change

    public Server() {
        connections = new ArrayList<>();
        done = false;

    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(9999); // port
            pool = Executors.newCachedThreadPool();
            while (!done) {
                Socket client = server.accept(); // host accepts client
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler); // new client added
                pool.execute(handler);
            }
        } catch (IOException e) {
            shutdown();
        }

    }

    public void broadcast(String message) {
        
        done = true;
        for (ConnectionHandler ch: connections) {
            ch.sendMessage(message);
        }
          
    
    }

    public void shutdown() {
        try {
            if (server.isClosed() == false) {
                server.close(); // close server
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ConnectionHandler implements Runnable {

        private Socket client; 
        private BufferedReader in; private PrintWriter out;
        private String nickname;


        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(),true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                out.println("Enter a username: ");
                nickname = in.readLine();

                while (nickname.isBlank()) {
                    out.println("Enter a username again: ");
                    nickname = in.readLine();
                }

                System.out.println("Client: "+nickname+" connected!");
                broadcast(nickname+" joined the chat!");

                String message;
                while ((message = in.readLine()) != null) {
                    if ((message.startsWith("/quit"))) {
                        broadcast(nickname+" left the chat!");
                        shutdown();
                    } else {
                        broadcast(nickname+": "+message); // message sent
                    }
                }
            } catch (IOException e) {
                //todo
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public void shutdown() {
            try {
                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {
                shutdown();
            }
        }

        
    }

    public static void main(String[] args) {
            Server server = new Server();
            server.run();
    }
}