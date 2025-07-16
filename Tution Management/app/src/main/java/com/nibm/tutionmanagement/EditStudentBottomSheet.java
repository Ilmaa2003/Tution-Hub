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

public class EditStudentBottomSheet extends BottomSheetDialogFragment {

    private StudentDB student;
    private StudentDatabaseHelper dbHelper;
    private OnUserUpdatedListener listener;

    public EditStudentBottomSheet(StudentDB student) {
        this.student = student;
    }

    // Setter for update listener
    public void setOnUserUpdatedListener(OnUserUpdatedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_student, container, false);

        EditText etName = view.findViewById(R.id.et_edit_name);
        EditText etAddress = view.findViewById(R.id.et_edit_address);
        EditText etDob = view.findViewById(R.id.et_edit_dob);
        EditText etPhone = view.findViewById(R.id.et_edit_phone);
        EditText etEmail = view.findViewById(R.id.et_edit_email);
        EditText etParentPhone = view.findViewById(R.id.et_edit_parent_phone);
        EditText etParentEmail = view.findViewById(R.id.et_edit_parent_email);
        Button btnSave = view.findViewById(R.id.btn_save_changes);

        dbHelper = new StudentDatabaseHelper(requireContext());

        if (student != null) {
            etName.setText(student.getName());
            etAddress.setText(student.getAddress());
            etDob.setText(student.getDob());
            etPhone.setText(student.getPhone());
            etEmail.setText(student.getEmail());
            etParentPhone.setText(student.getParentPhone());
            etParentEmail.setText(student.getParentEmail());
        }

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String dob = etDob.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String parentPhone = etParentPhone.getText().toString().trim();
            String parentEmail = etParentEmail.getText().toString().trim();

            // Basic validations
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

            if (!Patterns.PHONE.matcher(phone).matches()) {
                etPhone.setError("Enter a valid phone number");
                etPhone.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Enter a valid email address");
                etEmail.requestFocus();
                return;
            }

            if (!email.equals(student.getEmail()) && dbHelper.isEmailExists(email)) {
                etEmail.setError("Email already used by another student");
                etEmail.requestFocus();
                return;
            }

            if (!Patterns.PHONE.matcher(parentPhone).matches()) {
                etParentPhone.setError("Enter a valid parent's phone number");
                etParentPhone.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(parentEmail).matches()) {
                etParentEmail.setError("Enter a valid parent's email address");
                etParentEmail.requestFocus();
                return;
            }

            // Update student object
            student.setName(name);
            student.setAddress(address);
            student.setDob(dob);
            student.setPhone(phone);
            student.setEmail(email);
            student.setParentPhone(parentPhone);
            student.setParentEmail(parentEmail);

            boolean updated = dbHelper.updateStudent(student);

            if (updated) {
                Toast.makeText(requireContext(), "Student updated successfully", Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onUserUpdated();
                }
                dismiss();
            } else {
                Toast.makeText(requireContext(), "Failed to update student", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    public interface OnUserUpdatedListener {
        void onUserUpdated();
    }
}
