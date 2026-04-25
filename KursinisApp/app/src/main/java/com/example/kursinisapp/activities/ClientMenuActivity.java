package com.example.kursinisapp.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.kursinisapp.R;

public class ClientMenuActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_menu_activity);

        findViewById(R.id.btnRestaurants).setOnClickListener(v ->
                startActivity(new Intent(this, ClientActivity.class))
        );

        findViewById(R.id.btnClientHistory).setOnClickListener(v ->
                startActivity(new Intent(this, OrderHistoryActivity.class))
        );

        findViewById(R.id.btnClientProfile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class))
        );
    }
}
