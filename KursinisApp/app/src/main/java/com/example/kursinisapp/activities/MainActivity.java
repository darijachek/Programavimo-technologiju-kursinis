package com.example.kursinisapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        long userId = prefs.getLong("userId", -1);
        String role = prefs.getString("role", "");

        if (userId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            navigateToDashboard(role);
        }
        finish();
    }

    private void navigateToDashboard(String role) {
        Intent intent;
        if ("CLIENT".equalsIgnoreCase(role)) {
            intent = new Intent(this, ClientActivity.class);
        } else {
            intent = new Intent(this, DriverActivity.class);
        }
        startActivity(intent);
    }
}