package com.example.kursinisapp.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.kursinisapp.R;

public class DriverActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_activity);

        findViewById(R.id.btnAvailableOrders).setOnClickListener(v ->
                startActivity(new Intent(this, AvailableOrdersActivity.class))
        );

        findViewById(R.id.btnDriverHistory).setOnClickListener(v ->
                startActivity(new Intent(this, DriverHistoryActivity.class))
        );

        findViewById(R.id.btnDriverProfile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class))
        );
    }
}
