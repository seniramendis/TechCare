package com.example.techcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
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

        // Load background images from API (using Glide)
        loadServiceImages();
    }

    private void loadServiceImages() {
        // Using high-quality unsplash images.

        // Smartphone Repair
        ImageView imgPhone = findViewById(R.id.img_bg_phone);
        if (imgPhone != null) {
            Glide.with(this)
                    .load("https://images.unsplash.com/photo-1512054502232-10a0a035d672?w=500&q=80")
                    .centerCrop()
                    .into(imgPhone);
        }

        // Laptop/PC Repair
        ImageView imgLaptop = findViewById(R.id.img_bg_laptop);
        if (imgLaptop != null) {
            Glide.with(this)
                    .load("https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=500&q=80")
                    .centerCrop()
                    .into(imgLaptop);
        }

        // Home Appliances
        ImageView imgHome = findViewById(R.id.img_bg_home);
        if (imgHome != null) {
            Glide.with(this)
                    .load("https://images.unsplash.com/photo-1556911220-e15b29be8c8f?w=500&q=80")
                    .centerCrop()
                    .into(imgHome);
        }

        // Other Electronics
        ImageView imgOther = findViewById(R.id.img_bg_other);
        if (imgOther != null) {
            Glide.with(this)
                    .load("https://images.unsplash.com/photo-1550009158-9ebf69173e03?w=500&q=80")
                    .centerCrop()
                    .into(imgOther);
        }
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
        View cardPhone = findViewById(R.id.card_service_phone);
        View cardLaptop = findViewById(R.id.card_service_laptop);
        View cardHome = findViewById(R.id.card_service_home);
        View cardOther = findViewById(R.id.card_service_other);

        // Listener for the new Support Card (UPDATED to start Activity)
        View cardSupport = findViewById(R.id.card_support);
        if (cardSupport != null) {
            cardSupport.setOnClickListener(v -> {
                startActivity(new Intent(ServicesActivity.this, SupportActivity.class));
            });
        }

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