package com.example.techcare;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TechCare.db";
    private static final int DATABASE_VERSION = 10; // Updated Version

    // Users Table
    private static final String TABLE_USERS = "users";
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_EMAIL = "email";
    private static final String COL_PASSWORD = "password";
    private static final String COL_IMAGE = "profile_image";
    private static final String COL_PHONE = "phone";

    // Bookings Table
    private static final String TABLE_BOOKINGS = "bookings";
    private static final String COL_BOOKING_ID = "booking_id";
    private static final String COL_USER_EMAIL = "user_email";
    private static final String COL_DEVICE = "device_type";
    private static final String COL_ISSUE = "issue_description";
    private static final String COL_TYPE = "service_type";
    private static final String COL_STATUS = "status";
    private static final String COL_BOOKING_IMAGE = "booking_image";
    private static final String COL_SCHEDULE_DATE = "scheduled_date";
    private static final String COL_SCHEDULE_TIME = "scheduled_time";
    private static final String COL_TECHNICIAN = "technician_name";
    // [NEW] Column to track if we already notified the user
    private static final String COL_NOTIFIED = "is_notified";

    // Reviews Table
    private static final String TABLE_REVIEWS = "reviews";
    private static final String COL_REVIEW_ID = "review_id";
    private static final String COL_REVIEW_EMAIL = "review_email";
    private static final String COL_REVIEW_RATING = "rating";
    private static final String COL_REVIEW_COMMENT = "comment";

    // Saved Devices Table
    private static final String TABLE_DEVICES = "saved_devices";
    private static final String COL_DEV_ID = "device_id";
    private static final String COL_DEV_EMAIL = "user_email";
    private static final String COL_DEV_NAME = "device_name";
    private static final String COL_DEV_MODEL = "device_model";

    private Context mContext;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsers = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT, " +
                COL_EMAIL + " TEXT UNIQUE, " +
                COL_PHONE + " TEXT, " +
                COL_PASSWORD + " TEXT, " +
                COL_IMAGE + " TEXT)";
        db.execSQL(createUsers);

        // [CHANGE] Added COL_NOTIFIED to creation string
        String createBookings = "CREATE TABLE " + TABLE_BOOKINGS + " (" +
                COL_BOOKING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_EMAIL + " TEXT, " +
                COL_DEVICE + " TEXT, " +
                COL_ISSUE + " TEXT, " +
                COL_TYPE + " TEXT, " +
                COL_STATUS + " TEXT, " +
                COL_BOOKING_IMAGE + " TEXT, " +
                COL_SCHEDULE_DATE + " TEXT, " +
                COL_SCHEDULE_TIME + " TEXT, " +
                COL_TECHNICIAN + " TEXT, " +
                COL_NOTIFIED + " INTEGER DEFAULT 0)";
        db.execSQL(createBookings);

        String createReviews = "CREATE TABLE " + TABLE_REVIEWS + " (" +
                COL_REVIEW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_REVIEW_EMAIL + " TEXT, " +
                COL_REVIEW_RATING + " INTEGER, " +
                COL_REVIEW_COMMENT + " TEXT)";
        db.execSQL(createReviews);

        String createDevices = "CREATE TABLE " + TABLE_DEVICES + " (" +
                COL_DEV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DEV_EMAIL + " TEXT, " +
                COL_DEV_NAME + " TEXT, " +
                COL_DEV_MODEL + " TEXT)";
        db.execSQL(createDevices);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 8) {
            try { db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COL_PHONE + " TEXT"); }
            catch (Exception e) { Log.e("DatabaseHelper", "Error upgrading to v8", e); }
        }

        if (oldVersion < 9) {
            try {
                String createDevices = "CREATE TABLE " + TABLE_DEVICES + " (" +
                        COL_DEV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_DEV_EMAIL + " TEXT, " +
                        COL_DEV_NAME + " TEXT, " +
                        COL_DEV_MODEL + " TEXT)";
                db.execSQL(createDevices);
            } catch (Exception e) { Log.e("DatabaseHelper", "Error upgrading to v9", e); }
        }

        // [CHANGE] Upgrade for version 10: Add Notified Column
        if (oldVersion < 10) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_BOOKINGS + " ADD COLUMN " + COL_NOTIFIED + " INTEGER DEFAULT 0");
            } catch (Exception e) { Log.e("DatabaseHelper", "Error upgrading to v10", e); }
        }
    }

    // --- USER METHODS ---
    public boolean insertUser(String name, String email, String phone, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_NAME, name);
        contentValues.put(COL_EMAIL, email);
        contentValues.put(COL_PHONE, phone);
        contentValues.put(COL_PASSWORD, password);
        contentValues.put(COL_IMAGE, "");
        return db.insert(TABLE_USERS, null, contentValues) != -1;
    }

    public boolean checkUser(String identifier, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE (" +
                COL_EMAIL + " = ? OR " + COL_PHONE + " = ?) AND " +
                COL_PASSWORD + " = ?", new String[]{identifier, identifier, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    @SuppressLint("Range")
    public String getEmailFromIdentifier(String identifier) {
        SQLiteDatabase db = this.getReadableDatabase();
        String email = identifier;
        Cursor cursor = db.rawQuery("SELECT " + COL_EMAIL + " FROM " + TABLE_USERS +
                        " WHERE " + COL_EMAIL + " = ? OR " + COL_PHONE + " = ?",
                new String[]{identifier, identifier});
        if (cursor.moveToFirst()) {
            email = cursor.getString(cursor.getColumnIndex(COL_EMAIL));
        }
        cursor.close();
        return email;
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
    public boolean addBooking(String email, String device, String issue, String type, String imageUri, String date, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USER_EMAIL, email);
        cv.put(COL_DEVICE, device);
        cv.put(COL_ISSUE, issue);
        cv.put(COL_TYPE, type);
        cv.put(COL_STATUS, "Received");
        cv.put(COL_BOOKING_IMAGE, imageUri);
        cv.put(COL_SCHEDULE_DATE, date);
        cv.put(COL_SCHEDULE_TIME, time);
        cv.put(COL_NOTIFIED, 0); // Notified = False initially

        // [CHANGE] Sri Lankan Technician Names
        String[] techs = {
                "Kasun Perera",
                "Nuwan Silva",
                "Chamara Bandara",
                "Amila Fernando",
                "Ruwan Kumara",
                "Sanjaya Herath"
        };
        cv.put(COL_TECHNICIAN, techs[new Random().nextInt(techs.length)]);

        return db.insert(TABLE_BOOKINGS, null, cv) != -1;
    }

    public Cursor getUserBookings(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_BOOKINGS + " WHERE " + COL_USER_EMAIL + " = ? ORDER BY " + COL_BOOKING_ID + " DESC", new String[]{email});
    }

    // [CRITICAL FIX] Logic to prevent spam notifications
    public boolean updateBookingStatus(int bookingId, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();

        // 1. Check the CURRENT status in the database first
        String currentStatus = "";
        Cursor cursor = db.rawQuery("SELECT " + COL_STATUS + " FROM " + TABLE_BOOKINGS + " WHERE " + COL_BOOKING_ID + " = ?", new String[]{String.valueOf(bookingId)});
        if (cursor.moveToFirst()) {
            currentStatus = cursor.getString(0);
        }
        cursor.close();

        // 2. Prepare update
        ContentValues cv = new ContentValues();
        cv.put(COL_STATUS, newStatus);

        // If status is NOT Completed, reset notification flag so we can notify again later
        if (!"Completed".equalsIgnoreCase(newStatus)) {
            cv.put(COL_NOTIFIED, 0);
        }

        int rows = db.update(TABLE_BOOKINGS, cv, COL_BOOKING_ID + " = ?", new String[]{String.valueOf(bookingId)});

        // 3. Only Notify if:
        //    a) The update was successful (rows > 0)
        //    b) The NEW status is "Completed"
        //    c) The OLD status was NOT "Completed" (Meaning it JUST changed)
        if (rows > 0 && "Completed".equalsIgnoreCase(newStatus)) {
            if (!"Completed".equalsIgnoreCase(currentStatus)) {
                sendCompletionNotification(bookingId);
                markAsNotified(bookingId);
            }
        }

        return rows > 0;
    }

    private void markAsNotified(int bookingId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_NOTIFIED, 1);
        db.update(TABLE_BOOKINGS, cv, COL_BOOKING_ID + " = ?", new String[]{String.valueOf(bookingId)});
    }

    @SuppressLint("Range")
    private void sendCompletionNotification(int bookingId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String deviceName = "Device";
        Cursor cursor = db.rawQuery("SELECT " + COL_DEVICE + " FROM " + TABLE_BOOKINGS + " WHERE " + COL_BOOKING_ID + " = ?", new String[]{String.valueOf(bookingId)});
        if (cursor.moveToFirst()) {
            deviceName = cursor.getString(0);
        }
        cursor.close();

        NotificationHelper.sendBookingNotification(
                mContext,
                "Ready for Pickup",
                "Your " + deviceName + " is repaired and ready for pickup!"
        );
    }

    // --- REVIEW METHODS ---
    public boolean addReview(String email, int rating, String comment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_REVIEW_EMAIL, email);
        cv.put(COL_REVIEW_RATING, rating);
        cv.put(COL_REVIEW_COMMENT, comment);
        return db.insert(TABLE_REVIEWS, null, cv) != -1;
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

    // --- SAVED DEVICES METHODS ---
    public boolean addSavedDevice(String email, String name, String model) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_DEV_EMAIL, email);
        cv.put(COL_DEV_NAME, name);
        cv.put(COL_DEV_MODEL, model);
        return db.insert(TABLE_DEVICES, null, cv) != -1;
    }

    public Cursor getSavedDevices(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_DEVICES + " WHERE " + COL_DEV_EMAIL + " = ?", new String[]{email});
    }

    public void deleteDevice(int deviceId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DEVICES, COL_DEV_ID + " = ?", new String[]{String.valueOf(deviceId)});
    }
}