package com.nibm.tutionmanagement;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private final Context context;
    private final List<Course2> courseList;

    private boolean enableEdit = true;    // Show add/remove buttons
    private boolean enableUpload = false; // Show upload button for assignments

    // Listener interfaces
    public interface OnAddAssignmentClickListener {
        void onAddAssignment(int position, Course2 course);
    }

    public interface OnAddMaterialClickListener {
        void onAddMaterial(int position, Course2 course);
    }

    public interface OnRemoveMaterialClickListener {
        void onRemoveMaterial(int coursePosition, int materialPosition);
    }

    public interface OnRemoveAssignmentClickListener {
        void onRemoveAssignment(int coursePosition, int assignmentPosition);
    }

    public interface OnUploadAssignmentClickListener {
        void onUploadFile(int coursePosition, int assignmentPosition, Assignment assignment);
    }

    private OnAddAssignmentClickListener assignmentClickListener;
    private OnAddMaterialClickListener materialClickListener;
    private OnRemoveMaterialClickListener removeMaterialClickListener;
    private OnRemoveAssignmentClickListener removeAssignmentClickListener;
    private OnUploadAssignmentClickListener uploadClickListener;

    public CourseAdapter(Context context, List<Course2> courseList) {
        this.context = context;
        this.courseList = courseList;
    }

    // Setters for listeners
    public void setOnAddAssignmentClickListener(OnAddAssignmentClickListener listener) {
        this.assignmentClickListener = listener;
    }

    public void setOnAddMaterialClickListener(OnAddMaterialClickListener listener) {
        this.materialClickListener = listener;
    }

    public void setOnRemoveMaterialClickListener(OnRemoveMaterialClickListener listener) {
        this.removeMaterialClickListener = listener;
    }

    public void setOnRemoveAssignmentClickListener(OnRemoveAssignmentClickListener listener) {
        this.removeAssignmentClickListener = listener;
    }

    public void setOnUploadAssignmentClickListener(OnUploadAssignmentClickListener listener) {
        this.uploadClickListener = listener;
    }

    public void setEnableEdit(boolean enableEdit) {
        this.enableEdit = enableEdit;
        notifyDataSetChanged();
    }

    public void setEnableUpload(boolean enableUpload) {
        this.enableUpload = enableUpload;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_course_expandable, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course2 course = courseList.get(position);

        holder.tvCourseName.setText(course.getName());
        holder.tvGrade.setText("Grade: " + course.getGrade());
        holder.tvBatch.setText("Batch: " + course.getBatch());

        holder.expandableLayout.setVisibility(View.GONE);
        holder.btnExpand.setImageResource(android.R.drawable.arrow_down_float);

        // Show or hide Add buttons based on enableEdit
        holder.btnAddAssignment.setVisibility(enableEdit ? View.VISIBLE : View.GONE);
        holder.btnAddMaterial.setVisibility(enableEdit ? View.VISIBLE : View.GONE);

        holder.btnExpand.setOnClickListener(v -> {
            if (holder.expandableLayout.getVisibility() == View.GONE) {
                holder.expandableLayout.setVisibility(View.VISIBLE);
                holder.btnExpand.setImageResource(android.R.drawable.arrow_up_float);
                populateAssignmentsAndMaterials(holder, course, position);
            } else {
                holder.expandableLayout.setVisibility(View.GONE);
                holder.btnExpand.setImageResource(android.R.drawable.arrow_down_float);
                holder.assignmentsContainer.removeAllViews();
                holder.materialsContainer.removeAllViews();
            }
        });

        holder.btnAddAssignment.setOnClickListener(v -> {
            if (assignmentClickListener != null) {
                assignmentClickListener.onAddAssignment(position, course);
            }
        });

        holder.btnAddMaterial.setOnClickListener(v -> {
            if (materialClickListener != null) {
                materialClickListener.onAddMaterial(position, course);
            }
        });
    }

    private void populateAssignmentsAndMaterials(CourseViewHolder holder, Course2 course, int coursePosition) {
        holder.assignmentsContainer.removeAllViews();
        holder.materialsContainer.removeAllViews();

        // Assignments Title
        TextView assignmentsTitle = new TextView(context);
        assignmentsTitle.setText("Assignments");
        assignmentsTitle.setTextSize(16);
        assignmentsTitle.setPadding(8, 8, 8, 8);
        holder.assignmentsContainer.addView(assignmentsTitle);

        for (int i = 0; i < course.getAssignments().size(); i++) {
            final int assignmentIndex = i;
            Assignment assignment = course.getAssignments().get(i);
            View assignmentView = LayoutInflater.from(context).inflate(R.layout.item_assignment, holder.assignmentsContainer, false);

            TextView tvTitle = assignmentView.findViewById(R.id.tvAssignmentTitle);
            TextView tvDesc = assignmentView.findViewById(R.id.tvAssignmentDescription);
            TextView tvDeadline = assignmentView.findViewById(R.id.tvAssignmentDeadline);
            Button btnOpenFile = assignmentView.findViewById(R.id.btnOpenFile);
            ImageButton btnRemoveAssignment = assignmentView.findViewById(R.id.btnRemoveAssignment);
            Button btnUploadFile = assignmentView.findViewById(R.id.btnUploadFile);

            tvTitle.setText(assignment.getTitle());
            tvDesc.setText(assignment.getDescription());
            tvDeadline.setText("Deadline: " + assignment.getDeadline());

            btnOpenFile.setOnClickListener(v -> {
                String fileUrl = assignment.getFileUrl();
                if (fileUrl != null && !fileUrl.isEmpty()) {
                    openFileWithFileProvider(fileUrl);
                } else {
                    Toast.makeText(context, "No file available", Toast.LENGTH_SHORT).show();
                }
            });

            // Show/hide Remove button based on enableEdit
            btnRemoveAssignment.setVisibility(enableEdit ? View.VISIBLE : View.GONE);
            btnRemoveAssignment.setOnClickListener(v -> {
                if (removeAssignmentClickListener != null) {
                    removeAssignmentClickListener.onRemoveAssignment(coursePosition, assignmentIndex);
                }
            });

            // Show/hide Upload button based on enableUpload
            btnUploadFile.setVisibility(enableUpload ? View.VISIBLE : View.GONE);
            btnUploadFile.setOnClickListener(v -> {
                if (uploadClickListener != null) {
                    uploadClickListener.onUploadFile(coursePosition, assignmentIndex, assignment);
                }
            });

            holder.assignmentsContainer.addView(assignmentView);
        }

        // Materials Title
        TextView materialsTitle = new TextView(context);
        materialsTitle.setText("Course Materials");
        materialsTitle.setTextSize(16);
        materialsTitle.setPadding(8, 16, 8, 8);
        holder.materialsContainer.addView(materialsTitle);

        for (int i = 0; i < course.getMaterials().size(); i++) {
            final int materialIndex = i;
            CourseMaterial material = course.getMaterials().get(i);
            View materialView = LayoutInflater.from(context).inflate(R.layout.item_material, holder.materialsContainer, false);

            TextView tvTitle = materialView.findViewById(R.id.tvMaterialTitle);
            TextView tvDescription = materialView.findViewById(R.id.tvMaterialDescription);
            ImageButton btnRemove = materialView.findViewById(R.id.btnRemoveMaterial);

            tvTitle.setText(material.getTitle());
            tvDescription.setText(material.getDescription());

            materialView.setOnClickListener(v -> {
                String fileUrl = material.getFileUrl();
                if (fileUrl != null && !fileUrl.isEmpty()) {
                    openFileWithFileProvider(fileUrl);
                } else {
                    Toast.makeText(context, "No file available", Toast.LENGTH_SHORT).show();
                }
            });

            // Show/hide Remove button based on enableEdit
            btnRemove.setVisibility(enableEdit ? View.VISIBLE : View.GONE);
            btnRemove.setOnClickListener(v -> {
                if (removeMaterialClickListener != null) {
                    removeMaterialClickListener.onRemoveMaterial(coursePosition, materialIndex);
                }
            });

            holder.materialsContainer.addView(materialView);
        }
    }

    private void openFileWithFileProvider(String fileUrl) {
        try {
            File file = new File(Uri.parse(fileUrl).getPath());
            Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(contentUri, "*/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Unable to open file", Toast.LENGTH_SHORT).show();
        }
    }

    public List<Course2> getCourses() {
        return courseList;
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourseName, tvGrade, tvBatch;
        ImageButton btnExpand;
        LinearLayout expandableLayout, assignmentsContainer, materialsContainer;
        Button btnAddAssignment, btnAddMaterial;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvGrade = itemView.findViewById(R.id.tvGrade);
            tvBatch = itemView.findViewById(R.id.tvBatch);
            btnExpand = itemView.findViewById(R.id.btnExpand);
            expandableLayout = itemView.findViewById(R.id.expandableLayout);
            assignmentsContainer = itemView.findViewById(R.id.assignmentsContainer);
            materialsContainer = itemView.findViewById(R.id.materialsContainer);
            btnAddAssignment = itemView.findViewById(R.id.btnAddAssignment);
            btnAddMaterial = itemView.findViewById(R.id.btnAddMaterial);
        }
    }
}
