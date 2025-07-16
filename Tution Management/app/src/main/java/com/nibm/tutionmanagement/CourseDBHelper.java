package com.nibm.tutionmanagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class CourseDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TutionDB1.db";
    private static final int DATABASE_VERSION = 10;
    private static final String TAG = "CourseDBHelper";

    private static CourseDBHelper instance;

    // Singleton getInstance method
    public static synchronized CourseDBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new CourseDBHelper(context.getApplicationContext());
        }
        return instance;
    }

    private CourseDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.enableWriteAheadLogging();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createAllTables(db);
    }

    public CourseAssignmentFull getCourseAssignmentByCourseGradeBatch(String courseName, String grade, String batch) {
        SQLiteDatabase db = getReadableDatabase();
        CourseAssignmentFull result = null;
        Cursor cursor = null;
        try {
            String query = "SELECT ca.assignment_id, c.name, c.grade, c.batch, ca.description, ca.schedule_type, " +
                    "sf.day_of_week, sf.start_time, sf.end_time " +
                    "FROM CourseAssignment ca " +
                    "JOIN Course c ON ca.course_id = c.course_id " +
                    "LEFT JOIN ScheduleFixed sf ON ca.assignment_id = sf.assignment_id " +
                    "WHERE c.name = ? AND c.grade = ? AND c.batch = ?";

            cursor = db.rawQuery(query, new String[]{courseName, grade, batch});

            if (cursor.moveToFirst()) {
                int assignmentId = cursor.getInt(cursor.getColumnIndexOrThrow("assignment_id"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                String scheduleType = cursor.getString(cursor.getColumnIndexOrThrow("schedule_type"));
                String dayOfWeek = cursor.getString(cursor.getColumnIndexOrThrow("day_of_week"));
                String startTime = cursor.getString(cursor.getColumnIndexOrThrow("start_time"));
                String endTime = cursor.getString(cursor.getColumnIndexOrThrow("end_time"));

                result = new CourseAssignmentFull(
                        assignmentId,
                        courseName,
                        grade,
                        batch,
                        description,
                        scheduleType,
                        dayOfWeek,
                        startTime,
                        endTime
                );
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return result;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simple upgrade strategy: drop and recreate all tables
        db.execSQL("DROP TABLE IF EXISTS CourseAssignment_Student");
        db.execSQL("DROP TABLE IF EXISTS ScheduleFixed");
        db.execSQL("DROP TABLE IF EXISTS ExtraClass");
        db.execSQL("DROP TABLE IF EXISTS ScheduleCustomized");
        db.execSQL("DROP TABLE IF EXISTS CourseAssignment");
        db.execSQL("DROP TABLE IF EXISTS Course");
        createAllTables(db);
    }

    private void createAllTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS Course (" +
                "course_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "grade TEXT," +
                "batch TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS CourseAssignment (" +
                "assignment_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "course_id INTEGER," +
                "teacher_id INTEGER," +
                "description TEXT," +
                "schedule_type TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS CourseAssignment_Student (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "assignment_id INTEGER," +
                "student_id INTEGER)");

        db.execSQL("CREATE TABLE IF NOT EXISTS ScheduleFixed (" +
                "schedule_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "assignment_id INTEGER," +
                "day_of_week TEXT," +
                "start_time TEXT," +
                "end_time TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS ExtraClass (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "assignment_id INTEGER," +
                "date TEXT," +
                "start_time TEXT," +
                "end_time TEXT," +
                "description TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS ScheduleCustomized (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "assignment_id INTEGER," +
                "date TEXT," +
                "start_time TEXT," +
                "end_time TEXT)");

        Log.d(TAG, "All tables created or verified.");
    }

    // ----------------------------
    // CRUD Methods for Course
    // ----------------------------

    public boolean addCourse(String name, String grade, String batch) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("grade", grade);
        cv.put("batch", batch);
        long res = db.insert("Course", null, cv);
        return res != -1;
    }

    public boolean updateCourse(int id, String newName, String grade, String batch) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", newName);
        cv.put("grade", grade);
        cv.put("batch", batch);
        int rows = db.update("Course", cv, "course_id = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public boolean deleteCourse(int id) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete("Course", "course_id = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public List<String> getCourseNames() {
        List<String> courseNames = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT name FROM Course", null);
        if (cursor.moveToFirst()) {
            do {
                courseNames.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return courseNames;
    }

    // ----------------------------
    // CourseAssignment CRUD
    // ----------------------------

    // 1. Fetch full course + schedule details by assignmentId
    public CourseAssignmentFull getCourseAssignmentFullDetails(int assignmentId) {
        SQLiteDatabase db = getReadableDatabase();
        CourseAssignmentFull result = null;
        Cursor cursor = null;
        try {
            // Query joins CourseAssignment + Course + ScheduleFixed
            String query = "SELECT ca.assignment_id, c.name, c.grade, c.batch, ca.description, ca.schedule_type, " +
                    "sf.day_of_week, sf.start_time, sf.end_time " +
                    "FROM CourseAssignment ca " +
                    "JOIN Course c ON ca.course_id = c.course_id " +
                    "LEFT JOIN ScheduleFixed sf ON ca.assignment_id = sf.assignment_id " +
                    "WHERE ca.assignment_id = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(assignmentId)});
            if (cursor.moveToFirst()) {
                String courseName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String grade = cursor.getString(cursor.getColumnIndexOrThrow("grade"));
                String batch = cursor.getString(cursor.getColumnIndexOrThrow("batch"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                String scheduleType = cursor.getString(cursor.getColumnIndexOrThrow("schedule_type"));
                String dayOfWeek = cursor.getString(cursor.getColumnIndexOrThrow("day_of_week"));
                String startTime = cursor.getString(cursor.getColumnIndexOrThrow("start_time"));
                String endTime = cursor.getString(cursor.getColumnIndexOrThrow("end_time"));

                result = new CourseAssignmentFull(
                        assignmentId,
                        courseName,
                        grade,
                        batch,
                        description,
                        scheduleType,
                        dayOfWeek,
                        startTime,
                        endTime
                );
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return result;
    }

    // 2. Update description only for a given assignmentId
    public boolean updateCourseAssignmentDescription(int assignmentId, String description) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("description", description);
        int rows = db.update("CourseAssignment", cv, "assignment_id = ?", new String[]{String.valueOf(assignmentId)});
        return rows > 0;
    }

    // 3. Insert or update schedule fixed for a given assignmentId
    public boolean updateScheduleFixed(int assignmentId, String dayOfWeek, String startTime, String endTime) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = null;
        try {
            // Check if schedule exists
            cursor = db.rawQuery("SELECT schedule_id FROM ScheduleFixed WHERE assignment_id = ?", new String[]{String.valueOf(assignmentId)});
            ContentValues cv = new ContentValues();
            cv.put("assignment_id", assignmentId);
            cv.put("day_of_week", dayOfWeek);
            cv.put("start_time", startTime);
            cv.put("end_time", endTime);

            if (cursor.moveToFirst()) {
                // Exists, update
                int scheduleId = cursor.getInt(cursor.getColumnIndexOrThrow("schedule_id"));
                int rows = db.update("ScheduleFixed", cv, "schedule_id = ?", new String[]{String.valueOf(scheduleId)});
                return rows > 0;
            } else {
                // Does not exist, insert
                long id = db.insert("ScheduleFixed", null, cv);
                return id != -1;
            }
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public List<String> getTeacherNamesByCourseAndGrade(Context context, String courseName, String grade) {
        List<String> teacherNames = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        // Query to get distinct teacher IDs from Course table for given course name and grade
        Cursor cursor = db.rawQuery(
                "SELECT DISTINCT teacher_id FROM Course WHERE name = ? AND grade = ?",
                new String[]{courseName, grade}
        );

        TeacherDatabaseHelper teacherDbHelper = new TeacherDatabaseHelper(context);

        if (cursor.moveToFirst()) {
            do {
                int teacherId = cursor.getInt(cursor.getColumnIndexOrThrow("teacher_id"));
                // Fetch teacher name + email from TeacherDatabaseHelper using teacher ID
                String teacherNameWithEmail = teacherDbHelper.getTeacherNameWithEmailById(teacherId);
                if (!teacherNameWithEmail.isEmpty()) {
                    teacherNames.add(teacherNameWithEmail);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return teacherNames;
    }

    public List<Integer> getTeacherIdsForCourseAndGrade(String courseName, String grade) {
        List<Integer> teacherIds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT DISTINCT ca.teacher_id " +
                "FROM CourseAssignment ca " +
                "JOIN Course c ON ca.course_id = c.course_id " +
                "WHERE c.name = ? AND c.grade = ?";

        Cursor cursor = db.rawQuery(query, new String[]{courseName, grade});

        if (cursor.moveToFirst()) {
            do {
                int teacherId = cursor.getInt(cursor.getColumnIndexOrThrow("teacher_id"));
                teacherIds.add(teacherId);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return teacherIds;
    }

    public List<String> getGradesForCourse1(String courseName) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT DISTINCT grade FROM Course WHERE name = ?",
                    new String[]{courseName});
            if (cursor.moveToFirst()) {
                do {
                    String grade = cursor.getString(cursor.getColumnIndexOrThrow("grade"));
                    if (grade != null && !grade.isEmpty()) {
                        list.add(grade);
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }


  /*  public CourseAssignmentFull getCourseAssignmentFullDetails(int assignmentId) {
        SQLiteDatabase db = getReadableDatabase();
        CourseAssignmentFull result = null;
        Cursor cursor = null;

        // Join CourseAssignment + Course to get courseName, grade, batch, description, schedule_type
        String query = "SELECT ca.assignment_id, c.name, c.grade, c.batch, ca.description, ca.schedule_type " +
                "FROM CourseAssignment ca " +
                "JOIN Course c ON ca.course_id = c.course_id " +
                "WHERE ca.assignment_id = ?";

        try {
            cursor = db.rawQuery(query, new String[]{String.valueOf(assignmentId)});
            if (cursor.moveToFirst()) {
                String courseName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String grade = cursor.getString(cursor.getColumnIndexOrThrow("grade"));
                String batch = cursor.getString(cursor.getColumnIndexOrThrow("batch"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                String scheduleType = cursor.getString(cursor.getColumnIndexOrThrow("schedule_type"));

                // Now get schedule details (assuming ScheduleFixed for example)
                Cursor scheduleCursor = db.rawQuery(
                        "SELECT day_of_week, start_time, end_time FROM ScheduleFixed WHERE assignment_id = ?",
                        new String[]{String.valueOf(assignmentId)});

                String dayOfWeek = null;
                String startTime = null;
                String endTime = null;

                if (scheduleCursor.moveToFirst()) {
                    dayOfWeek = scheduleCursor.getString(scheduleCursor.getColumnIndexOrThrow("day_of_week"));
                    startTime = scheduleCursor.getString(scheduleCursor.getColumnIndexOrThrow("start_time"));
                    endTime = scheduleCursor.getString(scheduleCursor.getColumnIndexOrThrow("end_time"));
                }
                scheduleCursor.close();

                result = new CourseAssignmentFull(
                        assignmentId, courseName, grade, batch, description, scheduleType,
                        dayOfWeek, startTime, endTime);
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return result;
    }*/


    public List<CourseAssignment> getAllCourseAssignments() {
        List<CourseAssignment> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;

        String query = "SELECT ca.assignment_id, c.name, c.grade, c.batch, ca.description, ca.schedule_type " +
                "FROM CourseAssignment ca " +
                "JOIN Course c ON ca.course_id = c.course_id";

        try {
            cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                do {
                    int assignmentId = cursor.getInt(cursor.getColumnIndexOrThrow("assignment_id"));
                    String courseName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    String grade = cursor.getString(cursor.getColumnIndexOrThrow("grade"));
                    String batch = cursor.getString(cursor.getColumnIndexOrThrow("batch"));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                    String scheduleType = cursor.getString(cursor.getColumnIndexOrThrow("schedule_type"));

                    // For simplicity, dayOfWeek, startTime, endTime as null here
                    list.add(new CourseAssignment(
                            assignmentId, courseName, grade, batch,
                            description, scheduleType,
                            null, null, null));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }


    public boolean updateCourseAssignment(CourseAssignment course) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("description", course.getDescription());
        cv.put("schedule_type", course.getScheduleType());

        int rows = db.update("CourseAssignment", cv, "assignment_id = ?", new String[]{String.valueOf(course.getAssignmentId())});
        return rows > 0;
    }

    // ----------------------------
    // Filter Helpers
    // ----------------------------

    public List<String> getGradesForCourse(String courseName) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT DISTINCT grade FROM Course WHERE name = ?",
                    new String[]{courseName});
            if (cursor.moveToFirst()) {
                do {
                    String grade = cursor.getString(cursor.getColumnIndexOrThrow("grade"));
                    if (grade != null && !grade.isEmpty()) {
                        list.add(grade);
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }
    /**
     * Checks if a CourseAssignment exists for the given courseName, grade, and batch.
     * Returns true if exists, false otherwise.
     */
    public boolean isAssignmentExists(String courseName, String grade, String batch) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;

        try {
            // Step 1: Get course_id for the given courseName, grade, and batch
            cursor = db.rawQuery(
                    "SELECT course_id FROM Course WHERE name = ? AND grade = ? AND batch = ?",
                    new String[]{courseName, grade, batch}
            );

            if (cursor.moveToFirst()) {
                int courseId = cursor.getInt(cursor.getColumnIndexOrThrow("course_id"));
                cursor.close();

                // Step 2: Check if any assignment exists for this course_id
                cursor = db.rawQuery(
                        "SELECT 1 FROM CourseAssignment WHERE course_id = ? LIMIT 1",
                        new String[]{String.valueOf(courseId)}
                );

                exists = cursor.moveToFirst();
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return exists;
    }

    /**
     * Adds a course and corresponding assignment if not exists.
     * Returns assignment_id of existing or newly created assignment.
     */
    public long addCourseIfNotExists(String courseName, String grade, String batch, String description) {
        SQLiteDatabase db = getWritableDatabase();
        long assignmentId = -1;
        Cursor cursor = null;

        try {
            // Check if course exists
            cursor = db.rawQuery(
                    "SELECT course_id FROM Course WHERE name = ? AND grade = ? AND batch = ?",
                    new String[]{courseName, grade, batch}
            );

            if (cursor.moveToFirst()) {
                int courseId = cursor.getInt(cursor.getColumnIndexOrThrow("course_id"));
                cursor.close();

                // Check if assignment exists for this course
                cursor = db.rawQuery(
                        "SELECT assignment_id FROM CourseAssignment WHERE course_id = ?",
                        new String[]{String.valueOf(courseId)}
                );

                if (cursor.moveToFirst()) {
                    // Assignment exists - return existing assignment_id
                    assignmentId = cursor.getInt(cursor.getColumnIndexOrThrow("assignment_id"));
                    return assignmentId;
                } else {
                    // Insert new assignment for existing course
                    ContentValues assignCV = new ContentValues();
                    assignCV.put("course_id", courseId);
                    assignCV.put("teacher_id", 0); // Adjust teacher_id as needed
                    assignCV.put("description", description);
                    assignCV.put("schedule_type", "");
                    assignmentId = db.insert("CourseAssignment", null, assignCV);
                    return assignmentId;
                }
            }

            // Course does not exist - insert course first
            ContentValues cv = new ContentValues();
            cv.put("name", courseName);
            cv.put("grade", grade);
            cv.put("batch", batch);
            long courseId = db.insert("Course", null, cv);

            // Insert assignment for new course
            ContentValues assignCV = new ContentValues();
            assignCV.put("course_id", courseId);
            assignCV.put("teacher_id", 0); // Adjust teacher_id as needed
            assignCV.put("description", description);
            assignCV.put("schedule_type", "");
            assignmentId = db.insert("CourseAssignment", null, assignCV);

        } finally {
            if (cursor != null) cursor.close();
        }

        return assignmentId;
    }


    public List<String> getAllGrades() {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT DISTINCT grade FROM Course", null);
            if (cursor.moveToFirst()) {
                do {
                    String grade = cursor.getString(cursor.getColumnIndexOrThrow("grade"));
                    if (grade != null && !grade.isEmpty()) {
                        list.add(grade);
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public List<String> getBatchesForCourseAndGrade(String courseName, String gradeName) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT DISTINCT batch FROM Course WHERE name = ? AND grade = ?",
                    new String[]{courseName, gradeName});
            if (cursor.moveToFirst()) {
                do {
                    String batch = cursor.getString(cursor.getColumnIndexOrThrow("batch"));
                    if (batch != null && !batch.isEmpty()) {
                        list.add(batch);
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public List<String> getAllBatches() {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT DISTINCT batch FROM Course", null);
            if (cursor.moveToFirst()) {
                do {
                    String batch = cursor.getString(cursor.getColumnIndexOrThrow("batch"));
                    if (batch != null && !batch.isEmpty()) {
                        list.add(batch);
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public List<String> getStudentEmails(String course, String grade, String batch) {
        List<String> emails = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT s.email \n" +
                "FROM students s \n" +
                "JOIN CourseAssignment_Student cas ON s.id = cas.student_id \n" +
                "JOIN CourseAssignment ca ON cas.assignment_id = ca.assignment_id \n" +
                "JOIN Course c ON ca.course_id = c.course_id \n" +
                "WHERE c.name = ? AND c.grade = ? AND c.batch = ?\n";

        Cursor cursor = db.rawQuery(query, new String[]{course, grade, batch});
        if (cursor.moveToFirst()) {
            do {
                emails.add(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return emails;
    }

    public List<String> getBatchesForCourse(String courseName) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT DISTINCT batch FROM Course WHERE name = ?", new String[]{courseName});
            if (cursor.moveToFirst()) {
                do {
                    String batch = cursor.getString(cursor.getColumnIndexOrThrow("batch"));
                    if (batch != null && !batch.isEmpty()) {
                        list.add(batch);
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public List<String> getBatchesForGrade(String gradeName) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT DISTINCT batch FROM Course WHERE grade = ?", new String[]{gradeName});
            if (cursor.moveToFirst()) {
                do {
                    String batch = cursor.getString(cursor.getColumnIndexOrThrow("batch"));
                    if (batch != null && !batch.isEmpty()) {
                        list.add(batch);
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public List<Course2> getAllCoursesBasic() {
        List<Course2> courses = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;

        String query = "SELECT ca.assignment_id, c.name, c.grade, c.batch, ca.description, ca.schedule_type " +
                "FROM CourseAssignment ca " +
                "LEFT JOIN Course c ON ca.course_id = c.course_id";

        try {
            cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                do {
                    int assignmentId = cursor.getInt(cursor.getColumnIndexOrThrow("assignment_id"));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    String grade = cursor.getString(cursor.getColumnIndexOrThrow("grade"));
                    String batch = cursor.getString(cursor.getColumnIndexOrThrow("batch"));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                    String scheduleType = cursor.getString(cursor.getColumnIndexOrThrow("schedule_type"));

                    Log.d("CourseDebug", "Loaded: " + name + " - " + grade + " - " + batch);

                    courses.add(new Course2(assignmentId, name, grade, batch, description, scheduleType));
                } while (cursor.moveToNext());
            } else {
                Log.d("CourseDebug", "Cursor is empty.");
            }
        } catch (Exception e) {
            Log.e("CourseDebug", "Error: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        Log.d("CourseDebug", "Total: " + courses.size());
        return courses;
    }

}
