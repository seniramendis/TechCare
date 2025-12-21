package com.example.techcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);

        // Finding Views
        TextInputEditText etEmail = findViewById(R.id.et_login_email);
        TextInputEditText etPassword = findViewById(R.id.et_login_password);
        Button btnLogin = findViewById(R.id.btn_login);
        TextView signUpText = findViewById(R.id.tv_signup);
        TextView forgotPassText = findViewById(R.id.tv_forgot_pass);

        // Login Button Logic
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            } else {
                boolean checkCredentials = db.checkUser(email, password);

                if (checkCredentials) {
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();

                    // Save Email for later use (e.g. Booking repairs)
                    SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("email", email);
                    editor.apply();

                    // Go to Home Activity
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Navigation Links
        if (signUpText != null) {
            signUpText.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            });
        }

        if (forgotPassText != null) {
            forgotPassText.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            });
        }
    }
}