package com.example.techcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class SupportActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FaqAdapter adapter;
    private List<FaqAdapter.FaqItem> faqList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        // 1. Setup Header & Nav
        HeaderUtils.setupHeader(this);
        setupBottomNav();

        // 2. Customize Header Subtitle for Support
        TextView headerSubtitle = findViewById(R.id.tv_header_subtitle);
        if (headerSubtitle != null) {
            headerSubtitle.setText("How can we help you today?");
        }

        // 3. Setup Back Button
        ImageView btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 4. Setup RecyclerView
        recyclerView = findViewById(R.id.recycler_faq);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadFaqData();
        adapter = new FaqAdapter(faqList);
        recyclerView.setAdapter(adapter);

        // 5. Check Login Status (Optional Debug)
        checkLoginStatus();
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav == null) return;

        // Highlight "Services" because Support is part of the Services section
        bottomNav.setSelectedItemId(R.id.nav_services);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // If user clicks "Services", go back to the main Services grid
            if (id == R.id.nav_services) {
                startActivity(new Intent(this, ServicesActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            // Other Tabs
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

    private void checkLoginStatus() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String email = prefs.getString("email", null);
        if (email == null) {
            // Just a quiet log or toast for debugging
            // Toast.makeText(this, "Guest Mode", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadFaqData() {
        faqList = new ArrayList<>();

        faqList.add(new FaqAdapter.FaqItem(
                "How do I book a repair?",
                "Navigate to the Services tab, choose your device type (Smartphone, Laptop, etc.), fill in the problem details, and click 'Book Now'."
        ));

        faqList.add(new FaqAdapter.FaqItem(
                "What is the warranty period?",
                "We offer a 30-day service warranty on all repairs. If the exact same issue reoccurs within 30 days, we fix it for free."
        ));

        faqList.add(new FaqAdapter.FaqItem(
                "How can I track my repair status?",
                "Go to the 'My Bookings' tab in the bottom navigation bar. You will see the real-time status of your active requests."
        ));

        faqList.add(new FaqAdapter.FaqItem(
                "Do you offer home service?",
                "Yes, for heavy home appliances like Washing Machines, ACs, and Refrigerators, our technicians will visit your home."
        ));

        faqList.add(new FaqAdapter.FaqItem(
                "What are your payment options?",
                "We currently accept cash on delivery/completion. Online payment options will be added in the next update."
        ));

        faqList.add(new FaqAdapter.FaqItem(
                "I forgot my password, what do I do?",
                "On the Login screen, tap 'Forgot Password?'. Enter your registered email to receive a reset link."
        ));
    }
}