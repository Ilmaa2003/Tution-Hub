package com.nibm.tutionmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import java.text.NumberFormat;

public class TeacherMain extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageButton toggleSidebar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacher_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        toggleSidebar = findViewById(R.id.toggle_sidebar);

        // Display teacher and student counts
        TextView tvTotalTeachers = findViewById(R.id.tvTotalTeachers);
        TextView tvTotalStudents = findViewById(R.id.tvTotalStudents);

        TeacherDatabaseHelper teacherDbHelper = new TeacherDatabaseHelper(this);
        StudentDatabaseHelper studentDbHelper = new StudentDatabaseHelper(this);

        int teacherCount = teacherDbHelper.getTeacherCount();
        int studentCount = studentDbHelper.getStudentCount();

        NumberFormat formatter = NumberFormat.getInstance();
        tvTotalTeachers.setText(formatter.format(teacherCount));
        tvTotalStudents.setText(formatter.format(studentCount));

        // Toggle sidebar drawer
        toggleSidebar.setOnClickListener(v -> {
            View sidebar = findViewById(R.id.sidebar);
            if (drawerLayout.isDrawerOpen(sidebar)) {
                drawerLayout.closeDrawer(sidebar, false);
            } else {
                drawerLayout.openDrawer(sidebar, false);
            }
        });

        // Sidebar item navigation
        findViewById(R.id.nav_attendance).setOnClickListener(v -> {
            drawerLayout.closeDrawer(findViewById(R.id.sidebar), false);
            startActivity(new Intent(this, teacherAttendance.class));
        });

        findViewById(R.id.nav_courses).setOnClickListener(v -> {
            drawerLayout.closeDrawer(findViewById(R.id.sidebar), false);
            startActivity(new Intent(this, TeacherCourseActivity.class));
        });

        findViewById(R.id.nav_results).setOnClickListener(v -> {
            drawerLayout.closeDrawer(findViewById(R.id.sidebar), false);
            startActivity(new Intent(this, teacherResults.class));
        });

        setupQuickActions();
    }

    private void setupQuickActions() {
        findViewById(R.id.card_users).setOnClickListener(v -> {
            startActivity(new Intent(this, teacherAttendance.class));
        });

        findViewById(R.id.card_reports).setOnClickListener(v -> {
            startActivity(new Intent(this, teacherResults.class));
        });

        findViewById(R.id.card_alerts).setOnClickListener(v -> {
            startActivity(new Intent(this, TeacherCourseActivity.class));
        });
    }

    @Override
    public void onBackPressed() {
        View sidebar = findViewById(R.id.sidebar);
        if (drawerLayout.isDrawerOpen(sidebar)) {
            drawerLayout.closeDrawer(sidebar, false);
        } else {
            super.onBackPressed();
        }
    }
}
