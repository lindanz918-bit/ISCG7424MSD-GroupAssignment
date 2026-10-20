package com.groupproject.justeat.feature.manager;


import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.groupproject.justeat.R;
import com.groupproject.justeat.data.model.MenuItem;
import com.groupproject.justeat.feature.customer.CustomerMealAdapter;

import java.util.List;

public class ManagerMealAdapter
        extends RecyclerView.Adapter<ManagerMealAdapter.ViewHolder> {

    private List<MenuItem> mealList;
    private onItemClickListener listener;

    public interface onItemClickListener {
        void onItemClick(int position);
    }

    public ManagerMealAdapter(
            List<MenuItem> mealList,
            onItemClickListener listener) {

        this.mealList = mealList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_manager_meal, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {

        MenuItem meal = mealList.get(position);

        // Meal Image
        Glide.with(holder.itemView.getContext())
                .load(meal.getImageUrl())
                .placeholder(R.drawable.ic_restaurant_brand)
                .error(R.drawable.ic_restaurant_brand)
                .into(holder.imgMeal);

        // Meal Name
        holder.tvMealName.setText(meal.getmealName());

        // Meal Price
        holder.tvMealPrice.setText("Price: " + meal.getPrice());

        holder.tvSpiciness.setText(
                "Spiciness: " + String.valueOf(meal.getSpiciness())
        );
        holder.tvDescription.setText(meal.getDescription());

        holder.imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int position = holder.getAdapterPosition();

                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(position);
                }
            }
        });
        // Edit Button
//        holder.imgEdit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                int adapterPosition = holder.getAdapterPosition();
//
//                if (adapterPosition != RecyclerView.NO_POSITION
//                        && listener != null) {
//
//                    listener.onItemClick(adapterPosition);
//                }
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }

    public void updateData(List<MenuItem> newMealList) {
        this.mealList = newMealList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgEdit, imgMeal;
        TextView tvMealName, tvMealPrice, tvSpiciness, tvDescription;
//        Button btnEdit;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgMeal = itemView.findViewById(R.id.imgMeal);
            tvMealName = itemView.findViewById(R.id.tvMealName);
            tvMealPrice = itemView.findViewById(R.id.tvMealPrice);
            tvSpiciness = itemView.findViewById(R.id.tvSpiciness);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            imgEdit = itemView.findViewById(R.id.imgEdit);
//            imgEdit = itemView.findViewById(R.id.imgEdit);
        }
    }
}
