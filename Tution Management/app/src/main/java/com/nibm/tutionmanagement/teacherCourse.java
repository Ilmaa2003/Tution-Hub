package com.nibm.tutionmanagement;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class teacherCourse extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CourseAdapter courseAdapter;
    private List<Course2> courseList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacher_course); // Make sure you have this layout

        recyclerView = findViewById(R.id.rvCourses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load courses - replace with your actual data loading
        courseList = loadCourses();

        courseAdapter = new CourseAdapter(this, courseList);
        recyclerView.setAdapter(courseAdapter);

        courseAdapter.setOnAddAssignmentClickListener((position, course) -> {
            // Handle Add Assignment click
            Toast.makeText(this, "Add Assignment for " + course.getName(), Toast.LENGTH_SHORT).show();
            // Open dialog or new activity to add assignment here
        });

        courseAdapter.setOnAddMaterialClickListener((position, course) -> {
            // Handle Add Material click
            Toast.makeText(this, "Add Material for " + course.getName(), Toast.LENGTH_SHORT).show();
            // Open dialog or new activity to add material here
        });
    }

    private List<Course2> loadCourses() {
        // Dummy data or load from DB/Firebase
        List<Course2> list = new ArrayList<>();
        // Add sample Course2 objects to list here
        return list;
    }
}
