package com.groupproject.justeat.feature.customer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.groupproject.justeat.R;
import com.groupproject.justeat.data.model.MenuItem;
import com.groupproject.justeat.data.model.Order;
import com.groupproject.justeat.databinding.ItemMyMenuBinding;


import java.util.List;

public class CustomerMyMenuAdapter extends RecyclerView.Adapter<CustomerMyMenuAdapter.ViewHolder> {

    private final Context context;
    private final List<MenuItem> menuList;
    private final OnItemDeleteListener deleteListener;

    public interface OnItemDeleteListener {
        void onDeleteClick(MenuItem item, int position);
    }
    public CustomerMyMenuAdapter(Context context, List<MenuItem> menuList, OnItemDeleteListener deleteListener) {
        this.context = context;
        this.menuList = menuList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMyMenuBinding binding = ItemMyMenuBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MenuItem item = menuList.get(position);
        Log.d("AdapterCheck", "Item Name Value: " + item.getmealName());
        holder.binding.tvItemName.setText(item.getmealName());
        holder.binding.tvItemPrice.setText(item.getPrice());
        holder.binding.tvItemSpiciness.setText("Spiciness: " + item.getSpiciness());

        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .transform(new CenterCrop(), new RoundedCorners(16))
                .into(holder.binding.imgItem);

        if (deleteListener == null) {
            holder.binding.btnDelete.setVisibility(View.GONE); // Hides the trash can
        } else {
            // Normal Mode for "My Menu" page
            holder.binding.btnDelete.setVisibility(View.VISIBLE);
            holder.binding.btnDelete.setOnClickListener(v -> {
                deleteListener.onDeleteClick(item, position);
            });
        }
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemMyMenuBinding binding;
        public ViewHolder(ItemMyMenuBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}