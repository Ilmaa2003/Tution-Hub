package com.nibm.tutionmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class adminpayment extends AppCompatActivity {

    private Spinner spinnerCourse, spinnerGrade, spinnerBatch, spinnerMonth;
    private RecyclerView recyclerView;
    private PaymentAdapter adapter;
    private List<NewPaymentFragment.PaymentData> paymentList = new ArrayList<>();
    private DrawerLayout drawerLayout;


    private List<String> courses = new ArrayList<>();
    private List<String> grades = new ArrayList<>();
    private List<String> batches = new ArrayList<>();
    private List<String> months = new ArrayList<>();

    private String selectedCourse = "All";
    private String selectedGrade = "All";
    private String selectedBatch = "All";
    private String selectedMonth = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_payment);

        drawerLayout = findViewById(R.id.drawer_layout);

        recyclerView = findViewById(R.id.recyclerPayments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PaymentAdapter(this, paymentList);
        recyclerView.setAdapter(adapter);

        spinnerCourse = findViewById(R.id.spinnerCourse);
        spinnerGrade = findViewById(R.id.spinnerGrade);
        spinnerBatch = findViewById(R.id.spinnerBatch);
        spinnerMonth = findViewById(R.id.spinnerMonth);

        loadAndPopulateSpinners();


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




        findViewById(R.id.nav_dashboard).setOnClickListener(v -> {
            drawerLayout.closeDrawer(findViewById(R.id.sidebar), false);
            startActivity(new Intent(this, MainActivity.class));
        });
    }

    private void loadAndPopulateSpinners() {
        FirebaseDatabase.getInstance("https://tuition-b6019-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("payments")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Set<String> courseSet = new HashSet<>();
                        Set<String> gradeSet = new HashSet<>();
                        Set<String> batchSet = new HashSet<>();
                        Set<String> monthSet = new HashSet<>();

                        for (DataSnapshot snapshot : task.getResult().getChildren()) {
                            NewPaymentFragment.PaymentData payment = snapshot.getValue(NewPaymentFragment.PaymentData.class);
                            if (payment != null) {
                                courseSet.add(payment.getCourse());
                                gradeSet.add(payment.getGrade());
                                batchSet.add(payment.getBatch());
                                monthSet.add(payment.getMonth());
                            }
                        }

                        // Add default "All" option and convert sets to sorted lists
                        courses = new ArrayList<>();
                        grades = new ArrayList<>();
                        batches = new ArrayList<>();
                        months = new ArrayList<>();

                        courses.add("All");
                        grades.add("All");
                        batches.add("All");
                        months.add("All");

                        courses.addAll(courseSet);
                        grades.addAll(gradeSet);
                        batches.addAll(batchSet);
                        months.addAll(monthSet);

                        // Set spinner adapters
                        setSpinner(spinnerCourse, courses, selected -> {
                            selectedCourse = selected;
                            loadFilteredPayments();
                        });

                        setSpinner(spinnerGrade, grades, selected -> {
                            selectedGrade = selected;
                            loadFilteredPayments();
                        });

                        setSpinner(spinnerBatch, batches, selected -> {
                            selectedBatch = selected;
                            loadFilteredPayments();
                        });

                        setSpinner(spinnerMonth, months, selected -> {
                            selectedMonth = selected;
                            loadFilteredPayments();
                        });

                        loadFilteredPayments(); // Load initially
                    } else {
                        Toast.makeText(this, "Failed to load filters", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setSpinner(Spinner spinner, List<String> items, SpinnerSelectionListener listener) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                listener.onItemSelected(items.get(position));
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadFilteredPayments() {
        FirebaseDatabase.getInstance("https://tuition-b6019-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("payments")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        paymentList.clear();
                        for (DataSnapshot snapshot : task.getResult().getChildren()) {
                            NewPaymentFragment.PaymentData payment = snapshot.getValue(NewPaymentFragment.PaymentData.class);
                            if (payment != null && matchesFilter(payment)) {
                                paymentList.add(payment);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to load payments", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean matchesFilter(NewPaymentFragment.PaymentData payment) {
        return (selectedCourse.equals("All") || selectedCourse.equalsIgnoreCase(payment.getCourse())) &&
                (selectedGrade.equals("All") || selectedGrade.equalsIgnoreCase(payment.getGrade())) &&
                (selectedBatch.equals("All") || selectedBatch.equalsIgnoreCase(payment.getBatch())) &&
                (selectedMonth.equals("All") || selectedMonth.equalsIgnoreCase(payment.getMonth()));
    }

    interface SpinnerSelectionListener {
        void onItemSelected(String selected);
    }
}
