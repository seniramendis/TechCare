package com.example.techcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BookingActivity extends AppCompatActivity {

    DatabaseHelper db;
    EditText etDevice, etIssue;
    RadioGroup rgType;
    Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        db = new DatabaseHelper(this);

        // Link Java variables to XML IDs
        etDevice = findViewById(R.id.et_device);
        etIssue = findViewById(R.id.et_issue);
        rgType = findViewById(R.id.rg_service_type);
        btnSubmit = findViewById(R.id.btn_submit_request);

        // --- 1. SETUP HEADER LOGIC ---
        HeaderUtils.setupHeader(this);

        // --- 2. SETUP BOTTOM NAVIGATION ---
        setupBottomNav();

        // --- 3. SETUP SEARCH BAR ---
        setupSearchBar();

        // Check if we passed a device type from the Home Screen
        String autoDevice = getIntent().getStringExtra("DEVICE_TYPE");
        if(autoDevice != null) {
            etDevice.setText(autoDevice);
        }

        // Handle Submit Button
        btnSubmit.setOnClickListener(v -> {
            String device = etDevice.getText().toString();
            String issue = etIssue.getText().toString();

            // Get selected Radio Button text
            int selectedId = rgType.getCheckedRadioButtonId();
            RadioButton selectedBtn = findViewById(selectedId);
            String type = (selectedBtn != null) ? selectedBtn.getText().toString() : "Drop-off";

            // Get User Email from Session
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String email = prefs.getString("email", "");

            if(email.isEmpty()) {
                Toast.makeText(this, "Error: You are not logged in!", Toast.LENGTH_SHORT).show();
                return;
            }

            if(device.isEmpty() || issue.isEmpty()) {
                Toast.makeText(this, "Please fill in all details", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save to Database
            boolean success = db.addBooking(email, device, issue, type);
            if(success) {
                Toast.makeText(this, "Repair Request Submitted!", Toast.LENGTH_LONG).show();

                // Go to My Bookings (Dashboard) after submitting
                startActivity(new Intent(this, MyBookingsActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Failed to submit request", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            // FIX: Removed the line that forced 'Home' to be selected on start.
            // Since we are in BookingActivity, we shouldn't highlight Home.
            // If you have a specific ID for 'New Booking' in your menu, select that instead.

            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    // Navigate to Home AND Clear Stack
                    Intent intent = new Intent(BookingActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                    return true;
                }
                else if (id == R.id.nav_bookings) {
                    // Navigate to My Bookings Dashboard
                    startActivity(new Intent(BookingActivity.this, MyBookingsActivity.class));
                    finish();
                    return true;
                }
                else if (id == R.id.nav_profile) {
                    // Navigate to Profile
                    startActivity(new Intent(BookingActivity.this, ProfileActivity.class));
                    return true;
                }
                return false;
            });
        }
    }

    private void setupSearchBar() {
        SearchView searchView = findViewById(R.id.search_view);
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    Toast.makeText(BookingActivity.this, "Searching: " + query, Toast.LENGTH_SHORT).show();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return true;
                }
            });
        }
    }
}