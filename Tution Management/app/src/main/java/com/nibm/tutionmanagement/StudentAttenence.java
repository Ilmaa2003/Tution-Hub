package com.nibm.tutionmanagement;

import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.HashMap;
import java.util.Map;

public class StudentAttenence extends AppCompatActivity {
    private EditText etEmailInput;
    private ImageButton btnScanQRCode;
    private TextView tvScannedData;

    private DatabaseReference attendanceRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.studentattedece);

        etEmailInput = findViewById(R.id.etEmailInput);
        btnScanQRCode = findViewById(R.id.btnScanQRCode);
        tvScannedData = findViewById(R.id.tvScannedData);

        // Firebase DB reference
        attendanceRef = FirebaseDatabase.getInstance("https://tuition-b6019-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("attendance_records");

        btnScanQRCode.setOnClickListener(v -> {
            String email = etEmailInput.getText().toString().trim();

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email before scanning", Toast.LENGTH_SHORT).show();
                return;
            }

            ScanOptions options = new ScanOptions();
            options.setPrompt("Scan a QR code");
            options.setBeepEnabled(true);
            options.setOrientationLocked(true);
            options.setCaptureActivity(CaptureActivityPortrait.class);

            barcodeLauncher.launch(options);
        });
    }

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(), result -> {
        if(result.getContents() != null){
            String scannedData = result.getContents();
            tvScannedData.setText("Scanned QR Code: " + scannedData);

            String email = etEmailInput.getText().toString().trim();

            Map<String, Object> record = new HashMap<>();
            record.put("email", email);
            record.put("qrData", scannedData);
            record.put("timestamp", System.currentTimeMillis());

            String recordId = attendanceRef.push().getKey();
            if(recordId != null){
                attendanceRef.child(recordId).setValue(record)
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Attendance recorded", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed: "+e.getMessage(), Toast.LENGTH_LONG).show());
            }

        } else {
            Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
        }
    });
}
