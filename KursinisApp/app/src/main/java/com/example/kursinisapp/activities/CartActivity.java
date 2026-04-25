package com.example.kursinisapp.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kursinisapp.R;
import com.example.kursinisapp.remote.RetrofitClient;
import com.example.kursinisapp.utils.CartManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity {

    RecyclerView rv;
    TextView tvTotal;
    CartAdapter adapter;
    List<CartManager.CartItem> cartItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart_activity);
        setTitle("Krepšelis");

        View btnBack = findViewById(R.id.btnBackCart);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tvTotal = findViewById(R.id.tvTotalPrice);

        rv = findViewById(R.id.rvCartItems);
        rv.setLayoutManager(new LinearLayoutManager(this));

        cartItems = CartManager.getInstance().getItems();
        adapter = new CartAdapter(cartItems);
        rv.setAdapter(adapter);

        updateTotal();

        findViewById(R.id.btnSubmitOrder).setOnClickListener(v -> submitOrder());
    }

    private void updateTotal() {
        double total = 0;
        for (CartManager.CartItem item : cartItems) {
            total += item.price * item.quantity;
        }
        if (tvTotal != null) {
            tvTotal.setText(String.format("Viso: %.2f€", total));
        }
    }

    class CartAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        List<CartManager.CartItem> list;

        CartAdapter(List<CartManager.CartItem> list) {
            this.list = list;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
            return new RecyclerView.ViewHolder(v) {};
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder h, int position) {
            CartManager.CartItem item = list.get(position);

            TextView tvName = h.itemView.findViewById(R.id.tvCartItemName);
            TextView tvPrice = h.itemView.findViewById(R.id.tvCartItemPrice);
            Button btnRemove = h.itemView.findViewById(R.id.btnRemoveItem);

            if (tvName != null) tvName.setText(item.title);

            if (tvPrice != null) {
                double sum = item.price * item.quantity;
                tvPrice.setText(String.format("%.2f€ x %d = %.2f€", item.price, item.quantity, sum));
            }

            if (btnRemove != null) {
                btnRemove.setOnClickListener(v -> {
                    list.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, list.size());

                    updateTotal();

                    if (list.isEmpty()) {
                        CartManager.getInstance().clear();
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    private void submitOrder() {
        if (CartManager.getInstance().getItems().isEmpty()) {
            Toast.makeText(this, "Krepšelis tuščias!", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        Long userId = prefs.getLong("userId", -1);

        Map<String, Object> orderRequest = new HashMap<>();
        orderRequest.put("userId", userId);
        orderRequest.put("restaurantId", CartManager.getInstance().getRestaurantId());
        orderRequest.put("items", CartManager.getInstance().getItems());

        Toast.makeText(this, "Siunčiamas užsakymas...", Toast.LENGTH_SHORT).show();

        RetrofitClient.getApiService().createOrder(orderRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CartActivity.this, "Užsakymas sėkmingas!", Toast.LENGTH_LONG).show();
                    CartManager.getInstance().clear();
                    finish();
                } else {
                    String errorMsg = "Klaida";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(CartActivity.this, "Klaida: " + errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(CartActivity.this, "Ryšio klaida: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}