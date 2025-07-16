package com.nibm.tutionmanagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class TeacherDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "TeacherDBHelper";

    private static final String DATABASE_NAME = "TutionDB1.db";
    private static final int DATABASE_VERSION = 10;

    private static final String TABLE_TEACHERS = "teachers";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_DOB = "dob";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_ROLE = "role";

    public TeacherDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "TeacherDatabaseHelper instantiated.");
        SQLiteDatabase db = this.getWritableDatabase();

        // Forcefully create table if it doesn't exist
        if (!doesTableExist(db)) {
            Log.w(TAG, "Table 'teachers' missing — creating manually...");
            createTeachersTable(db);
        } else {
            Log.d(TAG, "Table 'teachers' exists.");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTeachersTable(db);
    }

    private void createTeachersTable(SQLiteDatabase db) {
        String CREATE_TEACHERS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_TEACHERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_ADDRESS + " TEXT, " +
                COLUMN_DOB + " TEXT, " +
                COLUMN_PHONE + " TEXT, " +
                COLUMN_EMAIL + " TEXT, " +
                COLUMN_ROLE + " TEXT DEFAULT 'Teacher')";
        db.execSQL(CREATE_TEACHERS_TABLE);
        Log.i(TAG, "Created table 'teachers'.");
    }


    public int getTeacherCount() {
        int count = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TEACHERS, null);
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading DB from version " + oldVersion + " to " + newVersion + ", dropping table...");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEACHERS);
        onCreate(db);
    }

    private boolean doesTableExist(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{TABLE_TEACHERS});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public List<String> getTeacherNamesWithEmailByIds(List<Integer> teacherIds) {
        List<String> teacherList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        for (int id : teacherIds) {
            Cursor cursor = db.rawQuery(
                    "SELECT " + COLUMN_NAME + ", " + COLUMN_EMAIL + " FROM " + TABLE_TEACHERS + " WHERE " + COLUMN_ID + " = ?",
                    new String[]{String.valueOf(id)}
            );

            if (cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL));
                teacherList.add(name + " (" + email + ")");
            }
            cursor.close();
        }

        db.close();
        return teacherList;
    }

    public String getTeacherNameWithEmailById(int teacherId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String result = "";

        Cursor cursor = db.rawQuery(
                "SELECT " + COLUMN_NAME + ", " + COLUMN_EMAIL + " FROM " + TABLE_TEACHERS + " WHERE " + COLUMN_ID + " = ?",
                new String[]{String.valueOf(teacherId)}
        );

        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL));
            result = name + " (" + email + ")";
        }
        cursor.close();
        db.close();

        return result;
    }

    public boolean addTeacher(TeacherDB teacher) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, teacher.getName());
        values.put(COLUMN_ADDRESS, teacher.getAddress());
        values.put(COLUMN_DOB, teacher.getDob());
        values.put(COLUMN_PHONE, teacher.getPhone());
        values.put(COLUMN_EMAIL, teacher.getEmail());
        values.put(COLUMN_ROLE, teacher.getRole() != null ? teacher.getRole() : "Teacher");

        long result = db.insert(TABLE_TEACHERS, null, values);
        db.close();
        return result != -1;
    }

    public ArrayList<TeacherDB> getAllTeachers() {
        ArrayList<TeacherDB> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TEACHERS, null);

        if (cursor.moveToFirst()) {
            do {
                TeacherDB teacher = new TeacherDB(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOB)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE))
                );
                list.add(teacher);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return list;
    }

    public boolean updateTeacher(TeacherDB teacher) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, teacher.getName());
        values.put(COLUMN_ADDRESS, teacher.getAddress());
        values.put(COLUMN_DOB, teacher.getDob());
        values.put(COLUMN_PHONE, teacher.getPhone());
        values.put(COLUMN_EMAIL, teacher.getEmail());
        values.put(COLUMN_ROLE, teacher.getRole());

        int rows = db.update(TABLE_TEACHERS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(teacher.getId())});
        db.close();
        return rows > 0;
    }

    public boolean deleteTeacher(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_TEACHERS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows > 0;
    }
    public String getNameByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM teachers WHERE email = ?", new String[]{email});
        String name = "Teacher";
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return name;
    }


    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + TABLE_TEACHERS + " WHERE " + COLUMN_EMAIL + " = ? LIMIT 1", new String[]{email});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exists;
    }

    public long getTeacherIdByEmail(String email) {
        long teacherId = -1;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TEACHERS, new String[]{COLUMN_ID}, COLUMN_EMAIL + " = ?", new String[]{email}, null, null, null);
        if (cursor.moveToFirst()) {
            teacherId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
        }
        cursor.close();
        db.close();
        return teacherId;
    }

    public List<String> getTeacherNamesWithEmail() {
        List<String> teacherList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_NAME + ", " + COLUMN_EMAIL + " FROM " + TABLE_TEACHERS + " ORDER BY " + COLUMN_NAME + " ASC", null);

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL));
                teacherList.add(name + " (" + email + ")");
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return teacherList;
    }

    public boolean addTeacher(String name, String email) {
        if (name == null || name.trim().isEmpty()) return false;
        if (email == null || email.trim().isEmpty()) return false;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name.trim());
        values.put(COLUMN_EMAIL, email.trim());
        values.put(COLUMN_ROLE, "Teacher");

        long result = db.insert(TABLE_TEACHERS, null, values);
        db.close();
        return result != -1;
    }
}
