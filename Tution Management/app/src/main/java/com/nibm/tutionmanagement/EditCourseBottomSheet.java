package com.nibm.tutionmanagement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class EditCourseBottomSheet extends BottomSheetDialogFragment {

    private CourseAssignmentFull course;
    private EditText etDescription;
    private Spinner spinnerDay;
    private TimePicker timeStart, timeEnd;
    private Button btnSave;

    public interface OnCourseUpdatedListener {
        void onCourseUpdated();
    }

    private OnCourseUpdatedListener listener;

    public EditCourseBottomSheet(CourseAssignmentFull course) {
        this.course = course;
    }

    public void setOnCourseUpdatedListener(OnCourseUpdatedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_edit_course, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etDescription = view.findViewById(R.id.et_description);
        spinnerDay = view.findViewById(R.id.spinner_day_of_week);
        timeStart = view.findViewById(R.id.timepicker_start);
        timeEnd = view.findViewById(R.id.timepicker_end);
        btnSave = view.findViewById(R.id.btn_save_course);

        // Setup day-of-week spinner
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, days);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDay.setAdapter(adapter);

        // Populate existing values
        etDescription.setText(course.getDescription() != null ? course.getDescription() : "");

        if (course.getDayOfWeek() != null) {
            int dayIndex = java.util.Arrays.asList(days).indexOf(course.getDayOfWeek());
            if (dayIndex != -1) spinnerDay.setSelection(dayIndex);
        }

        if (course.getStartTime() != null && course.getEndTime() != null) {
            String[] startParts = course.getStartTime().split(":");
            String[] endParts = course.getEndTime().split(":");

            timeStart.setHour(Integer.parseInt(startParts[0]));
            timeStart.setMinute(Integer.parseInt(startParts[1]));

            timeEnd.setHour(Integer.parseInt(endParts[0]));
            timeEnd.setMinute(Integer.parseInt(endParts[1]));
        }

        btnSave.setOnClickListener(v -> {
            String description = etDescription.getText().toString().trim();
            String selectedDay = spinnerDay.getSelectedItem().toString();
            String startTime = String.format("%02d:%02d", timeStart.getHour(), timeStart.getMinute());
            String endTime = String.format("%02d:%02d", timeEnd.getHour(), timeEnd.getMinute());

            if (description.isEmpty()) {
                Toast.makeText(getContext(), "Description is required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update values in model
            course.setDescription(description);
            course.setDayOfWeek(selectedDay);
            course.setStartTime(startTime);
            course.setEndTime(endTime);

            // Save to database
            CourseDBHelper db = CourseDBHelper.getInstance(requireContext());
            boolean descUpdated = db.updateCourseAssignmentDescription(course.getAssignmentId(), description);
            boolean schedUpdated = db.updateScheduleFixed(course.getAssignmentId(), selectedDay, startTime, endTime);

            if (descUpdated || schedUpdated) {
                Toast.makeText(getContext(), "Course updated", Toast.LENGTH_SHORT).show();
                if (listener != null) listener.onCourseUpdated();
                dismiss();
            } else {
                Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
