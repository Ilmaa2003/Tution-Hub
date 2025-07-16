package com.nibm.tutionmanagement;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class EditTeacherBottomSheet extends BottomSheetDialogFragment {

    private TeacherDB teacher;
    private TeacherDatabaseHelper dbHelper;
    private OnUserUpdatedListener listener;  // Listener field

    public EditTeacherBottomSheet(TeacherDB teacher) {
        this.teacher = teacher;
    }

    // Setter for listener
    public void setOnUserUpdatedListener(OnUserUpdatedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_teacher, container, false);

        EditText etName = view.findViewById(R.id.et_edit_name);
        EditText etAddress = view.findViewById(R.id.et_edit_address);
        EditText etDob = view.findViewById(R.id.et_edit_dob);
        EditText etPhone = view.findViewById(R.id.et_edit_phone);
        EditText etEmail = view.findViewById(R.id.et_edit_email);

        Button btnSave = view.findViewById(R.id.btn_save_changes);

        // Set current values
        etName.setText(teacher.getName());
        etAddress.setText(teacher.getAddress());
        etDob.setText(teacher.getDob());
        etPhone.setText(teacher.getPhone());
        etEmail.setText(teacher.getEmail());

        dbHelper = new TeacherDatabaseHelper(requireContext());

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String dob = etDob.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            // Validation checks
            if (name.isEmpty()) {
                etName.setError("Name required");
                etName.requestFocus();
                return;
            }

            if (address.isEmpty()) {
                etAddress.setError("Address required");
                etAddress.requestFocus();
                return;
            }

            if (dob.isEmpty()) {
                etDob.setError("DOB required");
                etDob.requestFocus();
                return;
            }

            if (!dob.matches("\\d{4}-\\d{2}-\\d{2}")) {
                etDob.setError("DOB must be in YYYY-MM-DD format");
                etDob.requestFocus();
                return;
            }

            if (!Patterns.PHONE.matcher(phone).matches() || phone.length() < 10) {
                etPhone.setError("Invalid phone");
                etPhone.requestFocus();
                return;
            }

            if (email.isEmpty()) {
                etEmail.setError("Email required");
                etEmail.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Invalid email");
                etEmail.requestFocus();
                return;
            }

            // Check email uniqueness only if changed
            if (!email.equals(teacher.getEmail()) && dbHelper.isEmailExists(email)) {
                etEmail.setError("Email already exists");
                etEmail.requestFocus();
                return;
            }

            // Update teacher object
            teacher.setName(name);
            teacher.setAddress(address);
            teacher.setDob(dob);
            teacher.setPhone(phone);
            teacher.setEmail(email);
            teacher.setRole("Teacher"); // fixed role

            boolean updated = dbHelper.updateTeacher(teacher);

            if (updated) {
                Toast.makeText(requireContext(), "Teacher updated", Toast.LENGTH_SHORT).show();
                // Notify listener
                if (listener != null) {
                    listener.onUserUpdated();
                }
                dismiss();
            } else {
                Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    // Listener interface for notifying updates
    public interface OnUserUpdatedListener {
        void onUserUpdated();
    }
}
