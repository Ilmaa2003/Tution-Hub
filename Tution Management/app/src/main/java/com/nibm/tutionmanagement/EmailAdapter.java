package com.nibm.tutionmanagement;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmailAdapter extends RecyclerView.Adapter<EmailAdapter.EmailViewHolder> {

    private final List<String> emails;
    private final Map<String, String> gradesMap = new HashMap<>();

    public EmailAdapter(List<String> emails) {
        this.emails = emails;
    }

    public Map<String, String> getGradesMap() {
        return gradesMap;
    }

    @NonNull
    @Override
    public EmailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_email, parent, false);
        return new EmailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmailViewHolder holder, int position) {
        String email = emails.get(position);
        holder.tvEmail.setText(email);

        // Set previous value if exists
        holder.etGrade.setText(gradesMap.getOrDefault(email, ""));

        holder.etGrade.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                gradesMap.put(email, s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    @Override
    public int getItemCount() {
        return emails.size();
    }
    public void clearGrades() {
        gradesMap.clear(); // or however you store grades keyed by email
        notifyDataSetChanged();
    }

    static class EmailViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmail;
        EditText etGrade;

        public EmailViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            etGrade = itemView.findViewById(R.id.etGrade);
        }
    }
}
