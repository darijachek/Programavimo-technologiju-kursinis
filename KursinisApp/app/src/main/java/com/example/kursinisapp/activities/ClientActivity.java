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
import com.example.kursinisapp.models.Restaurant;
import com.example.kursinisapp.remote.ApiService;
import com.example.kursinisapp.remote.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClientActivity extends AppCompatActivity {
    RecyclerView rv;
    ApiService api;

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
                startActivity(new Intent(ClientActivity.this, ProfileActivity.class));
            });
        }

        rv = findViewById(R.id.rvRestaurants);
        rv.setLayoutManager(new LinearLayoutManager(this));
        api = RetrofitClient.getApiService();

        View btnCart = findViewById(R.id.btnGoToCart);
        if (btnCart != null) {
            btnCart.setOnClickListener(v -> {
                startActivity(new Intent(ClientActivity.this, CartActivity.class));
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRestaurants();
    }

    private void loadRestaurants() {
        api.getRestaurants().enqueue(new Callback<List<Restaurant>>() {
            @Override
            public void onResponse(Call<List<Restaurant>> call, Response<List<Restaurant>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    rv.setAdapter(new RestaurantAdapter(response.body()));
                }
            }
            @Override
            public void onFailure(Call<List<Restaurant>> call, Throwable t) {
                Toast.makeText(ClientActivity.this, "Klaida", Toast.LENGTH_SHORT).show();
            }
        });
    }

    class RestaurantAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        List<Restaurant> list;
        RestaurantAdapter(List<Restaurant> list) { this.list = list; }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new RecyclerView.ViewHolder(v) {};
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            Restaurant r = list.get(position);
            ((TextView) holder.itemView.findViewById(android.R.id.text1)).setText(r.name);
            ((TextView) holder.itemView.findViewById(android.R.id.text2)).setText(r.address + " | " + r.phone);
            holder.itemView.setOnClickListener(v -> {
                Intent i = new Intent(ClientActivity.this, MenuActivity.class);
                i.putExtra("restaurantId", r.id);
                i.putExtra("restaurantName", r.name);
                startActivity(i);
            });
        }
        @Override
        public int getItemCount() { return list.size(); }
    }
}

