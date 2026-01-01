package com.example.techcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    DatabaseHelper db;
    TextInputEditText etIdentifier, etPassword; // [CHANGED] name to identifier
    Button btnLogin;
    TextView tvSignup, tvForgotPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);

        etIdentifier = findViewById(R.id.et_login_email);
        etPassword = findViewById(R.id.et_login_password);
        btnLogin = findViewById(R.id.btn_login);
        tvSignup = findViewById(R.id.tv_signup);
        tvForgotPass = findViewById(R.id.tv_forgot_pass);

        tvSignup.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            finish();
        });

        tvForgotPass.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });

        btnLogin.setOnClickListener(v -> {
            String identifier = etIdentifier.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (identifier.equals("") || password.equals("")) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            } else {
                // [CHANGED] Check user by Identifier (Email OR Phone)
                boolean checkuser = db.checkUser(identifier, password);
                if (checkuser) {
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();

                    // [NEW] Resolve Email from the identifier (if user logged in with phone)
                    // We need the email because it's used as the key for Bookings
                    String resolvedEmail = db.getEmailFromIdentifier(identifier);

                    // Save to Session
                    SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("email", resolvedEmail);
                    editor.apply();

                    // Navigate to Home
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}