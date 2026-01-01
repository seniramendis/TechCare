// File: app/src/main/java/com/example/techcare/DatabaseHelper.java
package com.example.techcare;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TechCare.db";
    // [CHANGED] Increment version to 6
    private static final int DATABASE_VERSION = 6;

    // Users Table
    private static final String TABLE_USERS = "users";
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_EMAIL = "email";
    private static final String COL_PASSWORD = "password";
    private static final String COL_IMAGE = "profile_image";

    // Bookings Table
    private static final String TABLE_BOOKINGS = "bookings";
    private static final String COL_BOOKING_ID = "booking_id";
    private static final String COL_USER_EMAIL = "user_email";
    private static final String COL_DEVICE = "device_type";
    private static final String COL_ISSUE = "issue_description";
    private static final String COL_TYPE = "service_type";
    private static final String COL_STATUS = "status";
    private static final String COL_BOOKING_IMAGE = "booking_image";
    // [NEW] Scheduling Columns
    private static final String COL_SCHEDULE_DATE = "scheduled_date";
    private static final String COL_SCHEDULE_TIME = "scheduled_time";

    // Reviews Table
    private static final String TABLE_REVIEWS = "reviews";
    private static final String COL_REVIEW_ID = "review_id";
    private static final String COL_REVIEW_EMAIL = "review_email";
    private static final String COL_REVIEW_RATING = "rating";
    private static final String COL_REVIEW_COMMENT = "comment";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsers = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT, " +
                COL_EMAIL + " TEXT UNIQUE, " +
                COL_PASSWORD + " TEXT, " +
                COL_IMAGE + " TEXT)";
        db.execSQL(createUsers);

        // [CHANGED] Add date/time columns to create statement
        String createBookings = "CREATE TABLE " + TABLE_BOOKINGS + " (" +
                COL_BOOKING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_EMAIL + " TEXT, " +
                COL_DEVICE + " TEXT, " +
                COL_ISSUE + " TEXT, " +
                COL_TYPE + " TEXT, " +
                COL_STATUS + " TEXT, " +
                COL_BOOKING_IMAGE + " TEXT, " +
                COL_SCHEDULE_DATE + " TEXT, " +
                COL_SCHEDULE_TIME + " TEXT)";
        db.execSQL(createBookings);

        String createReviews = "CREATE TABLE " + TABLE_REVIEWS + " (" +
                COL_REVIEW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_REVIEW_EMAIL + " TEXT, " +
                COL_REVIEW_RATING + " INTEGER, " +
                COL_REVIEW_COMMENT + " TEXT)";
        db.execSQL(createReviews);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // ... existing upgrades ...
        if (oldVersion < 4) {
            String createReviews = "CREATE TABLE " + TABLE_REVIEWS + " (" +
                    COL_REVIEW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_REVIEW_EMAIL + " TEXT, " +
                    COL_REVIEW_RATING + " INTEGER, " +
                    COL_REVIEW_COMMENT + " TEXT)";
            db.execSQL(createReviews);
        }
        if (oldVersion < 5) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_BOOKINGS + " ADD COLUMN " + COL_BOOKING_IMAGE + " TEXT");
            } catch (Exception e) {}
        }
        // [NEW] Upgrade to Version 6
        if (oldVersion < 6) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_BOOKINGS + " ADD COLUMN " + COL_SCHEDULE_DATE + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_BOOKINGS + " ADD COLUMN " + COL_SCHEDULE_TIME + " TEXT");
            } catch (Exception e) {}
        }
    }

    // ... User Methods ... (Keep as is)
    public boolean insertUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_NAME, name);
        contentValues.put(COL_EMAIL, email);
        contentValues.put(COL_PASSWORD, password);
        contentValues.put(COL_IMAGE, "");
        long result = db.insert(TABLE_USERS, null, contentValues);
        return result != -1;
    }
    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + " = ? AND " + COL_PASSWORD + " = ?", new String[]{email, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
    @SuppressLint("Range")
    public String getUserName(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String name = "User";
        Cursor cursor = db.rawQuery("SELECT " + COL_NAME + " FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + " = ?", new String[]{email});
        if (cursor.moveToFirst()) { name = cursor.getString(cursor.getColumnIndex(COL_NAME)); }
        cursor.close();
        return name;
    }
    @SuppressLint("Range")
    public String getUserImage(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String imageUri = "";
        Cursor cursor = db.rawQuery("SELECT " + COL_IMAGE + " FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + " = ?", new String[]{email});
        if (cursor.moveToFirst()) { imageUri = cursor.getString(cursor.getColumnIndex(COL_IMAGE)); }
        cursor.close();
        return imageUri;
    }
    public boolean updateUserImage(String email, String imageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_IMAGE, imageUri);
        return db.update(TABLE_USERS, cv, COL_EMAIL + " = ?", new String[]{email}) > 0;
    }
    public boolean checkEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + " = ?", new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
    public boolean updatePassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_PASSWORD, newPassword);
        return db.update(TABLE_USERS, contentValues, COL_EMAIL + " = ?", new String[]{email}) > 0;
    }

    // --- BOOKING METHODS ---
    // [CHANGED] Added date and time parameters
    public boolean addBooking(String email, String device, String issue, String type, String imageUri, String date, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USER_EMAIL, email);
        cv.put(COL_DEVICE, device);
        cv.put(COL_ISSUE, issue);
        cv.put(COL_TYPE, type);
        cv.put(COL_STATUS, "Received");
        cv.put(COL_BOOKING_IMAGE, imageUri);
        cv.put(COL_SCHEDULE_DATE, date); // [NEW]
        cv.put(COL_SCHEDULE_TIME, time); // [NEW]
        long result = db.insert(TABLE_BOOKINGS, null, cv);
        return result != -1;
    }

    public Cursor getUserBookings(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_BOOKINGS +
                        " WHERE " + COL_USER_EMAIL + " = ? ORDER BY " + COL_BOOKING_ID + " DESC",
                new String[]{email});
    }

    public boolean updateBookingStatus(int bookingId, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_STATUS, newStatus);
        return db.update(TABLE_BOOKINGS, cv, COL_BOOKING_ID + " = ?", new String[]{String.valueOf(bookingId)}) > 0;
    }

    // ... Review Methods (Keep as is) ...
    public boolean addReview(String email, int rating, String comment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_REVIEW_EMAIL, email);
        cv.put(COL_REVIEW_RATING, rating);
        cv.put(COL_REVIEW_COMMENT, comment);
        long result = db.insert(TABLE_REVIEWS, null, cv);
        return result != -1;
    }
    @SuppressLint("Range")
    public List<Review> getAllReviews() {
        List<Review> reviewList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT r." + COL_REVIEW_COMMENT + ", r." + COL_REVIEW_RATING + ", u." + COL_NAME + ", u." + COL_IMAGE + " FROM " + TABLE_REVIEWS + " r " + " LEFT JOIN " + TABLE_USERS + " u ON r." + COL_REVIEW_EMAIL + " = u." + COL_EMAIL + " ORDER BY r." + COL_REVIEW_ID + " DESC";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                String comment = cursor.getString(cursor.getColumnIndex(COL_REVIEW_COMMENT));
                int rating = cursor.getInt(cursor.getColumnIndex(COL_REVIEW_RATING));
                String name = cursor.getString(cursor.getColumnIndex(COL_NAME));
                String image = cursor.getString(cursor.getColumnIndex(COL_IMAGE));
                if (name == null) name = "Anonymous User";
                reviewList.add(new Review(comment, name, rating, image));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return reviewList;
    }
}