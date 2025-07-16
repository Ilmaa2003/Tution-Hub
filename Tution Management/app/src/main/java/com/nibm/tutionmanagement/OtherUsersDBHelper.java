package com.nibm.tutionmanagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class OtherUsersDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TutionDB1.db";
    private static final int DATABASE_VERSION = 10;

    private static final String TABLE_OTHER_USERS = "otherUsers";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_ROLE = "role";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";

    public OtherUsersDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        SQLiteDatabase db = this.getWritableDatabase();

        // Force creation of table if it does not exist
        if (!doesTableExist(db, TABLE_OTHER_USERS)) {
            createOtherUsersTable(db);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createOtherUsersTable(db);
    }

    private void createOtherUsersTable(SQLiteDatabase db) {
        String CREATE_OTHER_USERS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_OTHER_USERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_ROLE + " TEXT," +
                COLUMN_EMAIL + " TEXT UNIQUE," +
                COLUMN_PASSWORD + " TEXT" +
                ")";
        db.execSQL(CREATE_OTHER_USERS_TABLE);
    }

    private boolean doesTableExist(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                new String[]{tableName});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OTHER_USERS);
        onCreate(db);
    }

    // Insert or update user record in otherUsers table
    public boolean insertOrUpdateUser(String role, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_OTHER_USERS + " WHERE email = ?", new String[]{email});
        boolean exists = cursor.moveToFirst();
        cursor.close();

        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ROLE, role);
        cv.put(COLUMN_EMAIL, email);
        cv.put(COLUMN_PASSWORD, password);

        long result;
        if (exists) {
            result = db.update(TABLE_OTHER_USERS, cv, COLUMN_EMAIL + " = ?", new String[]{email});
        } else {
            result = db.insert(TABLE_OTHER_USERS, null, cv);
        }

        db.close();
        return result != -1;
    }

    // Get user role by email
    public String getRoleByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_ROLE + " FROM " + TABLE_OTHER_USERS + " WHERE " + COLUMN_EMAIL + " = ?", new String[]{email});
        if (cursor.moveToFirst()) {
            String role = cursor.getString(0);
            cursor.close();
            db.close();
            return role;
        }
        cursor.close();
        db.close();
        return null;
    }

    // Validate user credentials in otherUsers table
    public boolean validateUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + TABLE_OTHER_USERS + " WHERE " + COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ? LIMIT 1",
                new String[]{email, password});
        boolean valid = cursor.moveToFirst();
        cursor.close();
        db.close();
        return valid;
    }
}
