package com.nibm.tutionmanagement;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StartCourseFragment extends Fragment {

    // UI Elements
    private Spinner spinnerCourse, spinnerGrade, spinnerBatch, spinnerTeacher, spinnerDayOfWeek;
    private EditText etDescription;
    private ListView listViewStudents, listViewExtraClasses , listViewCustomizedClasses;
    private Button btnAddCourse;
    private ImageButton btnAddCourseNew, btnAddBatch, btnAddTeacher, btnAddStudent;

    private RadioGroup radioGroupSchedule;
    private RadioButton radioFixed, radioCustomized;
    private View layoutFixedSchedule, layoutCustomizedSchedule;
    private TimePicker timepickerStart, timepickerEnd;
    private CalendarView calendarExtraClasses, calendarCustomized;

    // DB Helpers (Assuming these classes are implemented)
    private CourseDBHelper courseDB;
    private TeacherDatabaseHelper teacherDB;
    private StudentDatabaseHelper studentDB;

    // Adapters
    private ArrayAdapter<String> courseAdapter, gradeAdapter, batchAdapter, teacherAdapter, studentAdapter, dayOfWeekAdapter, extraClassAdapter, customizedClassAdapter;

    // Draft prefs key
    private SharedPreferences draftPrefs;
    private static final String PREFS_NAME = "course_draft";

    // Temporary maps to store multiple extra and customized classes
    private final Map<Long, ExtraClassInfo> tempExtraClasses = new HashMap<>();
    private final Map<Long, ExtraClassInfo> tempCustomizedClasses = new HashMap<>();

    // Data holder for extra/customized class info (time + description)
    private static class ExtraClassInfo {
        int startHour, startMinute, endHour, endMinute;
        String description;

        ExtraClassInfo(int sH, int sM, int eH, int eM, String desc) {
            this.startHour = sH;
            this.startMinute = sM;
            this.endHour = eH;
            this.endMinute = eM;
            this.description = desc;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start_course, container, false);

        Context context = requireContext();

        // Initialize DB helpers
        courseDB = CourseDBHelper.getInstance(context);
        teacherDB = new TeacherDatabaseHelper(context);
        studentDB = new StudentDatabaseHelper(context);

        draftPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Initialize UI
        spinnerCourse = view.findViewById(R.id.spinner_course);
        spinnerGrade = view.findViewById(R.id.spinner_grade);
        spinnerBatch = view.findViewById(R.id.spinner_batch);
        spinnerTeacher = view.findViewById(R.id.spinner_teacher);
        listViewStudents = view.findViewById(R.id.listview_students);
        etDescription = view.findViewById(R.id.et_description);

        btnAddCourseNew = view.findViewById(R.id.btn_add_course);
        btnAddBatch = view.findViewById(R.id.btn_add_batch);
        btnAddTeacher = view.findViewById(R.id.btn_add_teacher);
        btnAddStudent = view.findViewById(R.id.btn_add_student);
        btnAddCourse = view.findViewById(R.id.add_course);

        radioGroupSchedule = view.findViewById(R.id.radio_group_schedule);
        radioFixed = view.findViewById(R.id.radio_fixed);
        radioCustomized = view.findViewById(R.id.radio_customized);

        layoutFixedSchedule = view.findViewById(R.id.layout_fixed_schedule);
        layoutCustomizedSchedule = view.findViewById(R.id.layout_customized_schedule);

        spinnerDayOfWeek = view.findViewById(R.id.spinner_day_of_week);
        timepickerStart = view.findViewById(R.id.timepicker_start);
        timepickerEnd = view.findViewById(R.id.timepicker_end);
        calendarExtraClasses = view.findViewById(R.id.calendar_extra_classes);
        calendarCustomized = view.findViewById(R.id.calendar_customized);

        listViewExtraClasses = view.findViewById(R.id.listview_extra_classes);
        listViewCustomizedClasses = view.findViewById(R.id.listview_customized_classes);

        // Setup UI components and events
        setupSpinnersAndLists();
        setupDayOfWeekSpinner();
        setupExtraClassesList();
        setupCustomizedClassesList();
        loadDraft();
        setupListeners();

        return view;
    }

    private void setupSpinnersAndLists() {
        // Populate courses spinner
        List<String> courseNames = courseDB.getCourseNames();
        courseAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, courseNames);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourse.setAdapter(courseAdapter);

        // Grades 1 to 13
        List<String> grades = new ArrayList<>();
        for (int i = 1; i <= 13; i++) grades.add("Grade " + i);
        gradeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, grades);
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGrade.setAdapter(gradeAdapter);

        // Initialize empty batch spinner
        batchAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>());
        batchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBatch.setAdapter(batchAdapter);

        // Teachers spinner
        List<String> teachers = teacherDB.getTeacherNamesWithEmail();
        teacherAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, teachers);
        teacherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTeacher.setAdapter(teacherAdapter);

        // Students ListView with multiple choice mode
        List<String> students = studentDB.getStudentNamesWithId();
        studentAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_multiple_choice, students);
        listViewStudents.setAdapter(studentAdapter);
        listViewStudents.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // Update batches whenever course or grade changes
        spinnerCourse.setOnItemSelectedListener(new AdapterViewListener());
        spinnerGrade.setOnItemSelectedListener(new AdapterViewListener());

        // Calendar extra classes date selection
        calendarExtraClasses.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            long dateMillis = createDateMillis(year, month, dayOfMonth);
            showExtraClassDialog(dateMillis);
        });

        // Calendar customized classes date selection
        calendarCustomized.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            long dateMillis = createDateMillis(year, month, dayOfMonth);
            showCustomizedClassDialog(dateMillis);
        });
    }

    private void setupDayOfWeekSpinner() {
        List<String> days = new ArrayList<>();
        days.add("Sunday");
        days.add("Monday");
        days.add("Tuesday");
        days.add("Wednesday");
        days.add("Thursday");
        days.add("Friday");
        days.add("Saturday");

        dayOfWeekAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, days);
        dayOfWeekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDayOfWeek.setAdapter(dayOfWeekAdapter);
    }

    private void setupExtraClassesList() {
        extraClassAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, new ArrayList<>());
        listViewExtraClasses.setAdapter(extraClassAdapter);

        listViewExtraClasses.setOnItemLongClickListener((parent, view, position, id) -> {
            long keyToRemove = (long) tempExtraClasses.keySet().toArray()[position];
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Extra Class")
                    .setMessage("Delete extra class on " + android.text.format.DateFormat.format("yyyy-MM-dd", keyToRemove) + "?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        tempExtraClasses.remove(keyToRemove);
                        refreshExtraClassesList();
                    })
                    .setNegativeButton("No", null)
                    .show();
            return true;
        });

        refreshExtraClassesList();
    }

    private void refreshExtraClassesList() {
        List<String> items = new ArrayList<>();
        for (Map.Entry<Long, ExtraClassInfo> entry : tempExtraClasses.entrySet()) {
            long date = entry.getKey();
            ExtraClassInfo info = entry.getValue();

            String timeRange = String.format("%02d:%02d - %02d:%02d", info.startHour, info.startMinute, info.endHour, info.endMinute);
            String desc = (info.description != null && !info.description.isEmpty()) ? " (" + info.description + ")" : "";
            String item = android.text.format.DateFormat.format("yyyy-MM-dd", date) + ": " + timeRange + desc;
            items.add(item);
        }
        extraClassAdapter.clear();
        extraClassAdapter.addAll(items);
        extraClassAdapter.notifyDataSetChanged();
    }



    private void refreshCustomizedClassesList() {
        List<String> items = new ArrayList<>();
        for (Map.Entry<Long, ExtraClassInfo> entry : tempCustomizedClasses.entrySet()) {
            long date = entry.getKey();
            ExtraClassInfo info = entry.getValue();

            String timeRange = String.format("%02d:%02d - %02d:%02d", info.startHour, info.startMinute, info.endHour, info.endMinute);
            String desc = (info.description != null && !info.description.isEmpty()) ? " (" + info.description + ")" : "";
            String item = android.text.format.DateFormat.format("yyyy-MM-dd", date) + ": " + timeRange + desc;
            items.add(item);

            android.util.Log.d("DEBUG", "Customized class added: " + item);
        }
        customizedClassAdapter.clear();
        customizedClassAdapter.addAll(items);
        customizedClassAdapter.notifyDataSetChanged();
        android.util.Log.d("DEBUG", "Customized class list refreshed with " + items.size() + " items");
    }

    private void showExtraClassDialog(long dateMillis) {
        ExtraClassInfo existing = tempExtraClasses.get(dateMillis);

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_extra_class, null);
        TimePicker startPicker = dialogView.findViewById(R.id.timepicker_start);
        TimePicker endPicker = dialogView.findViewById(R.id.timepicker_end);
        EditText etDesc = dialogView.findViewById(R.id.et_description);

        startPicker.setIs24HourView(true);
        endPicker.setIs24HourView(true);

        if (existing != null) {
            startPicker.setHour(existing.startHour);
            startPicker.setMinute(existing.startMinute);
            endPicker.setHour(existing.endHour);
            endPicker.setMinute(existing.endMinute);
            etDesc.setText(existing.description);
        } else {
            startPicker.setHour(9);
            startPicker.setMinute(0);
            endPicker.setHour(10);
            endPicker.setMinute(0);
            etDesc.setText("");
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Extra Class - " + android.text.format.DateFormat.format("yyyy-MM-dd", dateMillis))
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    int sHour = startPicker.getHour();
                    int sMinute = startPicker.getMinute();
                    int eHour = endPicker.getHour();
                    int eMinute = endPicker.getMinute();

                    if (eHour < sHour || (eHour == sHour && eMinute <= sMinute)) {
                        Toast.makeText(getContext(), "End time must be after start time", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String description = etDesc.getText().toString();

                    tempExtraClasses.put(dateMillis, new ExtraClassInfo(sHour, sMinute, eHour, eMinute, description));
                    refreshExtraClassesList();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showCustomizedClassDialog(long dateMillis) {
        ExtraClassInfo existing = tempCustomizedClasses.get(dateMillis);

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_extra_class, null);
        TimePicker startPicker = dialogView.findViewById(R.id.timepicker_start);
        TimePicker endPicker = dialogView.findViewById(R.id.timepicker_end);
        EditText etDesc = dialogView.findViewById(R.id.et_description);

        startPicker.setIs24HourView(true);
        endPicker.setIs24HourView(true);

        if (existing != null) {
            startPicker.setHour(existing.startHour);
            startPicker.setMinute(existing.startMinute);
            endPicker.setHour(existing.endHour);
            endPicker.setMinute(existing.endMinute);
            etDesc.setText(existing.description);
        } else {
            startPicker.setHour(9);
            startPicker.setMinute(0);
            endPicker.setHour(10);
            endPicker.setMinute(0);
            etDesc.setText("");
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Customized Class - " + android.text.format.DateFormat.format("yyyy-MM-dd", dateMillis))
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    int sHour = startPicker.getHour();
                    int sMinute = startPicker.getMinute();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private class AdapterViewListener implements android.widget.AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
            updateBatchSpinner();
        }

        @Override
        public void onNothingSelected(android.widget.AdapterView<?> parent) {}
    }

    private void updateBatchSpinner() {
        String course = (String) spinnerCourse.getSelectedItem();
        String grade = (String) spinnerGrade.getSelectedItem();
        if (course != null && grade != null) {
            List<String> batches = courseDB.getBatchesForCourseAndGrade(course, grade);
            batchAdapter.clear();
            batchAdapter.addAll(batches);
            batchAdapter.notifyDataSetChanged();
            if (!batches.isEmpty()) spinnerBatch.setSelection(0);
        }
    }

    private void setupListeners() {
        btnAddCourseNew.setOnClickListener(v -> promptAddNewCourse());
        btnAddBatch.setOnClickListener(v -> promptAddNewBatch());

        btnAddTeacher.setOnClickListener(v -> {
            saveDraft();
            startActivity(new Intent(getActivity(), addTeacherActivity.class));
        });

        btnAddStudent.setOnClickListener(v -> {
            saveDraft();
            startActivity(new Intent(getActivity(), AddStudentActivity.class));
        });

        btnAddCourse.setOnClickListener(v -> addCourseAndAssign());

        radioGroupSchedule.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_fixed) {
                layoutFixedSchedule.setVisibility(View.VISIBLE);
                layoutCustomizedSchedule.setVisibility(View.GONE);
            } else if (checkedId == R.id.radio_customized) {
                layoutFixedSchedule.setVisibility(View.GONE);
                layoutCustomizedSchedule.setVisibility(View.VISIBLE);
            }
        });
    }

    private String lastAddedCustomCourse = null;

    private void promptAddNewCourse() {
        final EditText input = new EditText(requireContext());
        input.setHint("Enter new course name");
        new AlertDialog.Builder(requireContext())
                .setTitle("Add New Course")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String courseName = input.getText().toString().trim();
                    if (TextUtils.isEmpty(courseName)) {
                        Toast.makeText(getContext(), "Course name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Remove previously added custom course (if any)
                    if (lastAddedCustomCourse != null) {
                        courseAdapter.remove(lastAddedCustomCourse);
                    }

                    // Add new course and store reference
                    courseAdapter.add(courseName);
                    courseAdapter.notifyDataSetChanged();
                    spinnerCourse.setSelection(courseAdapter.getPosition(courseName));
                    lastAddedCustomCourse = courseName;
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void safeSetSelection(Spinner spinner, int position) {
        if (spinner.getAdapter() != null) {
            int count = spinner.getAdapter().getCount();
            if (position >= 0 && position < count) {
                spinner.setSelection(position);
            } else if (count > 0) {
                spinner.setSelection(0);
            }
        }
    }


    private String lastAddedCustomBatch = null;

    private void promptAddNewBatch() {
        final EditText input = new EditText(requireContext());
        input.setHint("Enter new batch name");
        new AlertDialog.Builder(requireContext())
                .setTitle("Add New Batch")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String batchName = input.getText().toString().trim();
                    if (TextUtils.isEmpty(batchName)) {
                        Toast.makeText(getContext(), "Batch name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Remove previous custom batch
                    if (lastAddedCustomBatch != null) {
                        batchAdapter.remove(lastAddedCustomBatch);
                    }

                    // Add new batch and store reference
                    batchAdapter.add(batchName);
                    batchAdapter.notifyDataSetChanged();
                    spinnerBatch.setSelection(batchAdapter.getPosition(batchName));
                    lastAddedCustomBatch = batchName;
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private long createDateMillis(int year, int month, int dayOfMonth) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(year, month, dayOfMonth, 0, 0, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private String extractEmail(String text) {
        if (text == null) return "";
        int start = text.indexOf('(');
        int end = text.indexOf(')');
        if (start != -1 && end != -1 && end > start) {
            return text.substring(start + 1, end).trim();
        }
        return "";
    }

    private String formatDate(long millis) {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date(millis));
    }

    private long extractIdFromIdNameFormat(String text) {
        if (text == null) return -1;
        int dashIndex = text.indexOf(" - ");
        if (dashIndex == -1) return -1;
        try {
            return Long.parseLong(text.substring(0, dashIndex).trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void addCourseAndAssign() {
        String course = (String) spinnerCourse.getSelectedItem();
        String grade = (String) spinnerGrade.getSelectedItem();
        String batch = (String) spinnerBatch.getSelectedItem();
        String teacher = (String) spinnerTeacher.getSelectedItem();
        String description = etDescription.getText().toString();

        if (TextUtils.isEmpty(course) || TextUtils.isEmpty(grade) || TextUtils.isEmpty(batch) || TextUtils.isEmpty(teacher)) {
            Toast.makeText(getContext(), "Please select all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (listViewStudents.getCheckedItemCount() == 0) {
            Toast.makeText(getContext(), "Select at least one student", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check uniqueness
        if (courseDB.isAssignmentExists(course, grade, batch)) {
            Toast.makeText(getContext(), "This Course-Grade-Batch already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate schedule type selection
        int checkedScheduleId = radioGroupSchedule.getCheckedRadioButtonId();
        if (checkedScheduleId != R.id.radio_fixed && checkedScheduleId != R.id.radio_customized) {
            Toast.makeText(getContext(), "Please select a schedule type", Toast.LENGTH_SHORT).show();
            return;
        }
        SQLiteDatabase db = courseDB.getWritableDatabase();
        db.beginTransaction();

        try {
            long assignmentId = courseDB.addCourseIfNotExists(course, grade, batch, description);
            if (assignmentId == -1) throw new Exception("Course assignment creation failed");

            String teacherEmail = extractEmail(teacher);
            long teacherId = teacherDB.getTeacherIdByEmail(teacherEmail);
            if (teacherId == -1) throw new Exception("Teacher not found");

            ContentValues teacherUpdate = new ContentValues();
            teacherUpdate.put("teacher_id", teacherId);
            int rows = db.update("CourseAssignment", teacherUpdate, "assignment_id = ?", new String[]{String.valueOf(assignmentId)});
            if (rows <= 0) throw new Exception("Failed to assign teacher");

            for (int i = 0; i < studentAdapter.getCount(); i++) {
                if (listViewStudents.isItemChecked(i)) {
                    String rawItem = studentAdapter.getItem(i);
                    long studentId = extractIdFromIdNameFormat(rawItem);
                    if (studentId == -1) throw new Exception("Invalid student id format: " + rawItem);

                    ContentValues stuCV = new ContentValues();
                    stuCV.put("student_id", studentId);
                    stuCV.put("assignment_id", assignmentId);
                    long res = db.insert("CourseAssignment_Student", null, stuCV);
                    if (res == -1) throw new Exception("Failed to assign student: " + rawItem);
                }
            }

            // (Keep your existing schedule insertion logic here...)

            db.setTransactionSuccessful();
            Toast.makeText(getContext(), "Course and schedule added successfully", Toast.LENGTH_SHORT).show();

            // ✅ Remove draft
            clearDraft();
            // ✅ Clear the form
            clearForm();

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            try {
                db.endTransaction();
            } catch (IllegalStateException ex) {
                // Transaction already ended or DB closed
            }
        }
    }

    private void clearForm() {
        spinnerCourse.setSelection(0);
        spinnerGrade.setSelection(0);
        spinnerBatch.setSelection(0);
        spinnerTeacher.setSelection(0);
        etDescription.setText("");

        listViewStudents.clearChoices();
        listViewStudents.requestLayout();
        listViewStudents.invalidateViews();

        tempExtraClasses.clear();
        refreshExtraClassesList();

        tempCustomizedClasses.clear();
        refreshCustomizedClassesList();

        radioFixed.setChecked(true);
        layoutFixedSchedule.setVisibility(View.VISIBLE);
        layoutCustomizedSchedule.setVisibility(View.GONE);

        timepickerStart.setHour(9);
        timepickerStart.setMinute(0);
        timepickerEnd.setHour(10);
        timepickerEnd.setMinute(0);
    }

    private void saveDraft() {
        SharedPreferences.Editor editor = draftPrefs.edit();
        editor.putInt("spinnerCoursePos", spinnerCourse.getSelectedItemPosition());
        editor.putInt("spinnerGradePos", spinnerGrade.getSelectedItemPosition());
        editor.putInt("spinnerBatchPos", spinnerBatch.getSelectedItemPosition());
        editor.putInt("spinnerTeacherPos", spinnerTeacher.getSelectedItemPosition());
        editor.putString("description", etDescription.getText().toString());

        // Save checked students
        StringBuilder checkedStudents = new StringBuilder();
        for (int i = 0; i < studentAdapter.getCount(); i++) {
            if (listViewStudents.isItemChecked(i)) {
                if (checkedStudents.length() > 0) checkedStudents.append(",");
                checkedStudents.append(i);
            }
        }
        editor.putString("checkedStudents", checkedStudents.toString());

        editor.apply();
    }
    private void clearDraft() {
        SharedPreferences.Editor editor = draftPrefs.edit();
        editor.clear();
        editor.apply();
    }


    private void setupCustomizedClassesList() {
        customizedClassAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, new ArrayList<>());
        listViewCustomizedClasses.setAdapter(customizedClassAdapter);  // <-- Add this line!

        listViewCustomizedClasses.setOnItemLongClickListener((parent, view, position, id) -> {
            long keyToRemove = (long) tempCustomizedClasses.keySet().toArray()[position];
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Customized Class")
                    .setMessage("Delete customized class on " + android.text.format.DateFormat.format("yyyy-MM-dd", keyToRemove) + "?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        tempCustomizedClasses.remove(keyToRemove);
                        refreshCustomizedClassesList();
                    })
                    .setNegativeButton("No", null)
                    .show();
            return true;
        });

        refreshCustomizedClassesList();
    }


    private void loadDraft() {
        safeSetSelection(spinnerCourse, draftPrefs.getInt("spinnerCoursePos", 0));
        safeSetSelection(spinnerGrade, draftPrefs.getInt("spinnerGradePos", 0));
        safeSetSelection(spinnerBatch, draftPrefs.getInt("spinnerBatchPos", 0));
        safeSetSelection(spinnerTeacher, draftPrefs.getInt("spinnerTeacherPos", 0));

        etDescription.setText(draftPrefs.getString("description", ""));

        String checkedStudents = draftPrefs.getString("checkedStudents", "");
        if (!checkedStudents.isEmpty()) {
            String[] indices = checkedStudents.split(",");
            for (String indexStr : indices) {
                try {
                    int index = Integer.parseInt(indexStr);
                    listViewStudents.setItemChecked(index, true);
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }
}


