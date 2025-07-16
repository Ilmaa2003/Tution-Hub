package com.nibm.tutionmanagement;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CourseCardAdapter extends RecyclerView.Adapter<CourseCardAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(CourseAssignment course);
    }

    private final Context context;
    private final List<CourseAssignment> courseList;
    private OnItemClickListener listener;

    public CourseCardAdapter(Context context, List<CourseAssignment> courseList) {
        this.context = context;
        this.courseList = courseList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CourseCardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_course_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseCardAdapter.ViewHolder holder, int position) {
        CourseAssignment course = courseList.get(position);
        holder.courseName.setText(course.getCourseName());
        holder.gradeBatch.setText("Grade: " + course.getGrade() + " | Batch: " + course.getBatch());

        holder.moreOptions.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.moreOptions);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.course_card_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> handleMenuItemClick(item, position));
            popup.show();
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(course);
            }
        });
    }

    private boolean handleMenuItemClick(MenuItem item, int position) {
        CourseAssignment selected = courseList.get(position);
        int id = item.getItemId();
        if (id == R.id.menu_edit) {
            Toast.makeText(context, "Edit: " + selected.getCourseName(), Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu_delete) {
            Toast.makeText(context, "Delete: " + selected.getCourseName(), Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView courseName, gradeBatch;
        ImageButton moreOptions;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            courseName = itemView.findViewById(R.id.tv_course_name);
            gradeBatch = itemView.findViewById(R.id.tv_grade_batch);
            moreOptions = itemView.findViewById(R.id.btn_more_options);
        }
    }
}
