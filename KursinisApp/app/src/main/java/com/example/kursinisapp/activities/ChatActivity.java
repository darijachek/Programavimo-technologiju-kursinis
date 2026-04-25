package com.example.kursinisapp.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kursinisapp.R;
import com.example.kursinisapp.models.Message;
import com.example.kursinisapp.remote.RetrofitClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {
    TextView tvMessages;
    EditText etMessage;
    Long orderId;
    Long currentUserId;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity);
        findViewById(R.id.btnBackChat).setOnClickListener(v -> finish());

        orderId = getIntent().getLongExtra("orderId", -1);
        currentUserId = getSharedPreferences("UserSession", MODE_PRIVATE).getLong("userId", -1);

        tvMessages = findViewById(R.id.tvMessages);
        etMessage = findViewById(R.id.etMessage);
        findViewById(R.id.btnSend).setOnClickListener(v -> sendMessage());

        loadHistory();

        connectToSocket();
    }

    private void connectToSocket() {
        new Thread(() -> {
            try {
                socket = new Socket("10.21.17.165", 9090);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                isConnected = true;

                JsonObject joinJson = new JsonObject();
                joinJson.addProperty("type", "JOIN");
                joinJson.addProperty("userId", currentUserId);
                joinJson.addProperty("orderId", orderId);
                out.println(joinJson.toString());

                String incomingLine;
                while ((incomingLine = in.readLine()) != null) {
                    final String msgJson = incomingLine;

                    runOnUiThread(() -> displayNewMessage(msgJson));
                }

            } catch (Exception e) {
                e.printStackTrace();
                isConnected = false;
            }
        }).start();
    }

    private void displayNewMessage(String jsonStr) {
        try {
            JsonObject json = new Gson().fromJson(jsonStr, JsonObject.class);
            String sender = json.get("sender").getAsString();
            String role = json.get("role").getAsString();
            String text = json.get("text").getAsString();
            String time = json.get("time").getAsString();

            if(time.contains("T")) time = time.replace("T", " ").substring(0, 16);

            String formattedMsg = "[" + role + "] " + sender + " (" + time + "):\n" + text + "\n\n";
            tvMessages.append(formattedMsg);

            final ScrollView scroll = findViewById(R.id.scrollChat);
            if(scroll != null) scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String text = etMessage.getText().toString();
        if (text.isEmpty()) return;

        new Thread(() -> {
            if (out != null && isConnected) {
                JsonObject json = new JsonObject();
                json.addProperty("type", "SEND");
                json.addProperty("text", text);
                out.println(json.toString());

                runOnUiThread(() -> etMessage.setText(""));
            } else {
                runOnUiThread(() -> Toast.makeText(this, "Nėra ryšio su serveriu", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void loadHistory() {
        RetrofitClient.getApiService().getChatMessages(orderId).enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tvMessages.setText("");
                    for (Message m : response.body()) {
                        JsonObject json = new JsonObject();
                        json.addProperty("sender", m.sender != null ? m.sender.username : "Unknown");
                        json.addProperty("role", m.sender != null ? m.sender.role : "");
                        json.addProperty("text", m.text);
                        json.addProperty("time", m.createdAt != null ? m.createdAt : "");

                        displayNewMessage(json.toString());
                    }
                }
            }
            @Override public void onFailure(Call<List<Message>> call, Throwable t) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new Thread(() -> {
            try {
                if (socket != null) socket.close();
            } catch (Exception e) {}
        }).start();
    }
}