package com.example.techcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

        // [FIX 1] Create Notification Channel to prevent crashes
        NotificationHelper.createNotificationChannel(this);

        db = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recycler_bookings);
        emptyState = findViewById(R.id.tv_empty_state);

        HeaderUtils.setupHeader(this);
        setupBottomNav();
    }

    // [FIX 2] Refresh data every time you enter this screen
    @Override
    protected void onResume() {
        super.onResume();
        loadBookings();
    }

    private void loadBookings() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String email = prefs.getString("email", "");

        List<BookingModel> bookingList = new ArrayList<>();
        Cursor cursor = db.getUserBookings(email);

        if (cursor != null && cursor.moveToFirst()) {
            // [FIX 3] SAFELY find column numbers.
            // This prevents the "vanishing status" bug if the DB structure changes.
            int idIndex = cursor.getColumnIndex("booking_id");
            int deviceIndex = cursor.getColumnIndex("device_type");
            int issueIndex = cursor.getColumnIndex("issue_description");
            int typeIndex = cursor.getColumnIndex("service_type");
            int statusIndex = cursor.getColumnIndex("status");
            int techIndex = cursor.getColumnIndex("technician_name");

            do {
                BookingModel b = new BookingModel();
                // If column exists (!= -1), get data. Otherwise use default.
                b.id = (idIndex != -1) ? cursor.getInt(idIndex) : 0;
                b.device = (deviceIndex != -1) ? cursor.getString(deviceIndex) : "Unknown Device";
                b.issue = (issueIndex != -1) ? cursor.getString(issueIndex) : "";
                b.type = (typeIndex != -1) ? cursor.getString(typeIndex) : "";
                b.status = (statusIndex != -1) ? cursor.getString(statusIndex) : "Received";

                // Handle Technician (safely)
                if (techIndex != -1) {
                    b.technician = cursor.getString(techIndex);
                }
                if (b.technician == null || b.technician.isEmpty()) {
                    b.technician = "Pending Assignment";
                }

                bookingList.add(b);
            } while (cursor.moveToNext());
            cursor.close();
        }

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

    private void cancelBooking(int bookingId, String deviceName) {
        boolean success = db.updateBookingStatus(bookingId, "Cancelled");
        if (success) {
            Toast.makeText(this, "Booking Cancelled", Toast.LENGTH_SHORT).show();

            // Trigger Notification
            NotificationHelper.sendBookingNotification(
                    this,
                    "Booking Cancelled",
                    "Your repair request for " + deviceName + " has been cancelled."
            );

            loadBookings(); // Refresh list immediately
        } else {
            Toast.makeText(this, "Failed to cancel", Toast.LENGTH_SHORT).show();
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
            } else if (id == R.id.nav_services) {
                startActivity(new Intent(this, ServicesActivity.class));
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

    // --- Model & Adapter ---
    static class BookingModel {
        int id;
        String device, issue, type, status;
        String technician;
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
            h.tech.setText("Tech: " + m.technician);

            // Long Press to Cancel
            h.itemView.setOnLongClickListener(v -> {
                if ("Cancelled".equals(m.status) || "Completed".equals(m.status)) {
                    Toast.makeText(MyBookingsActivity.this, "Cannot cancel this booking", Toast.LENGTH_SHORT).show();
                    return true;
                }

                new AlertDialog.Builder(MyBookingsActivity.this)
                        .setTitle("Cancel Booking")
                        .setMessage("Are you sure you want to cancel this repair request?")
                        .setPositiveButton("Yes", (dialog, which) -> cancelBooking(m.id, m.device))
                        .setNegativeButton("No", null)
                        .show();
                return true;
            });
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
                tech = v.findViewById(R.id.tv_technician);
            }
        }
    }
}