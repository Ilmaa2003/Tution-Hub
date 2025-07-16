package com.nibm.tutionmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

public class StudentMain extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageButton toggleSidebar;

    private View navDashboard, navAttendance, navNotifications, navCourses, navResults, navPayments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        toggleSidebar = findViewById(R.id.toggle_sidebar);

        navDashboard = findViewById(R.id.nav_dashboard);
        navAttendance = findViewById(R.id.nav_attendance);
        navNotifications = findViewById(R.id.nav_notifications);
        navCourses = findViewById(R.id.nav_courses);
        navResults = findViewById(R.id.nav_results);
        navPayments = findViewById(R.id.nav_payments);

        // Toggle sidebar open/close when clicking toggleSidebar button
        toggleSidebar.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(findViewById(R.id.sidebar))) {
                drawerLayout.closeDrawer(findViewById(R.id.sidebar), true);
            } else {
                drawerLayout.openDrawer(findViewById(R.id.sidebar));
            }
        });

        navDashboard.setOnClickListener(v -> {
            drawerLayout.closeDrawer(findViewById(R.id.sidebar), true);
            startActivity(new Intent(this, StudentMain.class)); // Dashboard or home screen
        });

        navAttendance.setOnClickListener(v -> {
            drawerLayout.closeDrawer(findViewById(R.id.sidebar), true);
            startActivity(new Intent(this, StudentAttenence.class));
        });



        navCourses.setOnClickListener(v -> {
            drawerLayout.closeDrawer(findViewById(R.id.sidebar), true);
            startActivity(new Intent(this, StudentCourse.class));
        });

        navResults.setOnClickListener(v -> {
            drawerLayout.closeDrawer(findViewById(R.id.sidebar), true);
            startActivity(new Intent(this, StudentResults.class));
        });

        navPayments.setOnClickListener(v -> {
            drawerLayout.closeDrawer(findViewById(R.id.sidebar), true);
            startActivity(new Intent(this, StudentPayments.class));
        });
    }

    private void showToast(String message) {
        Toast.makeText(StudentMain.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        View sidebar = findViewById(R.id.sidebar);
        if (drawerLayout.isDrawerOpen(sidebar)) {
            drawerLayout.closeDrawer(sidebar, true); // close drawer with animation on back press
        } else {
            super.onBackPressed();
        }
    }
}
