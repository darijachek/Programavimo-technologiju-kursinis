package com.example.kursinisapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kursinisapp.R;
import com.example.kursinisapp.models.Order;
import com.example.kursinisapp.remote.RetrofitClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AvailableOrdersActivity extends AppCompatActivity {
    RecyclerView rv;
    Long driverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.available_orders_activity);
        findViewById(R.id.btnBackOrders).setOnClickListener(v -> finish());
        setTitle("Laisvi užsakymai");

        driverId = getSharedPreferences("UserSession", MODE_PRIVATE).getLong("userId", -1);
        rv = findViewById(R.id.rvOrderHistory);
        rv.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAvailableOrders();
    }

    private void loadAvailableOrders() {
        RetrofitClient.getApiService().getAvailableOrders().enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    rv.setAdapter(new AvailableOrderAdapter(response.body()));
                }
            }
            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {}
        });
    }

    private void checkAndAssignOrder(Long orderId) {
        RetrofitClient.getApiService().getDriverOrders(driverId).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean hasActive = false;
                    for (Order o : response.body()) {
                        if ("TAKEN".equals(o.status) || "DELIVERING".equals(o.status)) {
                            hasActive = true;
                            break;
                        }
                    }

                    if (hasActive) {
                        Toast.makeText(AvailableOrdersActivity.this, "Pirma užbaikite dabartinį užsakymą!", Toast.LENGTH_LONG).show();
                    } else {
                        assignOrder(orderId);
                    }
                } else {
                    assignOrder(orderId);
                }
            }
            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                Toast.makeText(AvailableOrdersActivity.this, "Ryšio klaida tikrinant statusą", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void assignOrder(Long orderId) {
        Map<String, Object> body = new HashMap<>();
        body.put("orderId", orderId);
        body.put("driverId", driverId);

        RetrofitClient.getApiService().takeOrder(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AvailableOrdersActivity.this, "Užsakymas paimtas!", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(AvailableOrdersActivity.this, ActiveDeliveryActivity.class);
                    i.putExtra("orderId", orderId);
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(AvailableOrdersActivity.this, "Klaida: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(AvailableOrdersActivity.this, "Ryšio klaida", Toast.LENGTH_SHORT).show();
            }
        });
    }

    class AvailableOrderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        List<Order> list;
        AvailableOrderAdapter(List<Order> list) { this.list = list; }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup p, int t) {
            return new RecyclerView.ViewHolder(LayoutInflater.from(p.getContext()).inflate(android.R.layout.simple_list_item_2, p, false)) {};
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder h, int p) {
            Order o = list.get(p);
            ((TextView) h.itemView.findViewById(android.R.id.text1)).setText("Užsakymas #" + o.id);
            ((TextView) h.itemView.findViewById(android.R.id.text2)).setText(o.restaurantName + " -> " + o.totalPrice + "€");

            h.itemView.setOnClickListener(v -> {
                new AlertDialog.Builder(AvailableOrdersActivity.this)
                        .setTitle("Pasiimti užsakymą?")
                        .setMessage("Ar tikrai norite pristatyti šį užsakymą?")
                        .setPositiveButton("Taip", (dialog, which) -> checkAndAssignOrder(o.id)) // Pakeista į checkAndAssignOrder
                        .setNegativeButton("Atšaukti", null)
                        .show();
            });
        }
        @Override public int getItemCount() { return list.size(); }
    }
}