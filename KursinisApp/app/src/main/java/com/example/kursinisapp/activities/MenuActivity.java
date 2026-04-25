package com.example.kursinisapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kursinisapp.R;
import com.example.kursinisapp.models.MenuItem;
import com.example.kursinisapp.remote.RetrofitClient;
import com.example.kursinisapp.utils.CartManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuActivity extends AppCompatActivity {
    Long restaurantId;
    RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_activity);

        View btnBack = findViewById(R.id.btnBackClient);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        View btnProfile = findViewById(R.id.btnProfile);
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                startActivity(new Intent(MenuActivity.this, ProfileActivity.class));
            });
        }

        restaurantId = getIntent().getLongExtra("restaurantId", -1);
        String name = getIntent().getStringExtra("restaurantName");
        setTitle(name + " Meniu");

        rv = findViewById(R.id.rvRestaurants);
        rv.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btnGoToCart).setOnClickListener(v -> {
            startActivity(new Intent(MenuActivity.this, CartActivity.class));
        });

        RetrofitClient.getApiService().getMenu(restaurantId).enqueue(new Callback<List<MenuItem>>() {
            @Override
            public void onResponse(Call<List<MenuItem>> call, Response<List<MenuItem>> response) {
                if (response.isSuccessful()) rv.setAdapter(new MenuAdapter(response.body()));
            }
            @Override
            public void onFailure(Call<List<MenuItem>> call, Throwable t) {}
        });
    }

    class MenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        List<MenuItem> items;
        MenuAdapter(List<MenuItem> items) { this.items = items; }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup p, int t) {
            View v = LayoutInflater.from(p.getContext()).inflate(android.R.layout.simple_list_item_2, p, false);
            return new RecyclerView.ViewHolder(v) {};
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder h, int p) {
            MenuItem m = items.get(p);
            ((TextView) h.itemView.findViewById(android.R.id.text1))
                    .setText(String.format("%s (%s) - %.2f€", m.title, m.category, m.basePrice));
            ((TextView) h.itemView.findViewById(android.R.id.text2)).setText(m.description);

            h.itemView.setOnClickListener(v -> {
                CartManager.getInstance().addItem(m, 1, restaurantId);
                Toast.makeText(MenuActivity.this, "Pridėta į krepšelį", Toast.LENGTH_SHORT).show();
            });
        }
        @Override
        public int getItemCount() { return items.size(); }
    }
}