package com.example.techcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView tvName, tvEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DatabaseHelper(this);

        // Setup UI
        tvName = findViewById(R.id.tv_profile_name);
        tvEmail = findViewById(R.id.tv_profile_email);

        HeaderUtils.setupHeader(this);
        setupBottomNav();
        setupMenuOptions();

        // Load User Data
        loadUserProfile();
    }

    private void loadUserProfile() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String email = prefs.getString("email", "Guest");

        tvEmail.setText(email);

        // Fetch Name from Database using Email
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            // Query matches your DatabaseHelper structure
            Cursor cursor = db.rawQuery("SELECT username FROM users WHERE email = ?", new String[]{email});

            if (cursor.moveToFirst()) {
                String name = cursor.getString(0);
                tvName.setText(name);
            } else {
                tvName.setText("TechCare User");
            }
            cursor.close();
        } catch (Exception e) {
            tvName.setText("TechCare User");
            e.printStackTrace();
        }
    }

    private void setupMenuOptions() {
        // My Bookings Navigation
        findViewById(R.id.menu_my_bookings).setOnClickListener(v -> {
            startActivity(new Intent(this, MyBookingsActivity.class));
        });

        // Edit Profile (Placeholder)
        findViewById(R.id.btn_edit_profile).setOnClickListener(v -> {
            Toast.makeText(this, "Edit Profile feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Help (Placeholder)
        findViewById(R.id.menu_help).setOnClickListener(v -> {
            Toast.makeText(this, "Support Center coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Logout Logic
        Button btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            // 1. Clear SharedPreferences
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            // 2. Redirect to Login and clear history so user can't go back
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_profile);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) return true;

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_bookings) {
                startActivity(new Intent(this, MyBookingsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_services) {
                startActivity(new Intent(this, ServicesActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }
}