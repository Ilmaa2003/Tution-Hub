package com.nibm.tutionmanagement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserAdapter<T> extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface Binder<T> {
        void bind(UserViewHolder holder, T item);
    }

    public interface OnItemClickListener<T> {
        void onItemClick(T item);
    }

    public interface OnItemLongClickListener<T> {
        void onItemLongClick(T item);
    }

    public interface OnEditClickListener<T> {
        void onEditClick(T item);
    }

    public interface OnDeleteClickListener<T> {
        void onDeleteClick(T item);
    }

    private List<T> userList;
    private final Binder<T> binder;
    private OnItemClickListener<T> clickListener;
    private OnItemLongClickListener<T> longClickListener;
    private OnEditClickListener<T> editClickListener;
    private OnDeleteClickListener<T> deleteClickListener;

    private final Set<T> selectedItems = new HashSet<>();

    public UserAdapter(List<T> userList, Binder<T> binder) {
        this.userList = userList;
        this.binder = binder;
    }

    public void setOnItemClickListener(OnItemClickListener<T> listener) {
        this.clickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener<T> listener) {
        this.longClickListener = listener;
    }

    public void setOnEditClickListener(OnEditClickListener<T> listener) {
        this.editClickListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener<T> listener) {
        this.deleteClickListener = listener;
    }

    public void setData(List<T> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }

    public void toggleSelection(T item) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item);
        } else {
            selectedItems.add(item);
        }
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public List<T> getSelectedItems() {
        return new ArrayList<>(selectedItems);
    }

    public boolean isSelectionMode() {
        return !selectedItems.isEmpty();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_card_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override

    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        T item = userList.get(position);
        binder.bind(holder, item);

        boolean selectionMode = isSelectionMode();
        holder.checkBox.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
        holder.checkBox.setChecked(selectedItems.contains(item));

        // Hide more options button during selection mode (long press activated)
        holder.btnMoreOptions.setVisibility(selectionMode ? View.GONE : View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onItemClick(item);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(item);
                return true;
            }
            return false;
        });

        holder.btnMoreOptions.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenuInflater().inflate(R.menu.user_card_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(menuItem -> {
                int id = menuItem.getItemId();
                if (id == R.id.menu_edit) {
                    if (editClickListener != null) editClickListener.onEditClick(item);
                    return true;
                } else if (id == R.id.menu_delete) {
                    if (deleteClickListener != null) deleteClickListener.onDeleteClick(item);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRole;
        CheckBox checkBox;
        ImageButton btnMoreOptions;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_user_name);
            tvRole = itemView.findViewById(R.id.tv_user_role);
            checkBox = itemView.findViewById(R.id.checkbox_select);
            btnMoreOptions = itemView.findViewById(R.id.btn_more_options);
        }
    }
}
