package com.example.techcare;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TechTipDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tech_tip_detail);

        // --- 1. Setup Header (Using the helper class) ---
        try {
            HeaderUtils.setupHeader(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // --- 2. Initialize Views ---
        ImageView heroImage = findViewById(R.id.img_detail_hero);
        TextView titleText = findViewById(R.id.tv_detail_title);
        TextView contentText = findViewById(R.id.tv_detail_content);
        Toolbar toolbar = findViewById(R.id.detail_toolbar);

        // --- 3. Setup Toolbar (Back Button) ---
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show back arrow
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            // Manually set the back icon drawable if needed, or use default
            // getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        }
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // --- 4. Get Data from Intent ---
        String title = getIntent().getStringExtra("TIP_TITLE");
        String content = getIntent().getStringExtra("TIP_CONTENT");
        String imageUrl = getIntent().getStringExtra("TIP_IMAGE");

        if (title != null) titleText.setText(title);
        if (content != null) contentText.setText(content);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(heroImage);
        }

        // --- 5. Setup Bottom Navigation ---
        setupBottomNav();
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            // Uncheck all items since we are in a detail view, or leave as is
            // bottomNav.getMenu().setGroupCheckable(0, false, true);

            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                    return true;
                }
                if (id == R.id.nav_services) {
                    startActivity(new Intent(this, ServicesActivity.class));
                    finish();
                    return true;
                }
                if (id == R.id.nav_bookings) {
                    startActivity(new Intent(this, MyBookingsActivity.class));
                    finish();
                    return true;
                }
                if (id == R.id.nav_profile) {
                    startActivity(new Intent(this, ProfileActivity.class));
                    finish();
                    return true;
                }
                return false;
            });
        }
    }
}