package com.example.techcare;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class ResetPasswordActivity extends AppCompatActivity {

    DatabaseHelper db;
    String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        db = new DatabaseHelper(this);
        // Receive email from the previous screen
        userEmail = getIntent().getStringExtra("EMAIL");

        TextInputEditText etPass = findViewById(R.id.et_new_password);
        TextInputEditText etConfirmPass = findViewById(R.id.et_confirm_password);
        Button btnSave = findViewById(R.id.btn_save_password);

        btnSave.setOnClickListener(v -> {
            String pass = etPass.getText().toString().trim();
            String confirmPass = etConfirmPass.getText().toString().trim();

            if (TextUtils.isEmpty(pass) || TextUtils.isEmpty(confirmPass)) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else if (!pass.equals(confirmPass)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                if (db.updatePassword(userEmail, pass)) {
                    Toast.makeText(this, "Password Updated Successfully!", Toast.LENGTH_SHORT).show();
                    // Go to Login
                    Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}