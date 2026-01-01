// File: app/src/main/java/com/example/techcare/BookingActivity.java
package com.example.techcare;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.Calendar;
import java.util.Locale;

public class BookingActivity extends AppCompatActivity {

    DatabaseHelper db;
    EditText etDevice, etIssue, etDate, etTime; // [CHANGED] Added etDate, etTime
    Button btnSubmit, btnUpload;
    ImageView imgPreview, imgHero, imgPickup, imgDropoff, icCheckPickup, icCheckDropoff;
    TextView tvPhotoStatus, tvTitle;
    View cardPickup, cardDropoff;

    String selectedImageUri = "";
    String serviceType = "Drop-off";
    ActivityResultLauncher<String> mGetContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        db = new DatabaseHelper(this);

        // Bind Views
        etDevice = findViewById(R.id.et_device);
        etIssue = findViewById(R.id.et_issue);
        etDate = findViewById(R.id.et_schedule_date); // [NEW]
        etTime = findViewById(R.id.et_schedule_time); // [NEW]
        btnSubmit = findViewById(R.id.btn_submit_request);
        btnUpload = findViewById(R.id.btn_upload_photo);
        imgPreview = findViewById(R.id.img_preview);
        tvPhotoStatus = findViewById(R.id.tv_photo_status);
        tvTitle = findViewById(R.id.tv_booking_title);

        imgHero = findViewById(R.id.img_hero_banner);
        imgPickup = findViewById(R.id.img_pickup);
        imgDropoff = findViewById(R.id.img_dropoff);

        cardPickup = findViewById(R.id.card_pickup);
        cardDropoff = findViewById(R.id.card_dropoff);
        icCheckPickup = findViewById(R.id.ic_check_pickup);
        icCheckDropoff = findViewById(R.id.ic_check_dropoff);

        HeaderUtils.setupHeader(this);
        setupBottomNav();
        setupSearchBar();
        setupApiImages();
        setupServiceSelection();

        // [NEW] Setup Date & Time Pickers
        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());

        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri.toString();
                        imgPreview.setImageURI(uri);
                        imgPreview.setVisibility(View.VISIBLE);
                        tvPhotoStatus.setText("Photo Selected");
                    }
                });

        btnUpload.setOnClickListener(v -> mGetContent.launch("image/*"));
        btnSubmit.setOnClickListener(v -> submitBooking());
    }

    // [NEW] Show Date Picker Dialog
    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                    etDate.setText(date);
                }, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000); // Disable past dates
        datePickerDialog.show();
    }

    // [NEW] Show Time Picker Dialog
    private void showTimePicker() {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute1) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
                    etTime.setText(time);
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private void setupApiImages() {
        String autoDevice = getIntent().getStringExtra("DEVICE_TYPE");
        String heroUrl = "https://images.unsplash.com/photo-1581092921461-eab62e97a782?w=800&q=80";

        if(autoDevice != null) {
            etDevice.setText(autoDevice);
            tvTitle.setText("Repair " + autoDevice);

            if(autoDevice.toLowerCase().contains("phone")) {
                heroUrl = "https://images.unsplash.com/photo-1596558450255-7c0b7be9d56a?w=800&q=80";
            } else if(autoDevice.toLowerCase().contains("laptop") || autoDevice.toLowerCase().contains("pc")) {
                heroUrl = "https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=800&q=80";
            } else if(autoDevice.toLowerCase().contains("appliance")) {
                heroUrl = "https://images.unsplash.com/photo-1556911220-e15b29be8c8f?w=800&q=80";
            }
        }

        Glide.with(this).load(heroUrl).centerCrop().into(imgHero);
        Glide.with(this).load("https://images.unsplash.com/photo-1616401784845-180882ba9ba8?w=400&q=80").centerCrop().into(imgPickup);
        Glide.with(this).load("https://images.unsplash.com/photo-1581092160562-40aa08e78837?w=400&q=80").centerCrop().into(imgDropoff);
    }

    private void setupServiceSelection() {
        cardPickup.setOnClickListener(v -> {
            serviceType = "Pickup";
            updateSelectionUI(true);
        });

        cardDropoff.setOnClickListener(v -> {
            serviceType = "Drop-off";
            updateSelectionUI(false);
        });
    }

    private void updateSelectionUI(boolean isPickup) {
        if(isPickup) {
            icCheckPickup.setVisibility(View.VISIBLE);
            icCheckDropoff.setVisibility(View.GONE);
            ((CardView)cardPickup).setCardBackgroundColor(Color.parseColor("#2C3E50"));
            ((CardView)cardDropoff).setCardBackgroundColor(Color.parseColor("#1B262C"));
        } else {
            icCheckPickup.setVisibility(View.GONE);
            icCheckDropoff.setVisibility(View.VISIBLE);
            ((CardView)cardPickup).setCardBackgroundColor(Color.parseColor("#1B262C"));
            ((CardView)cardDropoff).setCardBackgroundColor(Color.parseColor("#2C3E50"));
        }
    }

    private void submitBooking() {
        String device = etDevice.getText().toString();
        String issue = etIssue.getText().toString();
        String date = etDate.getText().toString(); // [NEW]
        String time = etTime.getText().toString(); // [NEW]

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String email = prefs.getString("email", "");

        if(email.isEmpty()) {
            Toast.makeText(this, "Error: You are not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        // [CHANGED] Added validation for date/time
        if(device.isEmpty() || issue.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Please fill in all details including schedule", Toast.LENGTH_SHORT).show();
            return;
        }

        // [CHANGED] Pass date and time to database
        boolean success = db.addBooking(email, device, issue, serviceType, selectedImageUri, date, time);
        if(success) {
            Toast.makeText(this, "Repair Request Submitted!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, MyBookingsActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Failed to submit request", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_bookings);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    Intent intent = new Intent(BookingActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (id == R.id.nav_services) {
                    startActivity(new Intent(BookingActivity.this, ServicesActivity.class));
                    finish();
                    return true;
                } else if (id == R.id.nav_bookings) {
                    startActivity(new Intent(BookingActivity.this, MyBookingsActivity.class));
                    finish();
                    return true;
                } else if (id == R.id.nav_profile) {
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
                @Override public boolean onQueryTextSubmit(String query) {
                    Toast.makeText(BookingActivity.this, "Searching: " + query, Toast.LENGTH_SHORT).show();
                    return true;
                }
                @Override public boolean onQueryTextChange(String newText) { return true; }
            });
        }
    }
}