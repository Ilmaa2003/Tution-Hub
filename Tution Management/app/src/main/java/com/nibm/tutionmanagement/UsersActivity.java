package com.nibm.tutionmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageButton toggleSidebar;
    private FloatingActionButton fabAddUser;
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private LinearLayout deletionBar;
    private TextView tvSelectedCount;
    private ImageButton btnDeleteSelectedBar;

    private List<StudentDB> studentList = new ArrayList<>();
    private List<TeacherDB> teacherList = new ArrayList<>();

    private UserAdapter<StudentDB> studentAdapter;
    private UserAdapter<TeacherDB> teacherAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_user);

        // Initialize views
        drawerLayout = findViewById(R.id.drawer_layout);
        toggleSidebar = findViewById(R.id.toggle_sidebar);
        tabLayout = findViewById(R.id.tab_layout_users);
        recyclerView = findViewById(R.id.rv_user_cards);
        searchView = findViewById(R.id.search_view_users);
        fabAddUser = findViewById(R.id.fab_add_user);
        deletionBar = findViewById(R.id.deletion_bar);
        tvSelectedCount = findViewById(R.id.tv_selected_count);
        btnDeleteSelectedBar = findViewById(R.id.btn_delete_selected_bar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setupAdapters();
        setupTabs();
        setupSearch();
        setupSidebarNavigation();
        setupDeleteBar();

        loadUserData();

        fabAddUser.setOnClickListener(v -> showAddUserDialog());

        toggleSidebar.setOnClickListener(v -> {
            View sidebar = findViewById(R.id.sidebar);
            if (drawerLayout.isDrawerOpen(sidebar)) {
                drawerLayout.closeDrawer(sidebar, false);
            } else {
                drawerLayout.openDrawer(sidebar, false);
            }
        });
    }

    private void setupAdapters() {
        studentAdapter = new UserAdapter<>(studentList, (holder, student) -> {
            holder.tvName.setText(student.getName());
            holder.tvRole.setText(student.getRole());
        });

        teacherAdapter = new UserAdapter<>(teacherList, (holder, teacher) -> {
            holder.tvName.setText(teacher.getName());
            holder.tvRole.setText(teacher.getRole());
        });

        // Item click listeners
        studentAdapter.setOnItemClickListener(student -> {
            if (studentAdapter.isSelectionMode()) {
                studentAdapter.toggleSelection(student);
                updateSelectionUI();
            } else {
                EditStudentBottomSheet bottomSheet = new EditStudentBottomSheet(student);
                bottomSheet.setOnUserUpdatedListener(this::loadUserData);
                bottomSheet.show(getSupportFragmentManager(), "EditStudentBottomSheet");
            }
        });

        teacherAdapter.setOnItemClickListener(teacher -> {
            if (teacherAdapter.isSelectionMode()) {
                teacherAdapter.toggleSelection(teacher);
                updateSelectionUI();
            } else {
                EditTeacherBottomSheet bottomSheet = new EditTeacherBottomSheet(teacher);
                bottomSheet.setOnUserUpdatedListener(this::loadUserData);
                bottomSheet.show(getSupportFragmentManager(), "EditTeacherBottomSheet");
            }
        });

        // Long click for selection mode
        studentAdapter.setOnItemLongClickListener(student -> {
            studentAdapter.toggleSelection(student);
            updateSelectionUI();
        });

        teacherAdapter.setOnItemLongClickListener(teacher -> {
            teacherAdapter.toggleSelection(teacher);
            updateSelectionUI();
        });

        // Popup menu Edit/Delete handlers for students
        studentAdapter.setOnEditClickListener(student -> {
            EditStudentBottomSheet bottomSheet = new EditStudentBottomSheet(student);
            bottomSheet.setOnUserUpdatedListener(this::loadUserData);
            bottomSheet.show(getSupportFragmentManager(), "EditStudentBottomSheet");
        });

        studentAdapter.setOnDeleteClickListener(student -> {
            new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                    .setTitle("Confirm Deletion")
                    .setMessage("Are you sure you want to delete " + student.getName() + "?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        new StudentDatabaseHelper(this).deleteStudent(student.getId());
                        loadUserData();
                        Toast.makeText(this, "Deleted " + student.getName(), Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Popup menu Edit/Delete handlers for teachers
        teacherAdapter.setOnEditClickListener(teacher -> {
            EditTeacherBottomSheet bottomSheet = new EditTeacherBottomSheet(teacher);
            bottomSheet.setOnUserUpdatedListener(this::loadUserData);
            bottomSheet.show(getSupportFragmentManager(), "EditTeacherBottomSheet");
        });

        teacherAdapter.setOnDeleteClickListener(teacher -> {
            new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                    .setTitle("Confirm Deletion")
                    .setMessage("Are you sure you want to delete " + teacher.getName() + "?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        new TeacherDatabaseHelper(this).deleteTeacher(teacher.getId());
                        loadUserData();
                        Toast.makeText(this, "Deleted " + teacher.getName(), Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Students"));
        tabLayout.addTab(tabLayout.newTab().setText("Teachers"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                clearSelection();
                if (tab.getPosition() == 0) {
                    recyclerView.setAdapter(studentAdapter);
                    filterStudents(searchView.getQuery().toString());
                } else {
                    recyclerView.setAdapter(teacherAdapter);
                    filterTeachers(searchView.getQuery().toString());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        tabLayout.selectTab(tabLayout.getTabAt(0));
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (tabLayout.getSelectedTabPosition() == 0) {
                    filterStudents(newText);
                } else {
                    filterTeachers(newText);
                }
                return true;
            }
        });
    }

    private void filterStudents(String query) {
        if (TextUtils.isEmpty(query)) {
            studentAdapter.setData(studentList);
            return;
        }
        List<StudentDB> filtered = new ArrayList<>();
        for (StudentDB s : studentList) {
            if (s.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(s);
            }
        }
        studentAdapter.setData(filtered);
    }

    private void filterTeachers(String query) {
        if (TextUtils.isEmpty(query)) {
            teacherAdapter.setData(teacherList);
            return;
        }
        List<TeacherDB> filtered = new ArrayList<>();
        for (TeacherDB t : teacherList) {
            if (t.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(t);
            }
        }
        teacherAdapter.setData(filtered);
    }

    private void setupSidebarNavigation() {
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

    private void navigate(String name) {
        drawerLayout.closeDrawers();
        Toast.makeText(this, name + " clicked", Toast.LENGTH_SHORT).show();
    }

    private void setupDeleteBar() {
        btnDeleteSelectedBar.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirm Deletion")
                    .setMessage("Delete selected users?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (tabLayout.getSelectedTabPosition() == 0) {
                            for (StudentDB s : studentAdapter.getSelectedItems()) {
                                new StudentDatabaseHelper(this).deleteStudent(s.getId());
                            }
                        } else {
                            for (TeacherDB t : teacherAdapter.getSelectedItems()) {
                                new TeacherDatabaseHelper(this).deleteTeacher(t.getId());
                            }
                        }
                        clearSelection();
                        loadUserData();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void updateSelectionUI() {
        int count = (tabLayout.getSelectedTabPosition() == 0)
                ? studentAdapter.getSelectedItems().size()
                : teacherAdapter.getSelectedItems().size();

        if (count > 0) {
            deletionBar.setVisibility(View.VISIBLE);
            tvSelectedCount.setText(count + " selected");
        } else {
            clearSelection();
        }
    }

    private void clearSelection() {
        deletionBar.setVisibility(View.GONE);
        studentAdapter.clearSelection();
        teacherAdapter.clearSelection();
    }

    private void loadUserData() {
        StudentDatabaseHelper studentDB = new StudentDatabaseHelper(this);
        TeacherDatabaseHelper teacherDB = new TeacherDatabaseHelper(this);

        studentList = studentDB.getAllStudents();
        teacherList = teacherDB.getAllTeachers();

        if (tabLayout.getSelectedTabPosition() == 0) {
            studentAdapter.setData(studentList);
            recyclerView.setAdapter(studentAdapter);
        } else {
            teacherAdapter.setData(teacherList);
            recyclerView.setAdapter(teacherAdapter);
        }
    }

    private void showAddUserDialog() {
        String[] userTypes = {"Student", "Teacher"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setTitle("Add New User")
                .setItems(userTypes, (dialog, which) -> {
                    if (which == 0) openAddStudentForm();
                    else openAddTeacherForm();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openAddStudentForm() {
        startActivity(new Intent(this, AddStudentActivity.class));
    }

    private void openAddTeacherForm() {
        startActivity(new Intent(this, addTeacherActivity.class));
    }

    @Override
    public void onBackPressed() {
        View sidebar = findViewById(R.id.sidebar);
        if (drawerLayout.isDrawerOpen(sidebar)) {
            drawerLayout.closeDrawer(sidebar, false);
        } else if (deletionBar.getVisibility() == View.VISIBLE) {
            clearSelection();
        } else {
            super.onBackPressed();
        }
    }
}
