package com.nibm.tutionmanagement;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NewPaymentFragment extends Fragment {

    private Spinner spinnerCourse, spinnerGrade, spinnerBatch, spinnerTeacher, spinnerMonth, spinnerYear;
    private EditText etDescription;
    private ImageView imgPaymentSlip;
    private Button btnSubmit, btnRemoveImage;
    private Uri imageUri; // this will store the copied URI inside app storage

    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String[]> permissionLauncher;

    private CourseDBHelper dbHelper;

    private FirebaseDatabase database;
    private DatabaseReference paymentsRef;

    private static final String TAG = "NewPaymentFragment";

    // Logged-in user's email from SharedPreferences
    private String loggedInEmail;

    public NewPaymentFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = CourseDBHelper.getInstance(requireContext());

        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        loggedInEmail = prefs.getString("email", "unknown@example.com");

        database = FirebaseDatabase.getInstance("https://tuition-b6019-default-rtdb.asia-southeast1.firebasedatabase.app");
        paymentsRef = database.getReference("payments");

        spinnerCourse = view.findViewById(R.id.spinner_course);
        spinnerGrade = view.findViewById(R.id.spinner_grade);
        spinnerBatch = view.findViewById(R.id.spinner_batch);
        spinnerTeacher = view.findViewById(R.id.spinner_teacher);
        spinnerMonth = view.findViewById(R.id.spinner_month);
        spinnerYear = view.findViewById(R.id.spinner_year);
        etDescription = view.findViewById(R.id.et_description);
        imgPaymentSlip = view.findViewById(R.id.img_payment_slip);
        btnSubmit = view.findViewById(R.id.btn_submit);
        btnRemoveImage = view.findViewById(R.id.btn_remove_image);

        btnRemoveImage.setVisibility(View.GONE);

        List<String> courseList = dbHelper.getCourseNames();
        spinnerCourse.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, courseList));

        List<String> months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) months.add(String.format("%02d", i));
        spinnerMonth.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, months));

        List<String> years = new ArrayList<>();
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = thisYear; i <= thisYear + 3; i++) years.add(String.valueOf(i));
        spinnerYear.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, years));

        spinnerCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                String selectedCourse = (String) spinnerCourse.getSelectedItem();
                if (selectedCourse != null) {
                    List<String> grades = dbHelper.getGradesForCourse1(selectedCourse);
                    spinnerGrade.setAdapter(new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_dropdown_item, grades));
                    spinnerBatch.setAdapter(null);
                    spinnerTeacher.setAdapter(null);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerGrade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                String selectedCourse = spinnerCourse.getSelectedItem() != null ? spinnerCourse.getSelectedItem().toString() : "";
                String selectedGrade = spinnerGrade.getSelectedItem() != null ? spinnerGrade.getSelectedItem().toString() : "";

                if (!selectedCourse.isEmpty() && !selectedGrade.isEmpty()) {
                    List<String> batches = dbHelper.getBatchesForCourseAndGrade(selectedCourse, selectedGrade);
                    spinnerBatch.setAdapter(new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_dropdown_item, batches));

                    List<Integer> teacherIds = dbHelper.getTeacherIdsForCourseAndGrade(selectedCourse, selectedGrade);
                    if (teacherIds != null && !teacherIds.isEmpty()) {
                        TeacherDatabaseHelper teacherDbHelper = new TeacherDatabaseHelper(requireContext());
                        List<String> teachers = teacherDbHelper.getTeacherNamesWithEmailByIds(teacherIds);
                        spinnerTeacher.setAdapter(new ArrayAdapter<>(requireContext(),
                                android.R.layout.simple_spinner_dropdown_item, teachers));
                    } else {
                        spinnerTeacher.setAdapter(null);
                    }
                } else {
                    spinnerBatch.setAdapter(null);
                    spinnerTeacher.setAdapter(null);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spinnerBatch.setAdapter(null);
                spinnerTeacher.setAdapter(null);
            }
        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri originalUri = result.getData().getData();
                        Uri copiedUri = copyImageToInternalStorage(originalUri);
                        if (copiedUri != null) {
                            imageUri = copiedUri;
                            imgPaymentSlip.setImageURI(imageUri);
                            btnRemoveImage.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(requireContext(), "Failed to copy image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // imageUri is already set in openCamera() before launching
                        Uri copiedUri = copyImageToInternalStorage(imageUri);
                        if (copiedUri != null) {
                            imageUri = copiedUri;
                            imgPaymentSlip.setImageURI(imageUri);
                            btnRemoveImage.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(requireContext(), "Failed to copy camera image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean cameraGranted = result.getOrDefault(Manifest.permission.CAMERA, false);
                    boolean storageGranted = result.getOrDefault(Manifest.permission.WRITE_EXTERNAL_STORAGE, false);
                    if (cameraGranted && storageGranted) openCamera();
                    else Toast.makeText(requireContext(),
                            "Camera and Storage permissions are required to take photo", Toast.LENGTH_SHORT).show();
                });

        imgPaymentSlip.setOnClickListener(v -> showImagePickDialog());

        btnRemoveImage.setOnClickListener(v -> {
            imageUri = null;
            imgPaymentSlip.setImageResource(R.drawable.ic_camera_alt);
            btnRemoveImage.setVisibility(View.GONE);
        });

        btnSubmit.setOnClickListener(v -> submitPayment());
    }

    private void submitPayment() {
        String course = getSelected(spinnerCourse);
        String grade = getSelected(spinnerGrade);
        String batch = getSelected(spinnerBatch);
        String teacher = getSelected(spinnerTeacher);
        String month = getSelected(spinnerMonth);
        String year = getSelected(spinnerYear);

        String descInput = etDescription.getText().toString().trim();
        String desc = descInput.isEmpty() ? "No description" : descInput;

        if (course.isEmpty() || grade.isEmpty() || batch.isEmpty() || teacher.isEmpty() || imageUri == null) {
            Toast.makeText(requireContext(), "Please fill all required fields and attach a payment slip", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);

        String localImageUri = imageUri.toString();

        PaymentData paymentData = new PaymentData(course, grade, batch, teacher, month, year, desc, localImageUri, loggedInEmail);

        Log.d(TAG, "Submitting payment to Firebase: " + paymentData);

        String paymentId = paymentsRef.push().getKey();
        if (paymentId != null) {
            paymentsRef.child(paymentId).setValue(paymentData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), "Payment submitted successfully!", Toast.LENGTH_SHORT).show();
                        resetForm();
                        btnSubmit.setEnabled(true);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Firebase submission failed: ", e);
                        Toast.makeText(requireContext(), "Failed to submit payment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnSubmit.setEnabled(true);
                    });
        } else {
            Toast.makeText(requireContext(), "Failed to generate payment ID", Toast.LENGTH_SHORT).show();
            btnSubmit.setEnabled(true);
        }
    }

    private void resetForm() {
        spinnerCourse.setSelection(0);
        spinnerGrade.setAdapter(null);
        spinnerBatch.setAdapter(null);
        spinnerTeacher.setAdapter(null);
        spinnerMonth.setSelection(0);
        spinnerYear.setSelection(0);
        etDescription.setText("");
        imageUri = null;
        imgPaymentSlip.setImageResource(R.drawable.ic_camera_alt);
        btnRemoveImage.setVisibility(View.GONE);
    }

    private String getSelected(Spinner spinner) {
        return spinner.getSelectedItem() != null ? spinner.getSelectedItem().toString() : "";
    }

    private void showImagePickDialog() {
        String[] options = {"Choose from Gallery", "Take a Photo"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Attach Payment Slip")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) pickFromGallery();
                    else checkCameraPermissionsAndOpen();
                })
                .show();
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        galleryLauncher.launch(Intent.createChooser(intent, "Select Payment Slip"));
    }

    private void checkCameraPermissionsAndOpen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean cameraPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
            boolean storagePermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

            if (cameraPermission && storagePermission) openCamera();
            else permissionLauncher.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Payment Slip");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        imageUri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraLauncher.launch(intent);
    }

    /**
     * Copies the image pointed to by sourceUri into the app's internal storage directory and returns
     * a new URI pointing to the copied file.
     */
    private Uri copyImageToInternalStorage(Uri sourceUri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(sourceUri);
            File dir = new File(requireContext().getFilesDir(), "payment_images");
            if (!dir.exists()) dir.mkdirs();

            String filename = "img_" + System.currentTimeMillis() + ".jpg";
            File copiedFile = new File(dir, filename);

            OutputStream outputStream = new FileOutputStream(copiedFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            return Uri.fromFile(copiedFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static class PaymentData {
        private String course;
        private String grade;
        private String batch;
        private String teacher;
        private String month;
        private String year;
        private String description;
        private String paymentSlipUrl;
        private String email;

        public PaymentData() {}

        public PaymentData(String course, String grade, String batch, String teacher,
                           String month, String year, String description, String paymentSlipUrl,
                           String email) {
            this.course = course;
            this.grade = grade;
            this.batch = batch;
            this.teacher = teacher;
            this.month = month;
            this.year = year;
            this.description = description;
            this.paymentSlipUrl = paymentSlipUrl;
            this.email = email;
        }

        // Getters
        public String getCourse() { return course; }
        public String getGrade() { return grade; }
        public String getBatch() { return batch; }
        public String getTeacher() { return teacher; }
        public String getMonth() { return month; }
        public String getYear() { return year; }
        public String getDescription() { return description; }
        public String getPaymentSlipUrl() { return paymentSlipUrl; }
        public String getEmail() { return email; }

        // Setters
        public void setCourse(String course) { this.course = course; }
        public void setGrade(String grade) { this.grade = grade; }
        public void setBatch(String batch) { this.batch = batch; }
        public void setTeacher(String teacher) { this.teacher = teacher; }
        public void setMonth(String month) { this.month = month; }
        public void setYear(String year) { this.year = year; }
        public void setDescription(String description) { this.description = description; }
        public void setPaymentSlipUrl(String paymentSlipUrl) { this.paymentSlipUrl = paymentSlipUrl; }
        public void setEmail(String email) { this.email = email; }

        @Override
        public String toString() {
            return "PaymentData{" +
                    "course='" + course + '\'' +
                    ", grade='" + grade + '\'' +
                    ", batch='" + batch + '\'' +
                    ", teacher='" + teacher + '\'' +
                    ", month='" + month + '\'' +
                    ", year='" + year + '\'' +
                    ", description='" + description + '\'' +
                    ", paymentSlipUrl='" + paymentSlipUrl + '\'' +
                    ", email='" + email + '\'' +
                    '}';
        }
    }

}
