package com.example.kursinisapp.activities;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kursinisapp.R;
import com.example.kursinisapp.remote.RetrofitClient;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kursinisapp.remote.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etExtraInfo;
    private RadioGroup rgRole;
    private Button btnRegister;
    private TextView tvLoginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        etUsername = findViewById(R.id.etRegUsername);
        etPassword = findViewById(R.id.etRegPassword);
        etExtraInfo = findViewById(R.id.etExtraInfo);
        rgRole = findViewById(R.id.rgRole);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        rgRole.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbClient) {
                etExtraInfo.setHint("Pristatymo adresas");
            } else {
                etExtraInfo.setHint("Transporto priemonė (pvz. Toyota Prius)");
            }
        });

        btnRegister.setOnClickListener(v -> handleRegistration());

        tvLoginLink.setOnClickListener(v -> finish());
    }

    private void handleRegistration() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String extra = etExtraInfo.getText().toString().trim();

        String role = rgRole.getCheckedRadioButtonId() == R.id.rbClient ? "CLIENT" : "DRIVER";

        if (username.isEmpty() || password.isEmpty() || extra.isEmpty()) {
            Toast.makeText(this, "Prašome užpildyti visus laukus", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("password", password);
        userData.put("role", role);

        if (role.equals("CLIENT")) {
            userData.put("address", extra);
        } else {
            userData.put("vehicle", extra);
        }

        RetrofitClient.getApiService().register(userData).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String result = response.body().string();

                        if (result.contains("Success")) {
                            Toast.makeText(RegisterActivity.this, "Registracija sėkminga! Prisijunkite.", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Klaida: " + result, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Serverio klaida", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Nepavyko pasiekti serverio", Toast.LENGTH_SHORT).show();
            }
        });
    }
}