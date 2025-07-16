package com.nibm.tutionmanagement;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StudentResults extends AppCompatActivity {

    private EditText etEmail;
    private Button btnLoadResults;
    private TextView tvResults;
    private DatabaseReference dbGradesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.studentresults);  // your XML layout file name

        etEmail = findViewById(R.id.et_email);
        btnLoadResults = findViewById(R.id.btn_load_results);
        tvResults = findViewById(R.id.tv_results);

        // Initialize Firebase database reference to "studentGrades"

        // Initialize Firebase with your custom DB URL
        dbGradesRef = FirebaseDatabase.getInstance(
                        "https://tuition-b6019-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("studentGrades");

        btnLoadResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailInput = etEmail.getText().toString().trim();
                if (TextUtils.isEmpty(emailInput)) {
                    Toast.makeText(StudentResults.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    return;
                }
                loadGradesByEmail(emailInput);
            }
        });
    }

    private void loadGradesByEmail(final String email) {
        tvResults.setText("Loading...");

        // Query to find entries where studentEmail equals the entered email
        dbGradesRef.orderByChild("studentEmail").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            StringBuilder results = new StringBuilder();
                            for (DataSnapshot child : snapshot.getChildren()) {
                                String subject = child.child("examName").getValue(String.class);
                                String grade = child.child("grade").getValue(String.class);

                                results.append("examName: ").append(subject == null ? "N/A" : subject).append("\n");
                                results.append("Grade: ").append(grade == null ? "N/A" : grade).append("\n\n");
                            }
                            tvResults.setText(results.toString());
                        } else {
                            tvResults.setText("No results found for " + email);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvResults.setText("Error loading results: " + error.getMessage());
                    }
                });
    }
}
