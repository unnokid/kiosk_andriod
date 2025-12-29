package com.example.smkiosk.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smkiosk.R;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryClickListener {
        void onClick(String category);
    }

    private List<String> items;
    private OnCategoryClickListener listener;
    private String selectedCategory;

    public CategoryAdapter(List<String> items, OnCategoryClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setItems(List<String> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public void setSelectedCategory(String category) {
        this.selectedCategory = category;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String category = items.get(position);
        holder.tvName.setText(category);

        boolean selected = category.equals(selectedCategory);
        holder.tvName.setSelected(selected);   // ★ 이 줄 추가

        if (selected) {
            holder.tvName.setBackgroundResource(R.drawable.bg_category_selected);
        } else {
            holder.tvName.setBackgroundResource(R.drawable.bg_category_normal);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(category);
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}
