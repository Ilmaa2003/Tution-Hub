package com.nibm.tutionmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

public class AddStudentActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageButton toggleSidebar;
    private EditText etName, etAddress, etDob, etPhone, etEmail, etParentPhone, etParentEmail;
    private Button btnSave, btnCancel;
    private StudentDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onResume();

        // Reinitialize or reload DB if necessary
        dbHelper = new StudentDatabaseHelper(this);
        setContentView(R.layout.activity_add_student); // Replace with your actual layout XML filename

        drawerLayout = findViewById(R.id.drawer_layout);
        toggleSidebar = findViewById(R.id.toggle_sidebar);

        etName = findViewById(R.id.et_student_name);
        etAddress = findViewById(R.id.et_student_address);
        etDob = findViewById(R.id.et_student_dob);
        etPhone = findViewById(R.id.et_student_phone);
        etEmail = findViewById(R.id.et_student_email);
        etParentPhone = findViewById(R.id.et_parent_phone);
        etParentEmail = findViewById(R.id.et_parent_email);

        btnSave = findViewById(R.id.btn_save_student);
        btnCancel = findViewById(R.id.btn_cancel_student);

        dbHelper = new StudentDatabaseHelper(this);

        // Sidebar toggle button
        toggleSidebar.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(findViewById(R.id.sidebar))) {
                drawerLayout.closeDrawer(findViewById(R.id.sidebar));
            } else {
                drawerLayout.openDrawer(findViewById(R.id.sidebar));
            }
        });

        // Save button click
        btnSave.setOnClickListener(v -> saveStudent());

        // Cancel button click (clears fields)
        btnCancel.setOnClickListener(v -> clearFields());

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

        findViewById(R.id.nav_notifications).setOnClickListener(v -> {
            drawerLayout.closeDrawer(findViewById(R.id.sidebar), false);
            startActivity(new Intent(this, NotificationsActivity.class));
        });


        findViewById(R.id.nav_dashboard).setOnClickListener(v -> {
            drawerLayout.closeDrawer(findViewById(R.id.sidebar), false);
            startActivity(new Intent(this, MainActivity.class));
        });
    }

    private void saveStudent() {
        String name = etName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String dob = etDob.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String parentPhone = etParentPhone.getText().toString().trim();
        String parentEmail = etParentEmail.getText().toString().trim();

        // Validate Name
        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }

        // Validate Address
        if (address.isEmpty()) {
            etAddress.setError("Address is required");
            etAddress.requestFocus();
            return;
        }

        // Validate DOB (simple check - not empty; you may add date format check if needed)
        if (dob.isEmpty()) {
            etDob.setError("Date of Birth is required");
            etDob.requestFocus();
            return;
        }

        // Validate Phone
        if (phone.isEmpty()) {
            etPhone.setError("Phone number is required");
            etPhone.requestFocus();
            return;
        }
        if (!phone.matches("\\d{10}")) {  // Example: exactly 10 digits
            etPhone.setError("Enter a valid 10-digit phone number");
            etPhone.requestFocus();
            return;
        }

        // Validate Email
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }
        // Check email uniqueness in DB
        if (dbHelper.isEmailExists(email)) {
            etEmail.setError("This email is already registered");
            etEmail.requestFocus();
            return;
        }

        // Validate Parent Phone
        if (parentPhone.isEmpty()) {
            etParentPhone.setError("Parent's phone number is required");
            etParentPhone.requestFocus();
            return;
        }
        if (!parentPhone.matches("\\d{10}")) {
            etParentPhone.setError("Enter a valid 10-digit phone number");
            etParentPhone.requestFocus();
            return;
        }

        // Validate Parent Email
        if (parentEmail.isEmpty()) {
            etParentEmail.setError("Parent's email is required");
            etParentEmail.requestFocus();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(parentEmail).matches()) {
            etParentEmail.setError("Enter a valid email");
            etParentEmail.requestFocus();
            return;
        }

        // If all validations pass, create StudentDB object and save
        StudentDB student = new StudentDB(0, name, address, dob, phone, email, parentPhone, parentEmail);

        boolean inserted = dbHelper.addStudent(student);
        if (inserted) {
            Toast.makeText(this, "Student saved successfully", Toast.LENGTH_SHORT).show();
            clearFields();
        } else {
            Toast.makeText(this, "Failed to save student", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFields() {
        etName.setText("");
        etAddress.setText("");
        etDob.setText("");
        etPhone.setText("");
        etEmail.setText("");
        etParentPhone.setText("");
        etParentEmail.setText("");
    }
}
