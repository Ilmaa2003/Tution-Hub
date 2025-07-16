package com.nibm.tutionmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import java.text.NumberFormat;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageButton toggleSidebar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        toggleSidebar = findViewById(R.id.toggle_sidebar);

        // ─────────────────────────────────────────────
        // Show Total Teacher Count
        TextView tvTotalTeachers = findViewById(R.id.tvTotalTeachers);
        TeacherDatabaseHelper teacherDbHelper = new TeacherDatabaseHelper(this);
        int teacherCount = teacherDbHelper.getTeacherCount();

        // Show Total Student Count
        TextView tvTotalStudents = findViewById(R.id.tvTotalStudents);
        StudentDatabaseHelper studentDbHelper = new StudentDatabaseHelper(this);
        int studentCount = studentDbHelper.getStudentCount();

        // Format and set counts
        NumberFormat formatter = NumberFormat.getInstance();
        tvTotalTeachers.setText(formatter.format(teacherCount));
        tvTotalStudents.setText(formatter.format(studentCount));
        // ─────────────────────────────────────────────

        // Sidebar toggle logic
        toggleSidebar.setOnClickListener(v -> {
            View sidebar = findViewById(R.id.sidebar);
            if (drawerLayout.isDrawerOpen(sidebar)) {
                drawerLayout.closeDrawer(sidebar, false);
            } else {
                drawerLayout.openDrawer(sidebar, false);
            }
        });

        findViewById(R.id.nav_dashboard).setOnClickListener(v -> {
            drawerLayout.closeDrawer(findViewById(R.id.sidebar), false);
            ScrollView scrollView = findViewById(R.id.scroll_view);
            if (scrollView != null) {
                scrollView.post(() -> scrollView.fullScroll(View.FOCUS_UP));
            }
        });

        findViewById(R.id.nav_users).setOnClickListener(v -> {
            drawerLayout.closeDrawer(findViewById(R.id.sidebar), false);
            startActivity(new Intent(this, UsersActivity.class));
        });

        findViewById(R.id.nav_payments).setOnClickListener(v -> {
            drawerLayout.closeDrawer(findViewById(R.id.sidebar), false);
            startActivity(new Intent(this, adminpayment.class));
        });

        findViewById(R.id.nav_courses).setOnClickListener(v -> {
            drawerLayout.closeDrawer(findViewById(R.id.sidebar), false);
            startActivity(new Intent(this, CourseTabsActivity.class));
        });

        findViewById(R.id.nav_reports).setOnClickListener(v -> {
            drawerLayout.closeDrawer(findViewById(R.id.sidebar), false);
            startActivity(new Intent(this, ReportsActivity.class));
        });

        setupQuickActions();
    }

    private void setupQuickActions() {
        findViewById(R.id.card_users).setOnClickListener(v -> {
            startActivity(new Intent(this, UsersActivity.class));
        });

        findViewById(R.id.card_reports).setOnClickListener(v -> {
            startActivity(new Intent(this, adminpayment.class));
        });

        findViewById(R.id.card_alerts).setOnClickListener(v -> {
            startActivity(new Intent(this, CourseTabsActivity.class));
        });
    }

    private void showToast(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
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
