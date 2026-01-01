// File: app/src/main/java/com/example/techcare/MyBookingsActivity.java
package com.example.techcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class MyBookingsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView emptyState;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        db = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recycler_bookings);
        emptyState = findViewById(R.id.tv_empty_state);

        HeaderUtils.setupHeader(this);
        setupBottomNav();
        loadBookings();
    }

    private void loadBookings() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String email = prefs.getString("email", "");

        List<BookingModel> bookingList = new ArrayList<>();
        Cursor cursor = db.getUserBookings(email);

        if (cursor.moveToFirst()) {
            do {
                BookingModel b = new BookingModel();
                b.id = cursor.getInt(0); // booking_id
                b.device = cursor.getString(2); // device
                b.issue = cursor.getString(3); // issue
                b.type = cursor.getString(4); // type
                b.status = cursor.getString(5); // status
                // Index 6 = image, 7 = date, 8 = time
                // [NEW] Get Technician from Index 9
                // Use try-catch or explicit column index to avoid bounds errors if older DB
                try {
                    int techIndex = cursor.getColumnIndex("technician_name");
                    if (techIndex != -1) {
                        b.technician = cursor.getString(techIndex);
                    } else {
                        b.technician = "Pending Assignment";
                    }
                } catch (Exception e) {
                    b.technician = "Pending Assignment";
                }

                if (b.technician == null || b.technician.isEmpty()) {
                    b.technician = "Pending Assignment";
                }

                bookingList.add(b);
            } while (cursor.moveToNext());
        }
        cursor.close();

        if (bookingList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new BookingAdapter(bookingList));
        }
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_bookings);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_bookings) return true;

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            else if (id == R.id.nav_services) {
                startActivity(new Intent(this, ServicesActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    // --- Model & Adapter ---
    static class BookingModel {
        int id;
        String device, issue, type, status;
        String technician; // [NEW] Field
    }

    class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.Holder> {
        List<BookingModel> list;
        BookingAdapter(List<BookingModel> list) { this.list = list; }
        @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new Holder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_booking, p, false));
        }
        @Override public void onBindViewHolder(@NonNull Holder h, int i) {
            BookingModel m = list.get(i);
            h.device.setText(m.device);
            h.issue.setText(m.issue);
            h.type.setText(m.type);
            h.status.setText(m.status);
            h.id.setText("Order #" + m.id);
            // [NEW] Bind Technician
            h.tech.setText("Tech: " + m.technician);
        }
        @Override public int getItemCount() { return list.size(); }
        class Holder extends RecyclerView.ViewHolder {
            TextView device, issue, type, status, id, tech;
            Holder(View v) {
                super(v);
                device = v.findViewById(R.id.tv_device_name);
                issue = v.findViewById(R.id.tv_issue_desc);
                type = v.findViewById(R.id.tv_service_type);
                status = v.findViewById(R.id.chip_status);
                id = v.findViewById(R.id.tv_booking_id);
                tech = v.findViewById(R.id.tv_technician); // [NEW] Bind ID
            }
        }
    }
}