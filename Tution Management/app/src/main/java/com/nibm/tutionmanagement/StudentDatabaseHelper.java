package com.nibm.tutionmanagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class StudentDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "StudentDBHelper";

    private static final String DATABASE_NAME = "TutionDB1.db";
    private static final int DATABASE_VERSION = 10;  // bumped from 8 to 9

    private static final String TABLE_STUDENTS = "students";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_DOB = "dob";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PARENT_PHONE = "parentPhone";
    private static final String COLUMN_PARENT_EMAIL = "parentEmail";
    private static final String COLUMN_ROLE = "role";

    public StudentDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "StudentDatabaseHelper instantiated.");
        this.getWritableDatabase(); // FORCE creation

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_STUDENTS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_STUDENTS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_NAME + " TEXT," +
                COLUMN_ADDRESS + " TEXT," +
                COLUMN_DOB + " TEXT," +
                COLUMN_PHONE + " TEXT," +
                COLUMN_EMAIL + " TEXT," +
                COLUMN_PARENT_PHONE + " TEXT," +
                COLUMN_PARENT_EMAIL + " TEXT," +
                COLUMN_ROLE + " TEXT DEFAULT 'Student')";
        db.execSQL(CREATE_STUDENTS_TABLE);
        Log.d(TAG, "Table created with query: " + CREATE_STUDENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ". Dropping old table.");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDENTS);
        onCreate(db);
    }
    public int getStudentCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_STUDENTS, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    // Insert student
    public boolean addStudent(StudentDB student) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, student.getName());
        values.put(COLUMN_ADDRESS, student.getAddress());
        values.put(COLUMN_DOB, student.getDob());
        values.put(COLUMN_PHONE, student.getPhone());
        values.put(COLUMN_EMAIL, student.getEmail());
        values.put(COLUMN_PARENT_PHONE, student.getParentPhone());
        values.put(COLUMN_PARENT_EMAIL, student.getParentEmail());

        String role = student.getRole() != null ? student.getRole() : "Student";
        values.put(COLUMN_ROLE, role);

        Log.d(TAG, "Inserting student with values: " + values.toString());

        long result = db.insert(TABLE_STUDENTS, null, values);
        Log.d(TAG, "Insert result: " + result);

        db.close();
        return result != -1;
    }
    public String getNameByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM students WHERE email = ?", new String[]{email});
        String name = "Student";
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return name;
    }


    // Get student ID by email
    public long getStudentIdByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return -1;
        }
        SQLiteDatabase db = this.getReadableDatabase();
        long studentId = -1;
        String query = "SELECT " + COLUMN_ID + " FROM " + TABLE_STUDENTS + " WHERE " + COLUMN_EMAIL + " = ? LIMIT 1";
        Cursor cursor = db.rawQuery(query, new String[]{email.trim()});

        if (cursor.moveToFirst()) {
            studentId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
        }

        cursor.close();
        db.close();
        return studentId;
    }

    // Get all students
    public ArrayList<StudentDB> getAllStudents() {
        ArrayList<StudentDB> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_STUDENTS, null);

        if (cursor.moveToFirst()) {
            do {
                StudentDB student = new StudentDB(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOB)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PARENT_PHONE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PARENT_EMAIL))
                );
                list.add(student);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return list;
    }

    // Get a single student by ID
    public StudentDB getStudentById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_STUDENTS, null, COLUMN_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            StudentDB student = new StudentDB(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOB)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PARENT_PHONE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PARENT_EMAIL))
            );
            cursor.close();
            db.close();
            return student;
        }

        if (cursor != null) cursor.close();
        db.close();
        return null;
    }

    // Update student
    public boolean updateStudent(StudentDB student) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, student.getName());
        values.put(COLUMN_ADDRESS, student.getAddress());
        values.put(COLUMN_DOB, student.getDob());
        values.put(COLUMN_PHONE, student.getPhone());
        values.put(COLUMN_EMAIL, student.getEmail());
        values.put(COLUMN_PARENT_PHONE, student.getParentPhone());
        values.put(COLUMN_PARENT_EMAIL, student.getParentEmail());

        int rows = db.update(TABLE_STUDENTS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(student.getId())});
        db.close();
        return rows > 0;
    }

    // Delete student
    public boolean deleteStudent(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_STUDENTS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows > 0;
    }

    // Check if email exists
    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT 1 FROM " + TABLE_STUDENTS + " WHERE " + COLUMN_EMAIL + " = ? LIMIT 1";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        boolean exists = cursor.moveToFirst();

        cursor.close();
        db.close();

        return exists;
    }

    // Get student names with IDs for display (e.g., spinner)
    public List<String> getStudentNamesWithId() {
        List<String> studentList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_ID + ", " + COLUMN_NAME + " FROM " + TABLE_STUDENTS + " ORDER BY " + COLUMN_NAME + " ASC";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                studentList.add(id + " - " + name);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return studentList;
    }

    // Add student with minimal info: name only
    public boolean addStudent(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name.trim());
        // Other fields left blank/default
        long result = db.insert(TABLE_STUDENTS, null, values);
        db.close();
        return result != -1;
    }
}
