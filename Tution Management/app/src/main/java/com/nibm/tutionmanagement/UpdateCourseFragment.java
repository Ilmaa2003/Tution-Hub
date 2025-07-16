package com.nibm.tutionmanagement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class UpdateCourseFragment extends Fragment {

    private Spinner spinnerCourse, spinnerGrade, spinnerBatch;
    private RecyclerView recyclerCourses;

    private CourseDBHelper dbHelper;
    private CourseCardAdapter adapter;

    private List<CourseAssignment> filteredCourses = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_update_course, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinnerCourse = view.findViewById(R.id.spinner_filter_course);
        spinnerGrade = view.findViewById(R.id.spinner_filter_grade);
        spinnerBatch = view.findViewById(R.id.spinner_filter_batch);
        recyclerCourses = view.findViewById(R.id.recycler_courses);

        dbHelper = CourseDBHelper.getInstance(requireContext());

        setupRecyclerView();
        loadCoursesIntoSpinner();

        spinnerCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCourse = (String) spinnerCourse.getSelectedItem();
                loadGradesForCourse(selectedCourse);
                spinnerBatch.setAdapter(null);
                filterCourses();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        spinnerGrade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCourse = (String) spinnerCourse.getSelectedItem();
                String selectedGrade = (String) spinnerGrade.getSelectedItem();
                loadBatchesForCourseAndGrade(selectedCourse, selectedGrade);
                filterCourses();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        spinnerBatch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterCourses();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void setupRecyclerView() {
        recyclerCourses.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CourseCardAdapter(requireContext(), filteredCourses);
        recyclerCourses.setAdapter(adapter);

        // Set the click listener on adapter items here
        adapter.setOnItemClickListener(course -> {
            // This method will open your bottom sheet dialog
            showEditCourseBottomSheet(course);
        });
    }

    private void loadCoursesIntoSpinner() {
        List<String> courses = dbHelper.getCourseNames();
        courses.add(0, "All");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, courses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourse.setAdapter(adapter);

        loadGradesForCourse("All");
    }

    private void loadGradesForCourse(String courseName) {
        List<String> grades;
        if ("All".equals(courseName)) {
            grades = dbHelper.getAllGrades();
        } else {
            grades = dbHelper.getGradesForCourse(courseName);
        }
        grades.add(0, "All");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, grades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGrade.setAdapter(adapter);

        spinnerBatch.setAdapter(null);
    }

    private void loadBatchesForCourseAndGrade(String courseName, String grade) {
        List<String> batches;
        if ("All".equals(courseName) && "All".equals(grade)) {
            batches = dbHelper.getAllBatches();
        } else if ("All".equals(courseName)) {
            batches = dbHelper.getBatchesForGrade(grade);
        } else if ("All".equals(grade)) {
            batches = dbHelper.getBatchesForCourse(courseName);
        } else {
            batches = dbHelper.getBatchesForCourseAndGrade(courseName, grade);
        }
        batches.add(0, "All");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, batches);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBatch.setAdapter(adapter);
    }

    private void filterCourses() {
        String course = (String) spinnerCourse.getSelectedItem();
        String grade = (String) spinnerGrade.getSelectedItem();
        String batch = (String) spinnerBatch.getSelectedItem();

        filteredCourses.clear();

        List<CourseAssignment> allCourses = dbHelper.getAllCourseAssignments();

        for (CourseAssignment ca : allCourses) {
            boolean matchesCourse = (course == null || course.equals("All") || ca.getCourseName().equals(course));
            boolean matchesGrade = (grade == null || grade.equals("All") || ca.getGrade().equals(grade));
            boolean matchesBatch = (batch == null || batch.equals("All") || ca.getBatch().equals(batch));

            if (matchesCourse && matchesGrade && matchesBatch) {
                filteredCourses.add(ca);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void showEditCourseBottomSheet(CourseAssignment course) {

        CourseAssignmentFull full = dbHelper.getCourseAssignmentFullDetails(course.getAssignmentId());
        EditCourseBottomSheet bottomSheet = new EditCourseBottomSheet(full);
        bottomSheet.setOnCourseUpdatedListener(() -> filterCourses());
        bottomSheet.show(getParentFragmentManager(), "EditCourseBottomSheet");

    }
    }
