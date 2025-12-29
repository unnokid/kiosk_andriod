package com.example.smkiosk.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smkiosk.R;
import com.example.smkiosk.model.MenuItem;
import com.example.smkiosk.model.MenuOption;

import java.util.ArrayList;
import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    public interface OnItemClickListener {
        void onClick(MenuItem item);
    }

    private List<MenuItem> items;
    private List<MenuOption> allOptions;  // 전체 옵션 리스트
    private OnItemClickListener listener;

    public MenuAdapter(List<MenuItem> items,
                       List<MenuOption> allOptions,
                       OnItemClickListener listener) {
        this.items = items;
        this.allOptions = allOptions;
        this.listener = listener;
    }

    public void setItems(List<MenuItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu, parent, false);
        return new MenuViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        MenuItem item = items.get(position);
        holder.tvName.setText(item.name);

        // 해당 메뉴의 옵션만 모으기
        List<MenuOption> optionsForMenu = new ArrayList<>();
        for (MenuOption o : allOptions) {
            if (o.menuId.equals(item.id)) {
                optionsForMenu.add(o);
            }
        }

        // 옵션 영역 초기화
        holder.layoutOptions.removeAllViews();

        int base = (item.price != null) ? item.price : 0;
        LayoutInflater inflater = LayoutInflater.from(holder.itemView.getContext());

        if (!optionsForMenu.isEmpty()) {
            // HOT / ICE 등 옵션별 한 줄씩 생성
            for (MenuOption o : optionsForMenu) {
                View row = inflater.inflate(R.layout.item_menu_option_row,
                        holder.layoutOptions, false);

                TextView tvLabel = row.findViewById(R.id.tvOptionLabel);
                TextView tvPrice = row.findViewById(R.id.tvOptionPrice);

                tvLabel.setText(o.optionName); // HOT / ICE / 딸기 등
                int total = base + o.price;
                tvPrice.setText(String.format("%,d원", total));

                holder.layoutOptions.addView(row);
            }
        } else {
            // 옵션 없는 메뉴
            View row = inflater.inflate(R.layout.item_menu_option_row,
                    holder.layoutOptions, false);

            TextView tvLabel = row.findViewById(R.id.tvOptionLabel);
            TextView tvPrice = row.findViewById(R.id.tvOptionPrice);

            tvLabel.setText(""); // 라벨 없음
            int total = base;
            if (total > 0) {
                tvPrice.setText(String.format("%,d원", total));
            } else {
                tvPrice.setText("가격 선택");
            }

            holder.layoutOptions.addView(row);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class MenuViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        LinearLayout layoutOptions;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            layoutOptions = itemView.findViewById(R.id.layoutOptions);
        }
    }
}
