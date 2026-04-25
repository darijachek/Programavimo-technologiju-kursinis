package com.example.kursinisbackend.socket;

import com.example.kursinisbackend.entities.Message;
import com.example.kursinisbackend.entities.Order;
import com.example.kursinisbackend.entities.User;
import com.example.kursinisbackend.repos.MessageRepository;
import com.example.kursinisbackend.repos.OrderRepository;
import com.example.kursinisbackend.repos.UserRepository;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class ChatServer implements CommandLineRunner {

    @Autowired private MessageRepository messageRepo;
    @Autowired private OrderRepository orderRepo;
    @Autowired private UserRepository userRepo;

    private static final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void run(String... args) {
        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(9090);
                System.out.println("CHAT SERVERIS STARTAVO ANT PORTO 9090");

                while (true) {
                    Socket socket = serverSocket.accept();
                    ClientHandler client = new ClientHandler(socket);
                    clients.add(client);
                    new Thread(client).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private Long userId;
        private Long orderId;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    handleMessage(inputLine);
                }
            } catch (Exception e) {
                System.out.println("Client disconnected");
            } finally {
                clients.remove(this);
                try { socket.close(); } catch (Exception e) {}
            }
        }

        private void handleMessage(String jsonStr) {
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(jsonStr, JsonObject.class);
            String type = json.get("type").getAsString();

            if (type.equals("JOIN")) {
                this.userId = json.get("userId").getAsLong();
                this.orderId = json.get("orderId").getAsLong();
                System.out.println("User " + userId + " joined chat for order " + orderId);

            } else if (type.equals("SEND")) {
                String text = json.get("text").getAsString();
                saveAndBroadcast(text);
            }
        }

        private void saveAndBroadcast(String text) {
            try {
                Order order = orderRepo.findById(this.orderId).orElseThrow();
                User sender = userRepo.findById(this.userId).orElseThrow();

                Message msg = new Message();
                msg.setOrder(order);
                msg.setSender(sender);
                msg.setText(text);
                msg.setCreatedAt(LocalDateTime.now());
                messageRepo.save(msg);

                JsonObject response = new JsonObject();
                response.addProperty("sender", sender.getUsername());
                response.addProperty("role", sender.getRole().toString());
                response.addProperty("text", text);
                response.addProperty("time", msg.getCreatedAt().toString());

                String jsonResponse = new Gson().toJson(response);

                synchronized (clients) {
                    for (ClientHandler client : clients) {
                        if (client.orderId != null && client.orderId.equals(this.orderId)) {
                            client.out.println(jsonResponse);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
