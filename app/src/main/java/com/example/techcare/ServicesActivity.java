package com.example.techcare;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ServicesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);

        HeaderUtils.setupHeader(this);
        setupBottomNav();
        setupClickListeners();
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

        bottomNav.setSelectedItemId(R.id.nav_services); // Highlight Services Item

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