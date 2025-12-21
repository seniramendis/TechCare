package com.example.techcare;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class SignupActivity extends AppCompatActivity {

    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        db = new DatabaseHelper(this);

        // Finding Views by the IDs we added in XML
        TextInputEditText etName = findViewById(R.id.et_signup_name);
        TextInputEditText etEmail = findViewById(R.id.et_signup_email);
        TextInputEditText etPassword = findViewById(R.id.et_signup_password);
        Button btnSignup = findViewById(R.id.btn_signup);
        TextView loginLink = findViewById(R.id.tv_login_link);

        // Sign Up Button Logic
        btnSignup.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            } else {
                // Save to SQLite
                boolean isInserted = db.insertUser(name, email, password);
                if (isInserted) {
                    Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();
                    // Redirect to Login
                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Registration Failed. Email may already exist.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Link to Login Page
        loginLink.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}