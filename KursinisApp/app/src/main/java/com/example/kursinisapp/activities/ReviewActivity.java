package com.example.kursinisapp.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
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

public class ReviewActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private EditText etComment;
    private Long targetId;
    private String targetType;
    private Long authorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_activity);

        targetId = getIntent().getLongExtra("targetId", -1);
        targetType = getIntent().getStringExtra("targetType");
        authorId = getSharedPreferences("UserSession", MODE_PRIVATE).getLong("userId", -1);

        TextView tvTitle = findViewById(R.id.tvReviewTitle);
        if ("CLIENT".equals(targetType)) tvTitle.setText("Įvertinkite klientą");
        else if ("RESTAURANT".equals(targetType)) tvTitle.setText("Įvertinkite restoraną");
        else if ("DRIVER".equals(targetType)) tvTitle.setText("Įvertinkite vairuotoją");

        ratingBar = findViewById(R.id.ratingBar);
        etComment = findViewById(R.id.etReviewComment);
        Button btnSubmit = findViewById(R.id.btnSubmitReview);

        btnSubmit.setOnClickListener(v -> submitReview());
    }

    private void submitReview() {
        int rating = (int) ratingBar.getRating();
        String comment = etComment.getText().toString();

        if (rating == 0) {
            Toast.makeText(this, "Prašome pasirinkti įvertinimą", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("authorId", authorId);
        data.put("targetType", targetType);
        data.put("rating", rating);
        data.put("comment", comment);

        if ("CLIENT".equals(targetType)) {
            data.put("clientId", targetId);
        } else if ("DRIVER".equals(targetType)) {
            data.put("driverId", targetId);
        } else if ("RESTAURANT".equals(targetType)) {
            data.put("restaurantId", targetId);
        }

        RetrofitClient.getApiService().postReview(data).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ReviewActivity.this, "Dėkojame už įvertinimą!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ReviewActivity.this, "Klaida: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ReviewActivity.this, "Klaida siunčiant įvertinimą", Toast.LENGTH_SHORT).show();
            }
        });
    }
}