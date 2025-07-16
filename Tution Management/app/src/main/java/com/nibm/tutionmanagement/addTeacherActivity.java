package com.nibm.tutionmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

public class addTeacherActivity extends AppCompatActivity {

    private EditText etName, etAddress, etDob, etPhone, etEmail;
    private Button btnSave, btnCancel;
    private DrawerLayout drawerLayout;

    private TeacherDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onResume();

        // Reinitialize or reload DB if necessary
        dbHelper = new TeacherDatabaseHelper(this);
        setContentView(R.layout.activity_add_teacher); // Make sure your XML file is named this way

        dbHelper = new TeacherDatabaseHelper(this);

        // Initialize UI components
        etName = findViewById(R.id.et_teacher_name);
        etAddress = findViewById(R.id.et_teacher_address);
        etDob = findViewById(R.id.et_teacher_dob);
        etPhone = findViewById(R.id.et_teacher_phone);
        etEmail = findViewById(R.id.et_teacher_email);
        btnSave = findViewById(R.id.btn_save_teacher);
        btnCancel = findViewById(R.id.btn_cancel_teacher);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTeacher();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearFields();
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

        findViewById(R.id.nav_notifications).setOnClickListener(v -> {
            drawerLayout.closeDrawer(findViewById(R.id.sidebar), false);
            startActivity(new Intent(this, NotificationsActivity.class));
        });


        findViewById(R.id.nav_dashboard).setOnClickListener(v -> {
            drawerLayout.closeDrawer(findViewById(R.id.sidebar), false);
            startActivity(new Intent(this, MainActivity.class));
        });
    }

    private void saveTeacher() {
        String name = etName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String dob = etDob.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String role = "Teacher";

        // Validate each field separately with specific errors
        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }
        if (address.isEmpty()) {
            etAddress.setError("Address is required");
            etAddress.requestFocus();
            return;
        }
        if (dob.isEmpty()) {
            etDob.setError("Date of birth is required");
            etDob.requestFocus();
            return;
        }
        if (phone.isEmpty()) {
            etPhone.setError("Phone number is required");
            etPhone.requestFocus();
            return;
        }
        if (!phone.matches("\\d{10}")) {  // Assuming 10 digit phone number
            etPhone.setError("Enter a valid 10-digit phone number");
            etPhone.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        // Optional: check if email already exists in teacher database
        if (dbHelper.isEmailExists(email)) {
            etEmail.setError("This email is already registered");
            etEmail.requestFocus();
            return;
        }

        TeacherDB teacher = new TeacherDB(0, name, address, dob, phone, email, role);

        boolean success = dbHelper.addTeacher(teacher);

        if (success) {
            Toast.makeText(this, "Teacher added successfully", Toast.LENGTH_SHORT).show();
            clearFields();
        } else {
            Toast.makeText(this, "Failed to add teacher", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFields() {
        etName.setText("");
        etAddress.setText("");
        etDob.setText("");
        etPhone.setText("");
        etEmail.setText("");
        etName.requestFocus();
    }
}
