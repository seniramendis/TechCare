package com.example.techcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private DatabaseHelper dbHelper;
    private TextView tvName, tvEmail, tvBookingCount, tvNoDevices;
    private ImageView imgProfile;
    private RecyclerView recyclerDevices;
    private String currentUserEmail;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        if (dbHelper.updateUserImage(currentUserEmail, imageUri.toString())) {
                            imgProfile.setImageURI(imageUri);
                            Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to update profile picture.", e);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUserEmail = prefs.getString("email", "Guest");

        if (currentUserEmail.equals("Guest")) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        tvName = findViewById(R.id.tv_profile_name);
        tvEmail = findViewById(R.id.tv_profile_email);
        tvBookingCount = findViewById(R.id.tv_stat_bookings);
        imgProfile = findViewById(R.id.img_profile_pic);

        Button btnAddDevice = findViewById(R.id.btn_add_device);
        recyclerDevices = findViewById(R.id.recycler_devices);
        tvNoDevices = findViewById(R.id.tv_no_devices);

        HeaderUtils.setupHeader(this);
        setupBottomNav();
        setupMenuOptions();

        loadUserProfile();
        setupDevicesList();

        btnAddDevice.setOnClickListener(v -> showAddDeviceDialog());
    }

    private void loadUserProfile() {
        tvEmail.setText(currentUserEmail);
        try {
            String name = dbHelper.getUserName(currentUserEmail);
            String imageUri = dbHelper.getUserImage(currentUserEmail);
            tvName.setText(name != null ? name : "TechCare User");

            if (imageUri != null && !imageUri.isEmpty()) {
                try {
                    imgProfile.setImageURI(Uri.parse(imageUri));
                } catch (Exception e) {
                    Log.e(TAG, "Failed to load profile image.", e);
                    imgProfile.setImageResource(R.drawable.ic_default_user);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load user profile.", e);
        }

        try (Cursor bookings = dbHelper.getUserBookings(currentUserEmail)) {
            if (bookings != null) {
                tvBookingCount.setText(String.valueOf(bookings.getCount()));
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load booking count.", e);
            if (tvBookingCount != null) tvBookingCount.setText("0");
        }
    }

    private void showAddDeviceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Device");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etName = new EditText(this);
        etName.setHint("Device Name (e.g. My Phone)");
        etName.setTextColor(android.graphics.Color.BLACK);
        layout.addView(etName);

        final EditText etModel = new EditText(this);
        etModel.setHint("Model (e.g. Samsung S21)");
        etModel.setTextColor(android.graphics.Color.BLACK);
        layout.addView(etModel);

        builder.setView(layout);

        builder.setPositiveButton("Save", (d, w) -> {
            String name = etName.getText().toString().trim();
            String model = etModel.getText().toString().trim();
            if(!name.isEmpty() && !model.isEmpty()) {
                dbHelper.addSavedDevice(currentUserEmail, name, model);
                setupDevicesList();
                Toast.makeText(this, "Device Saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (d, w) -> d.dismiss());
        builder.show();
    }

    private void setupDevicesList() {
        List<DeviceModel> list = new ArrayList<>();
        try (Cursor cursor = dbHelper.getSavedDevices(currentUserEmail)) {
            if (cursor.moveToFirst()) {
                do {
                    list.add(new DeviceModel(cursor.getInt(0), cursor.getString(2), cursor.getString(3)));
                } while (cursor.moveToNext());
            }
        }

        if (list.isEmpty()) {
            tvNoDevices.setVisibility(View.VISIBLE);
            recyclerDevices.setVisibility(View.GONE);
        } else {
            tvNoDevices.setVisibility(View.GONE);
            recyclerDevices.setVisibility(View.VISIBLE);
            recyclerDevices.setLayoutManager(new LinearLayoutManager(this));
            recyclerDevices.setAdapter(new DeviceAdapter(list));
        }
    }

    private void setupMenuOptions() {
        findViewById(R.id.btn_edit_pic).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        findViewById(R.id.btn_edit_name).setOnClickListener(v -> showEditNameDialog());

        findViewById(R.id.menu_my_bookings).setOnClickListener(v -> startActivity(new Intent(this, MyBookingsActivity.class)));
        findViewById(R.id.menu_payment).setOnClickListener(v -> Toast.makeText(this, "Payment integration coming soon!", Toast.LENGTH_SHORT).show());
        findViewById(R.id.menu_help).setOnClickListener(v -> Toast.makeText(this, "Support Center coming soon!", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            prefs.edit().clear().apply();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

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
                try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
                    db.execSQL("UPDATE users SET name = ? WHERE email = ?", new Object[]{newName, currentUserEmail});
                    tvName.setText(newName);
                    Toast.makeText(this, "Username updated!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e(TAG, "Failed to update username.", e);
                }
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
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

    // --- Inner Classes for Device List ---
    static class DeviceModel {
        int id; String name, model;
        DeviceModel(int id, String name, String model) { this.id = id; this.name = name; this.model = model; }
    }

    class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.Holder> {
        List<DeviceModel> list;
        DeviceAdapter(List<DeviceModel> list) { this.list = list; }
        @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup p, int v) {
            return new Holder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_saved_device, p, false));
        }
        @Override public void onBindViewHolder(@NonNull Holder h, int i) {
            DeviceModel item = list.get(i);
            h.tvName.setText(item.name);
            h.tvModel.setText(item.model);
            h.btnDelete.setOnClickListener(v -> {
                dbHelper.deleteDevice(item.id);
                setupDevicesList();
                Toast.makeText(ProfileActivity.this, "Device Removed", Toast.LENGTH_SHORT).show();
            });
        }
        @Override public int getItemCount() { return list.size(); }
        class Holder extends RecyclerView.ViewHolder {
            TextView tvName, tvModel; ImageButton btnDelete;
            public Holder(@NonNull View v) {
                super(v);
                tvName = v.findViewById(R.id.tv_device_name);
                tvModel = v.findViewById(R.id.tv_device_model);
                btnDelete = v.findViewById(R.id.btn_delete_device);
            }
        }
    }
}
