package com.nibm.tutionmanagement;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

public class ReportsActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageButton toggleSidebar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sdmin_report);


        drawerLayout = findViewById(R.id.drawer_layout);
        toggleSidebar = findViewById(R.id.toggle_sidebar);

        toggleSidebar.setOnClickListener(v -> {
            View sidebar = findViewById(R.id.sidebar);
            if (drawerLayout.isDrawerOpen(sidebar)) {
                drawerLayout.closeDrawer(sidebar, false); // close instantly, no animation
            } else {
                drawerLayout.openDrawer(sidebar, false);  // open instantly, no animation
            }
        });

        findViewById(R.id.nav_dashboard).setOnClickListener(v -> {
            drawerLayout.closeDrawer(findViewById(R.id.sidebar), false); // close instantly

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



        findViewById(R.id.nav_courses).setOnClickListener(v -> {
            drawerLayout.closeDrawer(findViewById(R.id.sidebar), false);
            startActivity(new Intent(this, CourseTabsActivity.class));
        });

    }


    private void showToast(String message) {
        Toast.makeText(ReportsActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        View sidebar = findViewById(R.id.sidebar);
        if (drawerLayout.isDrawerOpen(sidebar)) {
            drawerLayout.closeDrawer(sidebar, false); // close instantly on back press
        } else {
            super.onBackPressed();
        }
    }
}
