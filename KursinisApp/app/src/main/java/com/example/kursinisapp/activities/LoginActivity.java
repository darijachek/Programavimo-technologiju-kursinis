package com.example.kursinisapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kursinisapp.R;
import com.example.kursinisapp.activities.ClientActivity;
import com.example.kursinisapp.activities.DriverActivity;
import com.example.kursinisapp.activities.RegisterActivity;
import com.example.kursinisapp.models.User;
import com.example.kursinisapp.remote.RetrofitClient;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegisterLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);

        btnLogin.setOnClickListener(v -> {
            String user = etUsername.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Užpildykite visus laukus", Toast.LENGTH_SHORT).show();
            } else {
                attemptLogin(user, pass);
            }
        });

        tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void attemptLogin(String username, String password) {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", username);
        credentials.put("password", password);

        RetrofitClient.getApiService().login(credentials).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();

                        if (jsonResponse.equals("Error")) {
                            Toast.makeText(LoginActivity.this, "Neteisingas vardas arba slaptažodis", Toast.LENGTH_SHORT).show();
                        } else {
                            User user = new Gson().fromJson(jsonResponse, User.class);

                            saveUserSession(user);

                            navigateToDashboard(user.role);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, "Klaida apdorojant duomenis", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Prisijungimo klaida", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Nepavyko prisijungti prie serverio", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserSession(User user) {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putLong("userId", user.id);
        editor.putString("username", user.username);
        editor.putString("role", user.role);

        if ("CLIENT".equalsIgnoreCase(user.role)) {
            editor.putInt("loyaltyPoints", user.loyaltyPoints);
            editor.putString("address", user.address != null ? user.address : "");
        }

        if ("DRIVER".equalsIgnoreCase(user.role)) {
            editor.putString("vehicle", user.vehicle != null ? user.vehicle : "");
        }

        editor.apply();
    }

    private void navigateToDashboard(String role) {
        Intent intent;
        if ("CLIENT".equalsIgnoreCase(role)) {
            intent = new Intent(LoginActivity.this, ClientMenuActivity.class);
        } else if ("DRIVER".equalsIgnoreCase(role)) {
            intent = new Intent(LoginActivity.this, DriverActivity.class);
        } else {
            Toast.makeText(this, "Ši rolė neturi prieigos prie programėlės. Naudokite Desktop", Toast.LENGTH_LONG).show();
            return;
        }
        startActivity(intent);
        finish();
    }
}