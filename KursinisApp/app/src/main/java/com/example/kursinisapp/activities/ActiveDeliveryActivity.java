package com.example.kursinisapp.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kursinisapp.R;
import com.example.kursinisapp.models.Order;
import com.example.kursinisapp.models.User;
import com.example.kursinisapp.remote.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActiveDeliveryActivity extends AppCompatActivity {
    Long orderId;
    Long clientId;

    Button btnAction;
    TextView tvTitle, tvStatus, tvRestInfo, tvClientInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deliver_activity);

        findViewById(R.id.btnBackActiveDelivery).setOnClickListener(v -> finish());

        orderId = getIntent().getLongExtra("orderId", -1);

        tvTitle = findViewById(R.id.tvOrderTitle);
        tvStatus = findViewById(R.id.tvOrderStatus);
        tvRestInfo = findViewById(R.id.tvRestInfo);
        tvClientInfo = findViewById(R.id.tvClientInfo);

        btnAction = findViewById(R.id.btnCompleteDelivery);

        tvTitle.setText("Užsakymas #" + orderId);

        findViewById(R.id.btnChatWithClient).setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("orderId", orderId);
            startActivity(intent);
        });

        loadOrderDetails();
    }

    private void loadOrderDetails() {
        RetrofitClient.getApiService().getOrder(orderId).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Order o = response.body();

                    clientId = o.userId;

                    tvRestInfo.setText("PAIMTI IŠ:\n" + o.restaurantName);

                    if ("TAKEN".equals(o.status)) {
                        tvStatus.setText("Būsena: Vykstama į restoraną");
                        btnAction.setText("Paėmiau maistą (Esu restorane)");
                        btnAction.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#9C27B0")));
                        btnAction.setOnClickListener(v -> pickupOrder());

                    } else if ("DELIVERING".equals(o.status)) {
                        tvStatus.setText("Būsena: Vežama pas klientą");
                        btnAction.setText("Pristatyta (Užbaigti)");
                        btnAction.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                        btnAction.setOnClickListener(v -> completeOrder());
                    }

                    loadClientAddress(clientId);
                }
            }
            @Override
            public void onFailure(Call<Order> call, Throwable t) {}
        });
    }

    private void pickupOrder() {
        Map<String, Object> body = new HashMap<>();
        body.put("orderId", orderId);

        RetrofitClient.getApiService().pickupOrder(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ActiveDeliveryActivity.this, "Maistas paimtas! Vežkite klientui.", Toast.LENGTH_SHORT).show();
                    loadOrderDetails();
                } else {
                    Toast.makeText(ActiveDeliveryActivity.this, "Klaida: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ActiveDeliveryActivity.this, "Ryšio klaida", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadClientAddress(Long userId) {
        if (userId == null) return;

        RetrofitClient.getApiService().getUser(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String address = response.body().address;
                    String username = response.body().username;

                    tvClientInfo.setText("PRISTATYTI Į:\n" + address + "\n(Klientas: " + username + ")");

                    tvClientInfo.setOnClickListener(v -> {
                        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        if (mapIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(mapIntent);
                        }
                    });
                }
            }
            @Override public void onFailure(Call<User> call, Throwable t) {}
        });
    }

    private void completeOrder() {
        Long driverId = getSharedPreferences("UserSession", MODE_PRIVATE).getLong("userId", -1);

        Map<String, Long> body = new HashMap<>();
        body.put("driverId", driverId);
        body.put("orderId", orderId);

        RetrofitClient.getApiService().completeOrder(orderId, body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ActiveDeliveryActivity.this, "Pristatymas baigtas!", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(ActiveDeliveryActivity.this, ReviewActivity.class);
                    intent.putExtra("targetId", clientId);
                    intent.putExtra("targetType", "CLIENT");
                    startActivity(intent);
                    finish();
                } else {
                    String errorMsg = "Klaida serveryje: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += "\n" + response.errorBody().string();
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                    Toast.makeText(ActiveDeliveryActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ActiveDeliveryActivity.this, "Ryšio klaida: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}