package com.nibm.tutionmanagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CourseScheduleDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Tution.db";
    private static final int DATABASE_VERSION = 4; // Incremented version

    private static final String TABLE_SCHEDULE = "course_schedule";

    private static final String COL_ID = "schedule_id";
    private static final String COL_COURSE_ID = "course_id";
    private static final String COL_TYPE = "schedule_type";

    private static final String COL_DAY_OF_WEEK = "day_of_week";
    private static final String COL_START_HOUR = "start_hour";
    private static final String COL_START_MIN = "start_minute";
    private static final String COL_END_HOUR = "end_hour";
    private static final String COL_END_MIN = "end_minute";
    private static final String COL_EXTRA_CLASS_DATE = "extra_class_date";

    private static final String COL_CUSTOMIZED_DATE = "customized_date";

    // New column to mark extra classes
    private static final String COL_IS_EXTRA_CLASS = "is_extra_class";

    private Context context;

    public CourseScheduleDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        ensureTableExists(getWritableDatabase());
    }

    private void ensureTableExists(SQLiteDatabase db) {
        try {
            String createTable = "CREATE TABLE IF NOT EXISTS " + TABLE_SCHEDULE + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_COURSE_ID + " INTEGER NOT NULL, " +
                    COL_TYPE + " TEXT NOT NULL, " +
                    COL_DAY_OF_WEEK + " TEXT, " +
                    COL_START_HOUR + " INTEGER, " +
                    COL_START_MIN + " INTEGER, " +
                    COL_END_HOUR + " INTEGER, " +
                    COL_END_MIN + " INTEGER, " +
                    COL_EXTRA_CLASS_DATE + " INTEGER, " +
                    COL_CUSTOMIZED_DATE + " INTEGER, " +
                    COL_IS_EXTRA_CLASS + " INTEGER DEFAULT 0" +  // Added new column
                    ");";
            db.execSQL(createTable);
        } catch (Exception e) {
            Log.e("DB_ERROR", "Error ensuring table exists: " + e.getMessage());
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        ensureTableExists(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCHEDULE);
        onCreate(db);
    }

    // Insert Fixed Schedule (including extra classes)
    public boolean insertFixedSchedule(int courseId, String dayOfWeek, int startHour, int startMinute,
                                       int endHour, int endMinute, long extraClassDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ensureTableExists(db);
        ContentValues cv = new ContentValues();
        cv.put(COL_COURSE_ID, courseId);
        cv.put(COL_TYPE, "Fixed");
        cv.put(COL_DAY_OF_WEEK, dayOfWeek);
        cv.put(COL_START_HOUR, startHour);
        cv.put(COL_START_MIN, startMinute);
        cv.put(COL_END_HOUR, endHour);
        cv.put(COL_END_MIN, endMinute);
        if (extraClassDate > 0) {
            cv.put(COL_EXTRA_CLASS_DATE, extraClassDate);
            cv.put(COL_IS_EXTRA_CLASS, 1);  // Mark as extra class
        } else {
            cv.putNull(COL_EXTRA_CLASS_DATE);
            cv.put(COL_IS_EXTRA_CLASS, 0);
        }
        cv.putNull(COL_CUSTOMIZED_DATE);
        long id = db.insert(TABLE_SCHEDULE, null, cv);
        db.close();
        return id != -1;
    }

    // Insert Customized Schedule
    public boolean insertCustomizedSchedule(int courseId, long customizedDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ensureTableExists(db);
        ContentValues cv = new ContentValues();
        cv.put(COL_COURSE_ID, courseId);
        cv.put(COL_TYPE, "Customized");
        cv.putNull(COL_DAY_OF_WEEK);
        cv.putNull(COL_START_HOUR);
        cv.putNull(COL_START_MIN);
        cv.putNull(COL_END_HOUR);
        cv.putNull(COL_END_MIN);
        cv.putNull(COL_EXTRA_CLASS_DATE);
        cv.put(COL_CUSTOMIZED_DATE, customizedDate);
        cv.put(COL_IS_EXTRA_CLASS, 0);
        long id = db.insert(TABLE_SCHEDULE, null, cv);
        db.close();
        return id != -1;
    }

    // Get all schedules for a course
    public Cursor getSchedulesForCourse(int courseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        ensureTableExists(db);
        return db.query(TABLE_SCHEDULE,
                null,
                COL_COURSE_ID + "=?",
                new String[]{String.valueOf(courseId)},
                null,
                null,
                null);
    }

    // Get all extra classes for a course (new helper)
    public Cursor getExtraClassesForCourse(int courseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        ensureTableExists(db);
        return db.query(TABLE_SCHEDULE,
                null,
                COL_COURSE_ID + "=? AND " + COL_IS_EXTRA_CLASS + "=1",
                new String[]{String.valueOf(courseId)},
                null,
                null,
                COL_EXTRA_CLASS_DATE + " ASC");
    }

    // Update schedule by schedule_id
    public boolean updateSchedule(int scheduleId, int startHour, int startMinute, int endHour, int endMinute) {
        SQLiteDatabase db = this.getWritableDatabase();
        ensureTableExists(db);
        ContentValues cv = new ContentValues();
        cv.put(COL_START_HOUR, startHour);
        cv.put(COL_START_MIN, startMinute);
        cv.put(COL_END_HOUR, endHour);
        cv.put(COL_END_MIN, endMinute);
        int rows = db.update(TABLE_SCHEDULE, cv, COL_ID + "=?", new String[]{String.valueOf(scheduleId)});
        db.close();
        return rows > 0;
    }

    // Delete schedule by schedule_id
    public boolean deleteScheduleById(int scheduleId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ensureTableExists(db);
        int rows = db.delete(TABLE_SCHEDULE, COL_ID + "=?", new String[]{String.valueOf(scheduleId)});
        db.close();
        return rows > 0;
    }

    // Static convenience methods
    public static boolean addScheduleFixed(Context context, int courseId, String dayOfWeek, int startHour, int startMinute,
                                           int endHour, int endMinute, long extraClassDate) {
        CourseScheduleDBHelper dbHelper = new CourseScheduleDBHelper(context);
        return dbHelper.insertFixedSchedule(courseId, dayOfWeek, startHour, startMinute, endHour, endMinute, extraClassDate);
    }
    public static boolean addExtraClass(Context context, int courseId, int startHour, int startMinute,
                                        int endHour, int endMinute, long date, String description) {
        CourseScheduleDBHelper dbHelper = new CourseScheduleDBHelper(context);
        return dbHelper.insertExtraClass(courseId, startHour, startMinute, endHour, endMinute, date, description);
    }
    public boolean insertExtraClass(int courseId, int startHour, int startMinute,
                                    int endHour, int endMinute, long date, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ensureTableExists(db);

        ContentValues cv = new ContentValues();
        cv.put(COL_COURSE_ID, courseId);
        cv.put(COL_TYPE, "Fixed");  // Still marked as fixed
        cv.put(COL_DAY_OF_WEEK, "");  // Optional or null
        cv.put(COL_START_HOUR, startHour);
        cv.put(COL_START_MIN, startMinute);
        cv.put(COL_END_HOUR, endHour);
        cv.put(COL_END_MIN, endMinute);
        cv.put(COL_EXTRA_CLASS_DATE, date);
        cv.put(COL_IS_EXTRA_CLASS, 1);  // Mark as extra
        cv.put(COL_CUSTOMIZED_DATE, (Long) null);  // No customized date

        long id = db.insert(TABLE_SCHEDULE, null, cv);
        db.close();
        return id != -1;
    }

    public static boolean addScheduleCustomized(Context context, int courseId, long customizedDate) {
        CourseScheduleDBHelper dbHelper = new CourseScheduleDBHelper(context);
        return dbHelper.insertCustomizedSchedule(courseId, customizedDate);
    }
}
