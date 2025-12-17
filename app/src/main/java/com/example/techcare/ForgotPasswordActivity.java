package com.example.techcare;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        TextView backToLogin = findViewById(R.id.tv_back_login);
        backToLogin.setOnClickListener(v -> finish()); // Simply closes this screen to return to Login
    }
}