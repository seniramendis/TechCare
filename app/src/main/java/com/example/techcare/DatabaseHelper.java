package com.example.techcare;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TechCare.db";
    private static final int DATABASE_VERSION = 3;

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

        String createBookings = "CREATE TABLE " + TABLE_BOOKINGS + " (" +
                COL_BOOKING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_EMAIL + " TEXT, " +
                COL_DEVICE + " TEXT, " +
                COL_ISSUE + " TEXT, " +
                COL_TYPE + " TEXT, " +
                COL_STATUS + " TEXT)";
        db.execSQL(createBookings);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKINGS);
        onCreate(db);
    }

    // --- USER METHODS ---
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
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " +
                COL_EMAIL + " = ? AND " + COL_PASSWORD + " = ?", new String[]{email, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    @SuppressLint("Range")
    public String getUserName(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String name = "User";
        Cursor cursor = db.rawQuery("SELECT " + COL_NAME + " FROM " + TABLE_USERS +
                " WHERE " + COL_EMAIL + " = ?", new String[]{email});

        if (cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndex(COL_NAME));
        }
        cursor.close();
        return name;
    }

    @SuppressLint("Range")
    public String getUserImage(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String imageUri = "";
        Cursor cursor = db.rawQuery("SELECT " + COL_IMAGE + " FROM " + TABLE_USERS +
                " WHERE " + COL_EMAIL + " = ?", new String[]{email});

        if (cursor.moveToFirst()) {
            imageUri = cursor.getString(cursor.getColumnIndex(COL_IMAGE));
        }
        cursor.close();
        return imageUri;
    }

    public boolean updateUserImage(String email, String imageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_IMAGE, imageUri);
        int rows = db.update(TABLE_USERS, cv, COL_EMAIL + " = ?", new String[]{email});
        return rows > 0;
    }

    public boolean checkEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " +
                COL_EMAIL + " = ?", new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean updatePassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_PASSWORD, newPassword);
        int rows = db.update(TABLE_USERS, contentValues, COL_EMAIL + " = ?", new String[]{email});
        return rows > 0;
    }

    public boolean addBooking(String email, String device, String issue, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USER_EMAIL, email);
        cv.put(COL_DEVICE, device);
        cv.put(COL_ISSUE, issue);
        cv.put(COL_TYPE, type);
        cv.put(COL_STATUS, "Received");
        long result = db.insert(TABLE_BOOKINGS, null, cv);
        return result != -1;
    }

    public Cursor getUserBookings(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Order by ID descending (newest first)
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
}