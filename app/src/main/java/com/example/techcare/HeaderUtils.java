package com.example.techcare;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.TextView;
import java.util.Calendar;

public class HeaderUtils {

    // This static method can be called from ANY activity (Home, Booking, etc.)
    public static void setupHeader(Activity activity) {
        // 1. Find the TextView inside the header layout
        TextView welcomeText = activity.findViewById(R.id.tv_welcome_title);

        // If the header isn't included in this activity, stop safely
        if (welcomeText == null) return;

        // 2. Get User Email from Session
        SharedPreferences prefs = activity.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String email = prefs.getString("email", "");

        // 3. Get Full Name from Database
        DatabaseHelper db = new DatabaseHelper(activity);
        String fullName = db.getUserName(email);

        // Extract First Name (e.g., "Senira Mendis" -> "Senira")
        String firstName = fullName;
        if (fullName.contains(" ")) {
            firstName = fullName.split(" ")[0];
        }

        // 4. Calculate Time of Day for Greeting
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        String greeting;

        if (timeOfDay >= 0 && timeOfDay < 12) {
            greeting = "Good Morning";
        } else if (timeOfDay >= 12 && timeOfDay < 16) {
            greeting = "Good Afternoon";
        } else {
            greeting = "Good Evening";
        }

        // 5. Set the final text
        welcomeText.setText(greeting + ", " + firstName);
    }
}