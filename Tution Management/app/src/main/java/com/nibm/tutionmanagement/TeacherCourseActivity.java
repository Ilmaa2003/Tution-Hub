package com.nibm.tutionmanagement;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;

public class TeacherCourseActivity extends AppCompatActivity implements
        CourseAdapter.OnAddMaterialClickListener,
        CourseAdapter.OnRemoveMaterialClickListener,
        CourseAdapter.OnAddAssignmentClickListener,
        CourseAdapter.OnRemoveAssignmentClickListener {

    private DrawerLayout drawerLayout;
    private RecyclerView rvCourses;
    private CourseAdapter courseAdapter;
    private CourseDBHelper dbHelper;

    private ActivityResultLauncher<String> filePickerLauncher;
    private Uri pickedFileUri;
    private int currentCoursePosition = -1;

    private EditText etMaterialTitle, etMaterialDescription;
    private EditText etAssignmentTitle, etAssignmentDescription, etAssignmentDeadline;
    private Button btnPickDeadline;

    private DatabaseReference materialsRef;
    private DatabaseReference assignmentsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacher_course);

        drawerLayout = findViewById(R.id.drawer_layout);
        findViewById(R.id.btnToggle).setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(findViewById(R.id.sidebar))) {
                drawerLayout.closeDrawer(findViewById(R.id.sidebar));
            } else {
                drawerLayout.openDrawer(findViewById(R.id.sidebar));
            }
        });

        rvCourses = findViewById(R.id.rvCourses);
        rvCourses.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = CourseDBHelper.getInstance(this);
        materialsRef = FirebaseDatabase.getInstance("https://tuition-b6019-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("course_materials");
        assignmentsRef = FirebaseDatabase.getInstance("https://tuition-b6019-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("course_assignments");

        loadCourses();

        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                pickedFileUri = uri;
                Toast.makeText(this, "File selected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String sanitizeKey(String key) {
        return key.replaceAll("[.#$\\[\\] ]", "_");
    }

    private String getCourseFirebaseKey(Course2 course) {
        String rawKey = course.getName() + "_" + course.getGrade() + "_" + course.getBatch();
        return sanitizeKey(rawKey);
    }

    private void loadCourses() {
        List<Course2> courses = dbHelper.getAllCoursesBasic();

        if (courses.isEmpty()) {
            Toast.makeText(this, "No courses found", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Course2 course : courses) {
            course.getMaterials().clear();
            course.getAssignments().clear();

            String key = getCourseFirebaseKey(course);

            // Load materials
            materialsRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    course.getMaterials().clear();

                    if (snapshot.exists()) {
                        for (DataSnapshot materialSnap : snapshot.getChildren()) {
                            CourseMaterial material = materialSnap.getValue(CourseMaterial.class);
                            if (material != null) {
                                material.setFirebaseKey(materialSnap.getKey());
                                course.getMaterials().add(material);
                            }
                        }
                    }
                    if (courseAdapter != null) {
                        courseAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(TeacherCourseActivity.this, "Failed to load materials", Toast.LENGTH_SHORT).show();
                }
            });

            // Load assignments
            assignmentsRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    course.getAssignments().clear();

                    if (snapshot.exists()) {
                        for (DataSnapshot assignmentSnap : snapshot.getChildren()) {
                            Assignment assignment = assignmentSnap.getValue(Assignment.class);
                            if (assignment != null) {
                                assignment.setFirebaseKey(assignmentSnap.getKey());
                                course.getAssignments().add(assignment);
                            }
                        }
                    }
                    if (courseAdapter != null) {
                        courseAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(TeacherCourseActivity.this, "Failed to load assignments", Toast.LENGTH_SHORT).show();
                }
            });
        }

        courseAdapter = new CourseAdapter(this, courses);
        courseAdapter.setOnAddMaterialClickListener(this);
        courseAdapter.setOnRemoveMaterialClickListener(this);
        courseAdapter.setOnAddAssignmentClickListener(this);
        courseAdapter.setOnRemoveAssignmentClickListener(this);
        rvCourses.setAdapter(courseAdapter);
    }

    // Material callbacks
    @Override
    public void onAddMaterial(int position, Course2 course) {
        currentCoursePosition = position;
        showAddMaterialDialog(course);
    }

    @Override
    public void onRemoveMaterial(int coursePosition, int materialPosition) {
        List<Course2> courses = courseAdapter.getCourses();
        if (coursePosition >= 0 && coursePosition < courses.size()) {
            Course2 course = courses.get(coursePosition);
            if (materialPosition >= 0 && materialPosition < course.getMaterials().size()) {
                CourseMaterial materialToRemove = course.getMaterials().get(materialPosition);
                String key = getCourseFirebaseKey(course);

                if (materialToRemove.getFirebaseKey() != null) {
                    materialsRef.child(key).child(materialToRemove.getFirebaseKey()).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                course.getMaterials().remove(materialPosition);
                                courseAdapter.notifyItemChanged(coursePosition);
                                Toast.makeText(this, "Material removed from Firebase", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to remove material from Firebase", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    course.getMaterials().remove(materialPosition);
                    courseAdapter.notifyItemChanged(coursePosition);
                }
            }
        }
    }

    // Assignment callbacks
    @Override
    public void onAddAssignment(int position, Course2 course) {
        currentCoursePosition = position;
        showAddAssignmentDialog(course);
    }

    @Override
    public void onRemoveAssignment(int coursePosition, int assignmentPosition) {
        List<Course2> courses = courseAdapter.getCourses();
        if (coursePosition >= 0 && coursePosition < courses.size()) {
            Course2 course = courses.get(coursePosition);
            if (assignmentPosition >= 0 && assignmentPosition < course.getAssignments().size()) {
                Assignment assignmentToRemove = course.getAssignments().get(assignmentPosition);
                String key = getCourseFirebaseKey(course);

                if (assignmentToRemove.getFirebaseKey() != null) {
                    assignmentsRef.child(key).child(assignmentToRemove.getFirebaseKey()).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                course.getAssignments().remove(assignmentPosition);
                                courseAdapter.notifyItemChanged(coursePosition);
                                Toast.makeText(this, "Assignment removed from Firebase", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to remove assignment from Firebase", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    course.getAssignments().remove(assignmentPosition);
                    courseAdapter.notifyItemChanged(coursePosition);
                }
            }
        }
    }

    // Show dialog to add material
    private void showAddMaterialDialog(Course2 course) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_material, null);
        etMaterialTitle = dialogView.findViewById(R.id.etMaterialTitle);
        etMaterialDescription = dialogView.findViewById(R.id.etMaterialDescription);
        Button btnUploadFile = dialogView.findViewById(R.id.btnUploadFile);

        pickedFileUri = null;

        btnUploadFile.setOnClickListener(v -> filePickerLauncher.launch("*/*"));

        new AlertDialog.Builder(this)
                .setTitle("Add Course Material for " + course.getName())
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String title = etMaterialTitle.getText().toString().trim();
                    String description = etMaterialDescription.getText().toString().trim();

                    if (title.isEmpty()) {
                        Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (pickedFileUri == null) {
                        Toast.makeText(this, "Please select a file", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Uri copiedUri = copyFileToInternalStorage(pickedFileUri, title);
                    if (copiedUri == null) {
                        Toast.makeText(this, "Failed to copy file", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    CourseMaterial newMaterial = new CourseMaterial(title, description, copiedUri.toString());

                    String key = getCourseFirebaseKey(course);
                    DatabaseReference newMaterialRef = materialsRef.child(key).push();

                    newMaterialRef.setValue(newMaterial)
                            .addOnSuccessListener(aVoid -> {
                                newMaterial.setFirebaseKey(newMaterialRef.getKey());

                                course.getMaterials().add(newMaterial);
                                courseAdapter.notifyItemChanged(currentCoursePosition);

                                Toast.makeText(this, "Uploaded to Firebase", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Firebase upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                    pickedFileUri = null;
                })
                .setNegativeButton("Cancel", (dialog, which) -> pickedFileUri = null)
                .setOnDismissListener(dialog -> pickedFileUri = null)
                .show();
    }

    // Show dialog to add assignment with DatePicker for deadline
    private void showAddAssignmentDialog(Course2 course) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_assignment, null);
        etAssignmentTitle = dialogView.findViewById(R.id.etAssignmentTitle);
        etAssignmentDescription = dialogView.findViewById(R.id.etAssignmentDescription);
        etAssignmentDeadline = dialogView.findViewById(R.id.etDeadline);
        btnPickDeadline = dialogView.findViewById(R.id.btnPickDeadline);
        Button btnUploadFile = dialogView.findViewById(R.id.btnUploadFile);

        pickedFileUri = null;

        btnUploadFile.setOnClickListener(v -> filePickerLauncher.launch("*/*"));

        // Setup DatePicker dialog to pick deadline date
        btnPickDeadline.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        String dateString = String.format("%04d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                        etAssignmentDeadline.setText(dateString);
                    }, year, month, day);
            datePickerDialog.show();
        });

        new AlertDialog.Builder(this)
                .setTitle("Add Assignment for " + course.getName())
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String title = etAssignmentTitle.getText().toString().trim();
                    String description = etAssignmentDescription.getText().toString().trim();
                    String deadline = etAssignmentDeadline.getText().toString().trim();

                    if (title.isEmpty()) {
                        Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (deadline.isEmpty()) {
                        Toast.makeText(this, "Deadline cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (pickedFileUri == null) {
                        Toast.makeText(this, "Please select a file", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Uri copiedUri = copyFileToInternalStorage(pickedFileUri, title);
                    if (copiedUri == null) {
                        Toast.makeText(this, "Failed to copy file", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Assignment newAssignment = new Assignment(title, description, deadline, copiedUri.toString());

                    String key = getCourseFirebaseKey(course);
                    DatabaseReference newAssignmentRef = assignmentsRef.child(key).push();

                    newAssignmentRef.setValue(newAssignment)
                            .addOnSuccessListener(aVoid -> {
                                newAssignment.setFirebaseKey(newAssignmentRef.getKey());

                                course.getAssignments().add(newAssignment);
                                courseAdapter.notifyItemChanged(currentCoursePosition);

                                Toast.makeText(this, "Uploaded to Firebase", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Firebase upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                    pickedFileUri = null;
                })
                .setNegativeButton("Cancel", (dialog, which) -> pickedFileUri = null)
                .setOnDismissListener(dialog -> pickedFileUri = null)
                .show();
    }

    private Uri copyFileToInternalStorage(Uri sourceUri, String title) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(sourceUri);
            File dir = new File(getFilesDir(), "course_files");
            if (!dir.exists()) dir.mkdirs();

            String filename = "file_" + System.currentTimeMillis() + "_" + title.replaceAll("\\s+", "_") + ".dat";
            File destFile = new File(dir, filename);

            OutputStream outputStream = new FileOutputStream(destFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            return Uri.fromFile(destFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
