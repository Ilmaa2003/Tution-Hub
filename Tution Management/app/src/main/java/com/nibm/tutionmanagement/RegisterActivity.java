package com.nibm.tutionmanagement;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private static final String ADMIN_PASSWORD = "Admin@123";  // Hardcoded admin password

    private TextInputEditText edtFullName, edtEmail, edtPassword, edtConfirmPassword;
    private Button btnRegister;

    private UserDBHelper userDbHelper;
    private TeacherDatabaseHelper teacherDbHelper;
    private StudentDatabaseHelper studentDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userDbHelper = new UserDBHelper(this);
        teacherDbHelper = new TeacherDatabaseHelper(this);
        studentDbHelper = new StudentDatabaseHelper(this);

        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);

        // Disable registration UI initially
        setRegistrationEnabled(false);

        // Show admin password dialog first
        showAdminPasswordDialog();

        btnRegister.setOnClickListener(v -> {
            if (validateInputs()) {
                registerAdmin();
            }
        });
    }

    private void setRegistrationEnabled(boolean enabled) {
        edtFullName.setEnabled(enabled);
        edtEmail.setEnabled(enabled);
        edtPassword.setEnabled(enabled);
        edtConfirmPassword.setEnabled(enabled);
        btnRegister.setEnabled(enabled);
    }

    private void showAdminPasswordDialog() {
        final EditText input = new EditText(this);
        input.setHint("Enter admin password");
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new AlertDialog.Builder(this)
                .setTitle("Admin Authentication")
                .setMessage("Please enter admin password to access registration.")
                .setView(input)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    String enteredPassword = input.getText().toString().trim();
                    if (enteredPassword.equals(ADMIN_PASSWORD)) {
                        Toast.makeText(RegisterActivity.this, "Access granted.", Toast.LENGTH_SHORT).show();
                        setRegistrationEnabled(true);
                    } else {
                        Toast.makeText(RegisterActivity.this, "Incorrect password. Access denied.", Toast.LENGTH_LONG).show();
                        finish();  // Close the activity if wrong password
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Toast.makeText(RegisterActivity.this, "Access denied.", Toast.LENGTH_LONG).show();
                    finish();
                })
                .show();
    }

    private boolean validateInputs() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString();
        String confirmPassword = edtConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(fullName)) {
            edtFullName.setError("Full name is required");
            edtFullName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Email is required");
            edtEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Invalid email format");
            edtEmail.requestFocus();
            return false;
        }

        if (!isEmailUnique(email)) {
            edtEmail.setError("Email already registered");
            edtEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Password is required");
            edtPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            edtPassword.setError("Password must be at least 6 characters");
            edtPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError("Passwords do not match");
            edtConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private boolean isEmailUnique(String email) {
        return !userDbHelper.isEmailExists(email)
                && !teacherDbHelper.isEmailExists(email)
                && !studentDbHelper.isEmailExists(email);
    }

    private void registerAdmin() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString();

        boolean success = userDbHelper.addUser(fullName, email, password);

        if (success) {
            Toast.makeText(this, "Admin registered successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Registration failed. Try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
