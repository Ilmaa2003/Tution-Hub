package com.nibm.tutionmanagement;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class StudentCourse extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private RecyclerView rvCourses;
    private CourseAdapter courseAdapter;
    private CourseDBHelper dbHelper;

    private DatabaseReference materialsRef;
    private DatabaseReference assignmentsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacher_course);

        drawerLayout = findViewById(R.id.drawer_layout);
        findViewById(R.id.btnToggle).setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(findViewById(R.id.sidebar))) {
                drawerLayout.closeDrawer(findViewById(R.id.sidebar));
            } else {
                drawerLayout.openDrawer(findViewById(R.id.sidebar));
            }
        });

        rvCourses = findViewById(R.id.rvCourses);
        rvCourses.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = CourseDBHelper.getInstance(this);
        materialsRef = FirebaseDatabase.getInstance("https://tuition-b6019-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("course_materials");
        assignmentsRef = FirebaseDatabase.getInstance("https://tuition-b6019-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("course_assignments");

        loadCourses();
    }

    private String sanitizeKey(String key) {
        return key.replaceAll("[.#$\\[\\] ]", "_");
    }

    private String getCourseFirebaseKey(Course2 course) {
        String rawKey = course.getName() + "_" + course.getGrade() + "_" + course.getBatch();
        return sanitizeKey(rawKey);
    }

    private void loadCourses() {
        List<Course2> courses = dbHelper.getAllCoursesBasic();

        if (courses.isEmpty()) {
            Toast.makeText(this, "No courses found", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Course2 course : courses) {
            course.getMaterials().clear();
            course.getAssignments().clear();

            String key = getCourseFirebaseKey(course);

            materialsRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    course.getMaterials().clear();
                    if (snapshot.exists()) {
                        for (DataSnapshot materialSnap : snapshot.getChildren()) {
                            CourseMaterial material = materialSnap.getValue(CourseMaterial.class);
                            if (material != null) {
                                material.setFirebaseKey(materialSnap.getKey());
                                course.getMaterials().add(material);
                            }
                        }
                    }
                    if (courseAdapter != null) {
                        courseAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(StudentCourse.this, "Failed to load materials", Toast.LENGTH_SHORT).show();
                }
            });

            assignmentsRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    course.getAssignments().clear();
                    if (snapshot.exists()) {
                        for (DataSnapshot assignmentSnap : snapshot.getChildren()) {
                            Assignment assignment = assignmentSnap.getValue(Assignment.class);
                            if (assignment != null) {
                                assignment.setFirebaseKey(assignmentSnap.getKey());
                                course.getAssignments().add(assignment);
                            }
                        }
                    }
                    if (courseAdapter != null) {
                        courseAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(StudentCourse.this, "Failed to load assignments", Toast.LENGTH_SHORT).show();
                }
            });
        }

        courseAdapter = new CourseAdapter(this, courses);
        courseAdapter.setEnableEdit(false); // disable editing in adapter if implemented
        rvCourses.setAdapter(courseAdapter);
    }
}
