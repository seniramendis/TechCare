package com.example.techcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ServicesActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    // UI Components for the Review Section
    private RatingBar ratingBar;
    private EditText etComment;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);

        dbHelper = new DatabaseHelper(this);

        HeaderUtils.setupHeader(this);
        setupBottomNav();
        setupClickListeners();

        // Initialize the new feedback section
        setupFeedbackSection();
    }

    private void setupFeedbackSection() {
        ratingBar = findViewById(R.id.rating_bar_inline);
        etComment = findViewById(R.id.et_review_inline);
        btnSubmit = findViewById(R.id.btn_submit_review);

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> submitReview());
        }
    }

    private void submitReview() {
        // 1. Check if user is logged in
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String email = prefs.getString("email", null);

        if (email == null) {
            Toast.makeText(this, "Please login to write a review", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        // 2. Get input data
        float rating = ratingBar.getRating();
        String comment = etComment.getText().toString().trim();

        // 3. Validate input
        if (rating > 0 && !TextUtils.isEmpty(comment)) {
            // 4. Save to Database
            boolean success = dbHelper.addReview(email, (int) rating, comment);
            if (success) {
                Toast.makeText(ServicesActivity.this, "Thanks for your feedback!", Toast.LENGTH_SHORT).show();
                // Clear the form
                etComment.setText("");
                ratingBar.setRating(0);
            } else {
                Toast.makeText(ServicesActivity.this, "Error submitting review", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ServicesActivity.this, "Please provide a rating and comment", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        CardView cardPhone = findViewById(R.id.card_service_phone);
        CardView cardLaptop = findViewById(R.id.card_service_laptop);
        CardView cardHome = findViewById(R.id.card_service_home);
        CardView cardOther = findViewById(R.id.card_service_other);

        if(cardPhone != null) cardPhone.setOnClickListener(v -> openBooking("Smartphone"));
        if(cardLaptop != null) cardLaptop.setOnClickListener(v -> openBooking("Laptop/PC"));
        if(cardHome != null) cardHome.setOnClickListener(v -> openBooking("Home Appliance"));
        if(cardOther != null) cardOther.setOnClickListener(v -> openBooking("Other Device"));
    }

    private void openBooking(String deviceType) {
        Intent intent = new Intent(this, BookingActivity.class);
        intent.putExtra("DEVICE_TYPE", deviceType);
        startActivity(intent);
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav == null) return;

        bottomNav.setSelectedItemId(R.id.nav_services);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_services) return true;

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
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }
}