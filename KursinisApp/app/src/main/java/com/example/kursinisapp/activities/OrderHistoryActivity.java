package com.example.kursinisapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
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

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryActivity extends AppCompatActivity {
    private RecyclerView rvOrders;
    private Long userId;
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_history_activity);

        View btnBack = findViewById(R.id.btnBackOrders);
        if (btnBack == null) btnBack = findViewById(R.id.btnBackOrders);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        rvOrders = findViewById(R.id.rvOrderHistory);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));

        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        userId = prefs.getLong("userId", -1);
        role = prefs.getString("role", "");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }

    private void loadOrders() {
        System.out.println("DEBUG: Kraunama istorija vartotojui: " + userId + " su role: " + role);

        Call<List<Order>> call;
        if ("CLIENT".equalsIgnoreCase(role)) {
            call = RetrofitClient.getApiService().getClientOrders(userId);
        } else {
            call = RetrofitClient.getApiService().getDriverOrders(userId);
        }

        call.enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Order> data = response.body();
                    System.out.println("DEBUG: Serveris grąžino užsakymų: " + data.size());

                    if (data.isEmpty()) {
                        Toast.makeText(OrderHistoryActivity.this, "Jūs neturite užsakymų", Toast.LENGTH_SHORT).show();
                    }
                    rvOrders.setAdapter(new OrderAdapter(data));
                } else {
                    System.out.println("DEBUG: Serverio klaida: " + response.code());
                    Toast.makeText(OrderHistoryActivity.this, "Klaida: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                System.out.println("DEBUG: Ryšio klaida: " + t.getMessage());
                t.printStackTrace();
                Toast.makeText(OrderHistoryActivity.this, "Nepavyko prisijungti", Toast.LENGTH_SHORT).show();
            }
        });
    }

    class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
        List<Order> orders;
        OrderAdapter(List<Order> orders) { this.orders = orders; }

        @Override
        public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
            return new OrderViewHolder(v);
        }

        @Override
        public void onBindViewHolder(OrderViewHolder holder, int position) {
            Order o = orders.get(position);
            holder.tvTitle.setText(o.restaurantName + " (Užsakymas #" + o.id + ")");
            String dateStr = (o.createdAt != null) ? o.createdAt : "";
            holder.tvDetails.setText("Statusas: " + o.status + " | " + o.totalPrice + "€\n" + dateStr);

            holder.btnChat.setOnClickListener(v -> {
                Intent intent = new Intent(OrderHistoryActivity.this, ChatActivity.class);
                intent.putExtra("orderId", o.id);
                startActivity(intent);
            });

            boolean isCompleted = "DONE".equals(o.status) || "COMPLETED".equals(o.status);

            if (isCompleted) {
                holder.btnReview.setVisibility(View.VISIBLE);
                holder.btnReview.setOnClickListener(v -> showReviewDialogForClient(o));
            } else {
                holder.btnReview.setVisibility(View.GONE);
            }
        }

        @Override public int getItemCount() { return orders.size(); }

        class OrderViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDetails;
            View btnChat, btnReview;

            OrderViewHolder(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tvOrderTitle);
                tvDetails = v.findViewById(R.id.tvOrderDetails);
                btnChat = v.findViewById(R.id.btnOpenChat);
                btnReview = v.findViewById(R.id.btnLeaveReview);
            }
        }
    }

    private void showReviewDialogForClient(Order order) {
        String[] options = {"Vertinti Restoraną", "Vertinti Vairuotoją"};

        // Jei vairuotojo nėra, rodome tik restoraną
        if (order.driverId == null || order.driverId == 0) {
            openReview(order.restaurantId, "RESTAURANT");
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Palikti atsiliepimą")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openReview(order.restaurantId, "RESTAURANT");
                    } else {
                        openReview(order.driverId, "DRIVER");
                    }
                })
                .show();
    }

    private void openReview(Long targetId, String type) {
        Intent intent = new Intent(this, ReviewActivity.class);
        intent.putExtra("targetId", targetId);
        intent.putExtra("targetType", type);
        startActivity(intent);
    }
}