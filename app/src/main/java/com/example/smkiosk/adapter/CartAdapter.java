package com.example.smkiosk.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smkiosk.R;
import com.example.smkiosk.model.SelectedItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface CartListener {
        void onPlus(int position);

        void onMinus(int position);
    }

    private List<SelectedItem> cart;
    private CartListener listener;

    public CartAdapter(List<SelectedItem> cart, CartListener listener) {
        this.cart = cart;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        SelectedItem s = cart.get(position);

        String name = s.menu.name;
        if (s.option != null) {
            name += "\n- (" + s.option.optionName + ")";
        }
        holder.tvName.setText(name);

        holder.tvQty.setText(String.valueOf(s.count));

        int linePrice = s.getLinePrice();
        holder.tvLinePrice.setText(String.format("%,d", linePrice) + "원");

        holder.btnPlus.setOnClickListener(v -> {
            if (listener != null) listener.onPlus(holder.getAdapterPosition());
        });

        holder.btnMinus.setOnClickListener(v -> {
            if (listener != null) listener.onMinus(holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return cart == null ? 0 : cart.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQty, tvLinePrice;
        Button btnPlus, btnMinus;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvQty = itemView.findViewById(R.id.tvQty);
            tvLinePrice = itemView.findViewById(R.id.tvLinePrice);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
        }
    }
}

