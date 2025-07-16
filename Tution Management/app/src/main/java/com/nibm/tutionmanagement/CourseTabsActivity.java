package com.nibm.tutionmanagement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class CourseTabsActivity extends AppCompatActivity {

    private TabLayout tabLayout;

    private DrawerLayout drawerLayout;

    private ViewPager2 viewPager;
    private SharedPreferences draftPrefs;
    private static final String PREFS_NAME = "course_draft";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_course_tabs); // Layout should contain tabLayout and viewPager
        drawerLayout = findViewById(R.id.drawer_layout); // this must be called after setContentView()

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        draftPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        viewPager.setAdapter(new CoursePagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) tab.setText("Start Course");
                    else tab.setText("Update Course");
                }).attach();


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




        findViewById(R.id.nav_dashboard).setOnClickListener(v -> {
            drawerLayout.closeDrawer(findViewById(R.id.sidebar), false);
            startActivity(new Intent(this, MainActivity.class));
        });
    }

    private static class CoursePagerAdapter extends FragmentStateAdapter {
        public CoursePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new StartCourseFragment();
                case 1:
                    return new UpdateCourseFragment();
                default:
                    return new StartCourseFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveDraft();
    }

    private void saveDraft() {
        SharedPreferences.Editor editor = draftPrefs.edit();
        // Save fields here if needed (handled in StartCourseFragment ideally)
        editor.apply();
    }

    private void clearDraft() {
        SharedPreferences.Editor editor = draftPrefs.edit();
        editor.clear();
        editor.apply();
    }
}
