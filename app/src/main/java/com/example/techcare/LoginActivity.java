package com.example.techcare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1. Link the "Sign Up" text
        TextView signUpText = findViewById(R.id.tv_signup);
        if (signUpText != null) {
            signUpText.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            });
        }

        // 2. Link the "Forgot Password?" text
        // FIXED: Now looking for the correct ID 'tv_forgot_pass'
        TextView forgotPassText = findViewById(R.id.tv_forgot_pass);

        if (forgotPassText != null) {
            forgotPassText.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            });
        }
    }
}