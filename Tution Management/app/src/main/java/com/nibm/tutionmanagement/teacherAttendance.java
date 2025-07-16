package com.nibm.tutionmanagement;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.util.Calendar;
import java.util.List;

public class teacherAttendance extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageButton btnToggle;
    private LinearLayout sidebar;
    private Spinner spinnerCourse, spinnerGrade, spinnerBatch;
    private EditText etSelectDate;
    private Button btnGenerateQR;
    private ImageView ivGeneratedQR;

    private String selectedDate = "";

    private CourseDBHelper dbHelper;
    private DatabaseReference attendanceRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacherattendance);

        drawerLayout = findViewById(R.id.drawer_layout);
        btnToggle = findViewById(R.id.btnToggle);
        sidebar = findViewById(R.id.sidebar);

        spinnerCourse = findViewById(R.id.spinner_filter_course);
        spinnerGrade = findViewById(R.id.spinner_filter_grade);
        spinnerBatch = findViewById(R.id.spinner_filter_batch);
        etSelectDate = findViewById(R.id.etSelectDate);
        btnGenerateQR = findViewById(R.id.btnGenerateQR);
        ivGeneratedQR = findViewById(R.id.ivGeneratedQR);

        dbHelper = CourseDBHelper.getInstance(this);
        attendanceRef = FirebaseDatabase.getInstance("https://tuition-b6019-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("AttendanceQRs");

        btnToggle.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(sidebar)) {
                drawerLayout.closeDrawer(sidebar);
            } else {
                drawerLayout.openDrawer(sidebar);
            }
        });

        List<String> courses = dbHelper.getCourseNames();
        courses.add(0, "Select Course");
        setupSpinner(spinnerCourse, courses.toArray(new String[0]));

        setupSpinner(spinnerGrade, new String[]{"Select Grade"});
        setupSpinner(spinnerBatch, new String[]{"Select Batch"});

        spinnerCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    setupSpinner(spinnerGrade, new String[]{"Select Grade"});
                    setupSpinner(spinnerBatch, new String[]{"Select Batch"});
                    return;
                }
                String selectedCourse = (String) parent.getItemAtPosition(position);
                List<String> grades = dbHelper.getGradesForCourse(selectedCourse);
                grades.add(0, "Select Grade");
                setupSpinner(spinnerGrade, grades.toArray(new String[0]));
                setupSpinner(spinnerBatch, new String[]{"Select Batch"});
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerGrade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    setupSpinner(spinnerBatch, new String[]{"Select Batch"});
                    return;
                }
                String selectedGrade = (String) parent.getItemAtPosition(position);
                String selectedCourse = (String) spinnerCourse.getSelectedItem();

                List<String> batches = dbHelper.getBatchesForCourseAndGrade(selectedCourse, selectedGrade);
                batches.add(0, "Select Batch");
                setupSpinner(spinnerBatch, batches.toArray(new String[0]));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        etSelectDate.setOnClickListener(v -> showDatePickerDialog());

        btnGenerateQR.setOnClickListener(v -> generateQRCode());

        for (int i = 0; i < sidebar.getChildCount(); i++) {
            View child = sidebar.getChildAt(i);
            if (child instanceof TextView) {
                child.setOnClickListener(v -> {
                    String menu = ((TextView) v).getText().toString();
                    Toast.makeText(this, menu + " clicked", Toast.LENGTH_SHORT).show();
                    drawerLayout.closeDrawer(sidebar);
                });
            }
        }
    }

    private void setupSpinner(Spinner spinner, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                items
        );
        spinner.setAdapter(adapter);
    }

    private void showDatePickerDialog() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                    etSelectDate.setText(selectedDate);
                },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void generateQRCode() {
        String course = spinnerCourse.getSelectedItem().toString();
        String grade = spinnerGrade.getSelectedItem().toString();
        String batch = spinnerBatch.getSelectedItem().toString();

        if (course.startsWith("Select") || grade.startsWith("Select") || batch.startsWith("Select") || selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select all fields and date", Toast.LENGTH_SHORT).show();
            return;
        }

        String qrData = "Course:" + course + ";Grade:" + grade + ";Batch:" + batch + ";Date:" + selectedDate;

        try {
            Bitmap qrBitmap = encodeAsBitmap(qrData);
            ivGeneratedQR.setImageBitmap(qrBitmap);

            // Send to Firebase
            String id = attendanceRef.push().getKey();
            if (id != null) {
                attendanceRef.child(id).setValue(qrData)
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "QR data uploaded", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

        } catch (Exception e) {
            Toast.makeText(this, "Failed to generate QR: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private Bitmap encodeAsBitmap(String content) throws Exception {
        int size = 512;
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size);
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                bmp.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return bmp;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(sidebar)) {
            drawerLayout.closeDrawer(sidebar);
        } else {
            super.onBackPressed();
        }
    }
}
