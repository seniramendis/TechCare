package com.example.techcare;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    ImageView imgProfile;
    TextView tvName, tvEmail;
    DatabaseHelper db;
    String userEmail;
    Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Include Header Logic
        HeaderUtils.setupHeader(this);

        db = new DatabaseHelper(this);
        imgProfile = findViewById(R.id.img_profile_large);
        tvName = findViewById(R.id.tv_profile_name);
        tvEmail = findViewById(R.id.tv_profile_email);
        Button btnSave = findViewById(R.id.btn_save_profile);

        // Get User Info
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userEmail = prefs.getString("email", "");

        String name = db.getUserName(userEmail);
        String currentImage = db.getUserImage(userEmail);

        tvName.setText(name);
        tvEmail.setText(userEmail);

        // Load existing image if available
        if (currentImage != null && !currentImage.isEmpty()) {
            try {
                imgProfile.setImageURI(Uri.parse(currentImage));
            } catch (Exception e) {
                imgProfile.setImageResource(R.drawable.ic_default_user);
            }
        } else {
            imgProfile.setImageResource(R.drawable.ic_default_user);
        }

        // Image Picker Setup
        ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();

                        // Permission to keep accessing this image
                        try {
                            getContentResolver().takePersistableUriPermission(selectedImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        imgProfile.setImageURI(selectedImageUri);
                    }
                }
        );

        imgProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // Save Button Logic
        btnSave.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                boolean success = db.updateUserImage(userEmail, selectedImageUri.toString());
                if (success) {
                    Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Failed to update image", Toast.LENGTH_SHORT).show();
                }
            } else {
                finish(); // No changes made
            }
        });
    }
}