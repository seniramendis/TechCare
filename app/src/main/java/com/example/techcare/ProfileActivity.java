package com.example.techcare;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 100;
    private DatabaseHelper dbHelper;
    private TextView tvName, tvEmail, tvBookingCount;
    private ImageView imgProfile;
    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DatabaseHelper(this);

        // Get Email from Session
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUserEmail = prefs.getString("email", "Guest");

        // Setup UI References
        tvName = findViewById(R.id.tv_profile_name);
        tvEmail = findViewById(R.id.tv_profile_email);
        tvBookingCount = findViewById(R.id.tv_stat_bookings);
        imgProfile = findViewById(R.id.img_profile_pic);

        HeaderUtils.setupHeader(this);
        setupBottomNav();
        setupMenuOptions();

        // Load Data
        loadUserProfile();
    }

    private void loadUserProfile() {
        tvEmail.setText(currentUserEmail);

        // 1. Fetch User Details (Name & Image)
        try {
            // Using DatabaseHelper to get current data
            String name = dbHelper.getUserName(currentUserEmail);
            String imageUri = dbHelper.getUserImage(currentUserEmail);

            tvName.setText(name != null ? name : "TechCare User");

            // Load Image if it exists
            if (imageUri != null && !imageUri.isEmpty()) {
                try {
                    getContentResolver().takePersistableUriPermission(Uri.parse(imageUri), Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    imgProfile.setImageURI(Uri.parse(imageUri));
                } catch (Exception e) {
                    try {
                        imgProfile.setImageURI(Uri.parse(imageUri));
                    } catch (Exception ex) {
                        imgProfile.setImageResource(R.drawable.ic_default_user);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. Fetch Booking Stats
        try {
            Cursor bookings = dbHelper.getUserBookings(currentUserEmail);
            if (bookings != null) {
                tvBookingCount.setText(String.valueOf(bookings.getCount()));
                bookings.close();
            }
        } catch (Exception e) {
            if(tvBookingCount != null) tvBookingCount.setText("0");
        }
    }

    private void setupMenuOptions() {
        // --- 1. Edit Picture Logic ---
        findViewById(R.id.btn_edit_pic).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // --- 2. Edit Name Logic (Dialog) ---
        findViewById(R.id.btn_edit_name).setOnClickListener(v -> showEditNameDialog());

        // My Bookings
        findViewById(R.id.menu_my_bookings).setOnClickListener(v -> {
            startActivity(new Intent(this, MyBookingsActivity.class));
        });

        // Payment (Placeholder)
        findViewById(R.id.menu_payment).setOnClickListener(v -> {
            Toast.makeText(this, "Payment integration coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Help
        findViewById(R.id.menu_help).setOnClickListener(v -> {
            Toast.makeText(this, "Support Center coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Logout
        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            prefs.edit().clear().apply();

            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    // Dialog for Editing Name
    private void showEditNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Username");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setText(tvName.getText().toString());
        input.setPadding(50, 40, 50, 40);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                updateNameInDatabase(newName);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateNameInDatabase(String newName) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            // Executing raw update to avoid changing DatabaseHelper file if possible
            db.execSQL("UPDATE users SET name = ? WHERE email = ?", new Object[]{newName, currentUserEmail});
            tvName.setText(newName);
            Toast.makeText(this, "Username updated!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to update name", Toast.LENGTH_SHORT).show();
        }
    }

    // Handle Image Selection Result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            try {
                // Persist permission
                getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // Update DB
                boolean success = dbHelper.updateUserImage(currentUserEmail, imageUri.toString());

                if (success) {
                    imgProfile.setImageURI(imageUri);
                    Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to save image.", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error setting image.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_profile);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) return true;

            Class<?> targetClass = null;
            if (id == R.id.nav_home) targetClass = HomeActivity.class;
            else if (id == R.id.nav_bookings) targetClass = MyBookingsActivity.class;
            else if (id == R.id.nav_services) targetClass = ServicesActivity.class;

            if (targetClass != null) {
                startActivity(new Intent(this, targetClass));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }
}