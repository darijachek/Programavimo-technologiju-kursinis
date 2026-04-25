package com.example.kursinisapp.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog; // <--- Pridėtas importas

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

public class DriverHistoryActivity extends AppCompatActivity {
    private RecyclerView rv;
    private Long driverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_history_activity);
        findViewById(R.id.btnBackDriverHistory).setOnClickListener(v -> finish());

        rv = findViewById(R.id.rvDriverHistory);
        rv.setLayoutManager(new LinearLayoutManager(this));

        driverId = getSharedPreferences("UserSession", MODE_PRIVATE).getLong("userId", -1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistory();
    }

    private void loadHistory() {
        RetrofitClient.getApiService().getDriverOrders(driverId).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    rv.setAdapter(new HistoryAdapter(response.body()));
                }
            }
            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                Toast.makeText(DriverHistoryActivity.this, "Klaida kraunant duomenis", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showReviewDialogForDriver(Order order) {
        String[] options = {"Vertinti Klientą", "Vertinti Restoraną"};

        new AlertDialog.Builder(this)
                .setTitle("Palikti atsiliepimą")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openReview(order.userId, "CLIENT");
                    } else {
                        openReview(order.restaurantId, "RESTAURANT");
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

    class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        List<Order> list;
        HistoryAdapter(List<Order> list) { this.list = list; }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup p, int t) {
            View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_order, p, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder h, int p) {
            Order o = list.get(p);

            h.tvTitle.setText(o.restaurantName + " (Užsakymas #" + o.id + ")");
            String statusLt = o.status;
            h.tvDetails.setText("Statusas: " + statusLt + " | Kaina: " + o.totalPrice + "€");

            boolean isCompleted = "DONE".equals(o.status) || "COMPLETED".equals(o.status);

            if (isCompleted) {
                h.tvDetails.setTextColor(Color.parseColor("#4CAF50"));
                // Rodyti vertinimo mygtuką
                h.btnReview.setVisibility(View.VISIBLE);
                h.btnReview.setOnClickListener(v -> showReviewDialogForDriver(o));
            } else {
                h.tvDetails.setTextColor(Color.GRAY);
                // Slėpti vertinimo mygtuką
                h.btnReview.setVisibility(View.GONE);
            }

            h.btnChat.setOnClickListener(v -> {
                Intent intent = new Intent(DriverHistoryActivity.this, ChatActivity.class);
                intent.putExtra("orderId", o.id);
                intent.putExtra("isCompleted", isCompleted);
                startActivity(intent);
            });

            h.itemView.setOnClickListener(v -> {
                if ("TAKEN".equals(o.status) || "DELIVERING".equals(o.status)) {
                    // Jei užsakymas aktyvus -> atidarome ActiveDeliveryActivity
                    Intent intent = new Intent(DriverHistoryActivity.this, ActiveDeliveryActivity.class);
                    intent.putExtra("orderId", o.id);
                    startActivity(intent);
                } else {
                    Toast.makeText(DriverHistoryActivity.this, "Šis užsakymas jau įvykdytas", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDetails;
            View btnChat, btnReview;

            ViewHolder(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tvOrderTitle);
                tvDetails = v.findViewById(R.id.tvOrderDetails);
                btnChat = v.findViewById(R.id.btnOpenChat);
                btnReview = v.findViewById(R.id.btnLeaveReview);
            }
        }
    }
}