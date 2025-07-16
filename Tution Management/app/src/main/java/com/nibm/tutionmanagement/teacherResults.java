package com.nibm.tutionmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Map;

public class teacherResults extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageButton toggleSidebar;
    private Spinner spinnerCourse, spinnerGrade, spinnerBatch;
    private EditText etExamName;
    private RecyclerView recyclerStudents;
    private Button btnLoadStudents, btnSubmit;
    private CourseDBHelper dbHelper;
    private EmailAdapter emailAdapter;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacher_results);

        drawerLayout = findViewById(R.id.drawer_layout);
        toggleSidebar = findViewById(R.id.btnToggle);
        spinnerCourse = findViewById(R.id.spinnerCourse);
        spinnerGrade = findViewById(R.id.spinnerGrade);
        spinnerBatch = findViewById(R.id.spinnerBatch);
        etExamName = findViewById(R.id.etExamName);
        recyclerStudents = findViewById(R.id.recyclerStudents);
        btnLoadStudents = findViewById(R.id.btnLoadStudents);
        btnSubmit = findViewById(R.id.btnSubmit);

        dbHelper = CourseDBHelper.getInstance(this);

        recyclerStudents.setLayoutManager(new LinearLayoutManager(this));
        recyclerStudents.setVisibility(View.GONE);

        databaseReference = FirebaseDatabase.getInstance(
                        "https://tuition-b6019-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("studentGrades");

        setupSidebarNavigation();
        loadCourses();

        spinnerCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCourse = (String) parent.getItemAtPosition(position);
                if (selectedCourse != null && !selectedCourse.isEmpty()) {
                    loadGrades(selectedCourse);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spinnerGrade.setAdapter(null);
                spinnerBatch.setAdapter(null);
                recyclerStudents.setVisibility(View.GONE);
            }
        });

        spinnerGrade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedGrade = (String) parent.getItemAtPosition(position);
                String selectedCourse = spinnerCourse.getSelectedItem() != null ? spinnerCourse.getSelectedItem().toString() : "";
                if (!selectedCourse.isEmpty() && selectedGrade != null && !selectedGrade.isEmpty()) {
                    loadBatches(selectedCourse, selectedGrade);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spinnerBatch.setAdapter(null);
                recyclerStudents.setVisibility(View.GONE);
            }
        });

        btnLoadStudents.setOnClickListener(v -> loadStudentsEmails());

        btnSubmit.setOnClickListener(v -> {
            if (emailAdapter == null) {
                Toast.makeText(this, "Load students first", Toast.LENGTH_SHORT).show();
                return;
            }

            String course = spinnerCourse.getSelectedItem() != null ? spinnerCourse.getSelectedItem().toString() : "";
            String examName = etExamName.getText().toString().trim();

            if (course.isEmpty() || examName.isEmpty()) {
                Toast.makeText(this, "Course and Exam name are required", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, String> gradesMap = emailAdapter.getGradesMap();

            if (gradesMap.isEmpty()) {
                Toast.makeText(this, "Enter grades for students", Toast.LENGTH_SHORT).show();
                return;
            }

            for (Map.Entry<String, String> entry : gradesMap.entrySet()) {
                String email = entry.getKey();
                String grade = entry.getValue();

                if (grade == null || grade.isEmpty()) continue;

                String key = databaseReference.push().getKey();
                StudentGrade studentGrade = new StudentGrade(course, examName, email, grade);

                if (key != null) {
                    databaseReference.child(key).setValue(studentGrade)
                            .addOnSuccessListener(aVoid -> Log.d("Firebase", "Grade saved for " + email))
                            .addOnFailureListener(e -> Log.e("Firebase", "Failed to save grade for " + email, e));
                }
            }

            Toast.makeText(this, "Grades uploaded to Firebase", Toast.LENGTH_SHORT).show();

            // Clear fields after upload
            etExamName.setText("");
            if (emailAdapter != null) {
                emailAdapter.clearGrades(); // You need to implement this method in EmailAdapter
            }
            recyclerStudents.setVisibility(View.GONE);

            spinnerCourse.setSelection(0);
            spinnerGrade.setAdapter(null);
            spinnerBatch.setAdapter(null);
        });
    }

    private void setupSidebarNavigation() {
        toggleSidebar.setOnClickListener(v -> {
            View sidebar = findViewById(R.id.sidebar);
            if (drawerLayout.isDrawerOpen(sidebar)) {
                drawerLayout.closeDrawer(sidebar);
            } else {
                drawerLayout.openDrawer(sidebar);
            }
        });

        View navAttendance = findViewById(R.id.nav_attendance);
        if (navAttendance != null) {
            navAttendance.setOnClickListener(v -> {
                drawerLayout.closeDrawer(findViewById(R.id.sidebar));
                startActivity(new Intent(this, UsersActivity.class));
            });
        }

        View navNotifications = findViewById(R.id.nav_notifications);
        if (navNotifications != null) {
            navNotifications.setOnClickListener(v -> {
                drawerLayout.closeDrawer(findViewById(R.id.sidebar));
                startActivity(new Intent(this, adminpayment.class));
            });
        }

        View navCourses = findViewById(R.id.nav_courses);
        if (navCourses != null) {
            navCourses.setOnClickListener(v -> {
                drawerLayout.closeDrawer(findViewById(R.id.sidebar));
                startActivity(new Intent(this, CourseTabsActivity.class));
            });
        }

        View navResults = findViewById(R.id.nav_results);
        if (navResults != null) {
            navResults.setOnClickListener(v -> drawerLayout.closeDrawer(findViewById(R.id.sidebar)));
        }
    }

    private void loadCourses() {
        List<String> courses = dbHelper.getCourseNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourse.setAdapter(adapter);

        spinnerGrade.setAdapter(null);
        spinnerBatch.setAdapter(null);
        recyclerStudents.setVisibility(View.GONE);
    }

    private void loadGrades(String courseName) {
        List<String> grades = dbHelper.getGradesForCourse(courseName);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, grades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGrade.setAdapter(adapter);

        spinnerBatch.setAdapter(null);
        recyclerStudents.setVisibility(View.GONE);
    }

    private void loadBatches(String courseName, String grade) {
        List<String> batches = dbHelper.getBatchesForCourseAndGrade(courseName, grade);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, batches);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBatch.setAdapter(adapter);

        recyclerStudents.setVisibility(View.GONE);
    }

    private void loadStudentsEmails() {
        if (spinnerCourse.getSelectedItem() == null ||
                spinnerGrade.getSelectedItem() == null ||
                spinnerBatch.getSelectedItem() == null) {
            Toast.makeText(this, "Please select Course, Grade, and Batch", Toast.LENGTH_SHORT).show();
            return;
        }

        String course = spinnerCourse.getSelectedItem().toString();
        String grade = spinnerGrade.getSelectedItem().toString();
        String batch = spinnerBatch.getSelectedItem().toString();
        String examName = etExamName.getText().toString().trim();

        if (examName.isEmpty()) {
            Toast.makeText(this, "Enter exam name", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean assignmentExists = dbHelper.isAssignmentExists(course, grade, batch);
        if (assignmentExists) {
            List<String> studentEmails = dbHelper.getStudentEmails(course, grade, batch);
            if (studentEmails.isEmpty()) {
                recyclerStudents.setVisibility(View.GONE);
                Toast.makeText(this, "No students found.", Toast.LENGTH_SHORT).show();
            } else {
                recyclerStudents.setVisibility(View.VISIBLE);
                emailAdapter = new EmailAdapter(studentEmails);
                recyclerStudents.setAdapter(emailAdapter);
                Toast.makeText(this, "Found " + studentEmails.size() + " students.", Toast.LENGTH_SHORT).show();
            }
        } else {
            recyclerStudents.setVisibility(View.GONE);
            Toast.makeText(this, "No assignment exists for the selected course/grade/batch.", Toast.LENGTH_SHORT).show();
        }
    }
}
