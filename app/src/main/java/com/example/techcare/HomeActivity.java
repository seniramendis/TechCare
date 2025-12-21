package com.example.techcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // --- ACTIVATE HEADER LOGIC ---
        // This one line handles the "Good Morning, Name" feature
        HeaderUtils.setupHeader(this);
        // -----------------------------

        // Category Cards Setup
        CardView cardPhone = findViewById(R.id.card_smartphone);
        CardView cardLaptop = findViewById(R.id.card_laptop);
        CardView cardAppliance = findViewById(R.id.card_appliance);
        CardView cardOther = findViewById(R.id.card_other);

        if (cardPhone != null) cardPhone.setOnClickListener(v -> openBooking("Smartphone"));
        if (cardLaptop != null) cardLaptop.setOnClickListener(v -> openBooking("Laptop/PC"));
        if (cardAppliance != null) cardAppliance.setOnClickListener(v -> openBooking("Home Appliance"));
        if (cardOther != null) cardOther.setOnClickListener(v -> openBooking("Other Device"));

        // Logout Button Logic
        Button btnLogout = findViewById(R.id.btn_logout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear(); // Clear session
                editor.apply();

                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void openBooking(String deviceType) {
        Intent intent = new Intent(HomeActivity.this, BookingActivity.class);
        intent.putExtra("DEVICE_TYPE", deviceType);
        startActivity(intent);
    }
}