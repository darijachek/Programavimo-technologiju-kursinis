package com.example.kursinisapp.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kursinisapp.R;
import com.example.kursinisapp.remote.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    EditText etUpdateInfo;
    TextView tvLoyaltyPoints;
    String role;
    Long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);

        findViewById(R.id.btnBackProfile).setOnClickListener(v -> finish());

        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        userId = prefs.getLong("userId", -1);
        role = prefs.getString("role", "");

        etUpdateInfo = findViewById(R.id.etUpdateInfo);
        tvLoyaltyPoints = findViewById(R.id.tvLoyaltyPoints);
        TextView tvUser = findViewById(R.id.tvProfileUsername);

        String username = prefs.getString("username", "Nėra vardo");
        tvUser.setText("Vartotojas: " + username);

        if ("CLIENT".equals(role)) {
            etUpdateInfo.setHint("Naujas adresas");

            int points = prefs.getInt("loyaltyPoints", 0);
            tvLoyaltyPoints.setText("Jūsų lojalumo taškai: " + points);
            tvLoyaltyPoints.setVisibility(View.VISIBLE);

            etUpdateInfo.setText(prefs.getString("address", ""));
        } else {
            etUpdateInfo.setHint("Nauja transporto priemonė");
            tvLoyaltyPoints.setVisibility(View.GONE);
            etUpdateInfo.setText(prefs.getString("vehicle", ""));
        }

        findViewById(R.id.btnUpdateProfile).setOnClickListener(v -> updateInfo());
    }

    private void updateInfo() {
        String newValue = etUpdateInfo.getText().toString().trim();
        if (newValue.isEmpty()) {
            Toast.makeText(this, "Laukas negali būti tuščias", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> body = new HashMap<>();
        Call<ResponseBody> call;

        if (role.equals("CLIENT")) {
            body.put("address", newValue);
            call = RetrofitClient.getApiService().updateAddress(userId, body);
        } else {
            body.put("vehicle", newValue);
            call = RetrofitClient.getApiService().updateVehicle(userId, body);
        }

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    SharedPreferences.Editor editor = getSharedPreferences("UserSession", MODE_PRIVATE).edit();
                    if (role.equals("CLIENT")) {
                        editor.putString("address", newValue);
                    } else {
                        editor.putString("vehicle", newValue);
                    }
                    editor.apply();

                    Toast.makeText(ProfileActivity.this, "Informacija sėkmingai atnaujinta!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Klaida atnaujinant: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Tinklo klaida: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}