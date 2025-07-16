package com.nibm.tutionmanagement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;
import java.util.Random;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtEmail, edtPassword;
    private Button btnLogin;

    private TextView tvCreateAccount;
    private UserDBHelper userDbHelper;
    private OtherUsersDBHelper otherUsersDbHelper;
    private TeacherDatabaseHelper teacherDbHelper;
    private StudentDatabaseHelper studentDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userDbHelper = new UserDBHelper(this);
        otherUsersDbHelper = new OtherUsersDBHelper(this);
        teacherDbHelper = new TeacherDatabaseHelper(this);
        studentDbHelper = new StudentDatabaseHelper(this);

        edtEmail = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString();

            if (email.isEmpty()) {
                edtEmail.setError("Email is required");
                edtEmail.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                edtPassword.setError("Password is required");
                edtPassword.requestFocus();
                return;
            }

            handleLogin(email, password);
        });

        TextView tvCreateAccount = findViewById(R.id.tvCreateAccount);
        tvCreateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class); // Replace with your activity
            startActivity(intent);
        });
    }



    private void handleLogin(String email, String password) {
        // 1. Check if admin in users table
        if (userDbHelper.validateUser(email, password)) {
            Toast.makeText(this, "Welcome Admin!", Toast.LENGTH_SHORT).show();
            openMainActivity("admin", email);
            return;
        }

        // 2. Check otherUsers table first (teachers or students with updated passwords)
        String roleInOther = otherUsersDbHelper.getRoleByEmail(email);
        if (roleInOther != null) {
            // Validate password from otherUsers table
            if (otherUsersDbHelper.validateUser(email, password)) {
                Toast.makeText(this, "Welcome " + capitalize(roleInOther) + "!", Toast.LENGTH_SHORT).show();
                openMainActivity(roleInOther, email);
            } else {
                Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // 3. Check teachers and students tables for default password "1234"
        if (teacherDbHelper.isEmailExists(email)) {
            if (password.equals("1234")) {
                // First-time teacher login: generate new password and save in otherUsers
                String newPassword = generateRandomPassword();
                boolean saved = otherUsersDbHelper.insertOrUpdateUser("teacher", email, newPassword);
                if (saved) {
                    showNewPasswordDialog("teacher", email, newPassword);
                } else {
                    Toast.makeText(this, "Failed to update password. Try again.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (studentDbHelper.isEmailExists(email)) {
            if (password.equals("1234")) {
                // First-time student login: generate new password and save in otherUsers
                String newPassword = generateRandomPassword();
                boolean saved = otherUsersDbHelper.insertOrUpdateUser("student", email, newPassword);
                if (saved) {
                    showNewPasswordDialog("student", email, newPassword);
                } else {
                    Toast.makeText(this, "Failed to update password. Try again.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // If no matches
        Toast.makeText(this, "User not found. Please register or check credentials.", Toast.LENGTH_SHORT).show();
    }

    private void openMainActivity(String role, String email) {
        // Get user's name from relevant DB based on role
        String name = "User";
        switch (role.toLowerCase(Locale.ROOT)) {
            case "admin":
                name = userDbHelper.getNameByEmail(email);
                break;
            case "teacher":
                name = teacherDbHelper.getNameByEmail(email);
                break;
            case "student":
                name = studentDbHelper.getNameByEmail(email);
                break;
        }

        // Save role, email, and name to SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("role", role);
        editor.putString("email", email);
        editor.putString("name", name);
        editor.apply();

        Intent intent;

        // Redirect based on role
        switch (role.toLowerCase(Locale.ROOT)) {
            case "admin":
                intent = new Intent(LoginActivity.this, MainActivity.class);
                break;
            case "teacher":
                intent = new Intent(LoginActivity.this, TeacherMain.class);
                break;
            case "student":
                intent = new Intent(LoginActivity.this, StudentMain.class);
                break;
            default:
                // fallback to a generic main activity
                intent = new Intent(LoginActivity.this, MainActivity.class);
                break;
        }

        intent.putExtra("role", role);
        intent.putExtra("email", email);
        startActivity(intent);
        finish();
    }

    private void showNewPasswordDialog(String role, String email, String newPassword) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Password Generated");
        builder.setMessage(String.format(Locale.getDefault(),
                "Welcome %s!\n\nYour new password is:\n\n%s\n\nPlease use this password to login next time.",
                capitalize(role), newPassword));
        builder.setCancelable(false);
        builder.setPositiveButton("OK", (dialog, which) -> openMainActivity(role, email));
        builder.show();
    }

    private String generateRandomPassword() {
        Random random = new Random();
        int number = 100000 + random.nextInt(900000); // 6-digit number between 100000-999999
        return String.valueOf(number);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
