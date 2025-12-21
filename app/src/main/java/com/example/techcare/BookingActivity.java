package com.example.techcare;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class BookingActivity extends AppCompatActivity {

    DatabaseHelper db;
    EditText etDevice, etIssue;
    RadioGroup rgType;
    Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        db = new DatabaseHelper(this);

        // Link Java variables to XML IDs
        etDevice = findViewById(R.id.et_device);
        etIssue = findViewById(R.id.et_issue);
        rgType = findViewById(R.id.rg_service_type);
        btnSubmit = findViewById(R.id.btn_submit_request);

        // Check if we passed a device type from the Home Screen
        String autoDevice = getIntent().getStringExtra("DEVICE_TYPE");
        if(autoDevice != null) {
            etDevice.setText(autoDevice);
        }

        // Handle Submit Button
        btnSubmit.setOnClickListener(v -> {
            String device = etDevice.getText().toString();
            String issue = etIssue.getText().toString();

            // Get selected Radio Button text
            int selectedId = rgType.getCheckedRadioButtonId();
            RadioButton selectedBtn = findViewById(selectedId);
            String type = (selectedBtn != null) ? selectedBtn.getText().toString() : "Drop-off";

            // Get User Email from Session
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String email = prefs.getString("email", "");

            if(email.isEmpty()) {
                Toast.makeText(this, "Error: You are not logged in!", Toast.LENGTH_SHORT).show();
                return;
            }

            if(device.isEmpty() || issue.isEmpty()) {
                Toast.makeText(this, "Please fill in all details", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save to Database
            boolean success = db.addBooking(email, device, issue, type);
            if(success) {
                Toast.makeText(this, "Repair Request Submitted!", Toast.LENGTH_LONG).show();
                finish(); // Go back to Home
            } else {
                Toast.makeText(this, "Failed to submit request", Toast.LENGTH_SHORT).show();
            }
        });
    }
}