package com.example.techcare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;

public class HeaderUtils {

    public static void setupHeader(Activity activity) {
        TextView welcomeText = activity.findViewById(R.id.tv_welcome_title);
        ImageView profileIcon = activity.findViewById(R.id.icon_profile);

        // Safety check: stop if views aren't found
        if (welcomeText == null || profileIcon == null) return;

        SharedPreferences prefs = activity.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String email = prefs.getString("email", "");

        DatabaseHelper db = new DatabaseHelper(activity);
        String fullName = db.getUserName(email);
        String imageUriString = db.getUserImage(email);

        // --- NEW CRASH-PROOF CODE STARTS HERE ---
        String firstName = "User";

        // This block handles empty names, null names, and names that are just spaces
        if (fullName != null) {
            String trimmedName = fullName.trim();
            if (!trimmedName.isEmpty()) {
                // We use a regex "\\s+" to split by ANY whitespace safely
                String[] parts = trimmedName.split("\\s+");
                if (parts.length > 0) {
                    firstName = parts[0];
                }
            }
        }
        // --- NEW CODE ENDS HERE ---

        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (timeOfDay >= 0 && timeOfDay < 12) greeting = "Good Morning";
        else if (timeOfDay >= 12 && timeOfDay < 16) greeting = "Good Afternoon";
        else greeting = "Good Evening";

        welcomeText.setText(greeting + ", " + firstName);

        // Image Logic
        if (imageUriString != null && !imageUriString.isEmpty()) {
            try {
                profileIcon.setImageURI(Uri.parse(imageUriString));
            } catch (Exception e) {
                profileIcon.setImageResource(R.drawable.ic_default_user);
            }
        } else {
            profileIcon.setImageResource(R.drawable.ic_default_user);
        }

        // Dropdown Logic
        profileIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(activity, profileIcon);
            popup.getMenuInflater().inflate(R.menu.header_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_profile) {
                    activity.startActivity(new Intent(activity, ProfileActivity.class));
                    return true;
                } else if (id == R.id.menu_bookings) {
                    Toast.makeText(activity, "My Bookings", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.menu_logout) {
                    logout(activity);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    private static void logout(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(activity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}
