package com.example.techcare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class SignupActivity extends AppCompatActivity {

    DatabaseHelper db;
    TextInputEditText etName, etEmail, etPhone, etPassword; // [CHANGED] Added etPhone
    Button btnSignup;
    TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        db = new DatabaseHelper(this);

        etName = findViewById(R.id.et_signup_name);
        etEmail = findViewById(R.id.et_signup_email);
        etPhone = findViewById(R.id.et_signup_phone); // [NEW] Bind
        etPassword = findViewById(R.id.et_signup_password);
        btnSignup = findViewById(R.id.btn_signup);
        tvLogin = findViewById(R.id.tv_login);

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });

        btnSignup.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim(); // [NEW] Get text
            String password = etPassword.getText().toString().trim();

            if (name.equals("") || email.equals("") || phone.equals("") || password.equals("")) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                if (db.checkEmail(email)) {
                    Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show();
                } else {
                    // [CHANGED] Pass phone to insertUser
                    boolean insert = db.insertUser(name, email, phone, password);
                    if (insert) {
                        Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}