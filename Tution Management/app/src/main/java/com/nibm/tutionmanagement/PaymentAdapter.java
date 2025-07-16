package com.nibm.tutionmanagement;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.ViewHolder> {

    private Context context;
    private List<NewPaymentFragment.PaymentData> paymentList;

    public PaymentAdapter(Context context, List<NewPaymentFragment.PaymentData> paymentList) {
        this.context = context;
        this.paymentList = paymentList;
    }

    @NonNull
    @Override
    public PaymentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_payment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentAdapter.ViewHolder holder, int position) {
        NewPaymentFragment.PaymentData payment = paymentList.get(position);

        holder.txtCourse.setText("Course: " + payment.getCourse());
        holder.txtGrade.setText("Grade: " + payment.getGrade());
        holder.txtBatch.setText("Batch: " + payment.getBatch());
        holder.txtTeacher.setText("Teacher: " + payment.getTeacher());
        holder.txtMonth.setText("Month: " + payment.getMonth());
        holder.txtYear.setText("Year: " + payment.getYear());
        holder.txtDescription.setText("Description: " + payment.getDescription());

        // Load image with Glide
        Glide.with(context)
                .load(payment.getPaymentSlipUrl())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(holder.imgSlip);

        // Set initial image height to 200dp (small)
        ViewGroup.LayoutParams params = holder.imgSlip.getLayoutParams();
        params.height = dpToPx(200);
        holder.imgSlip.setLayoutParams(params);
        holder.imgSlip.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // Toggle image size on click: expand to 400dp or shrink to 200dp
        holder.imgSlip.setOnClickListener(v -> {
            ViewGroup.LayoutParams layoutParams = holder.imgSlip.getLayoutParams();
            if (layoutParams.height == dpToPx(200)) {
                layoutParams.height = dpToPx(400);
                holder.imgSlip.setScaleType(ImageView.ScaleType.FIT_CENTER);
            } else {
                layoutParams.height = dpToPx(200);
                holder.imgSlip.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
            holder.imgSlip.setLayoutParams(layoutParams);
        });
    }

    @Override
    public int getItemCount() {
        return paymentList.size();
    }

    // Helper method to convert dp to px
    private int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtCourse, txtGrade, txtBatch, txtTeacher, txtMonth, txtYear, txtDescription;
        ImageView imgSlip;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCourse = itemView.findViewById(R.id.txtCourse);
            txtGrade = itemView.findViewById(R.id.txtGrade);
            txtBatch = itemView.findViewById(R.id.txtBatch);
            txtTeacher = itemView.findViewById(R.id.txtTeacher);
            txtMonth = itemView.findViewById(R.id.txtMonth);
            txtYear = itemView.findViewById(R.id.txtYear);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            imgSlip = itemView.findViewById(R.id.imgSlip);
        }
    }
}
