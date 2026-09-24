package com.groupproject.justeat.feature.customer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.groupproject.justeat.R;
import com.groupproject.justeat.feature.manager.Meal;

import java.util.ArrayList;
import java.util.List;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder>{

    private List<Meal> mealList;
    private List<Meal> fulllist;

    private onItemClickListener listener;
    public interface onItemClickListener{
        void onItemClick(int position);
    }
    public MainAdapter(List<Meal> mealList, onItemClickListener listener){
        this.mealList = mealList;
        this.listener = listener;
        this.fulllist = List.copyOf(mealList);
    }
    public void filter(String query){
        mealList.clear();
        if(query.isEmpty()){
            mealList.addAll(fulllist);
        }else{
            query = query.toLowerCase().trim();
            for(Meal meal : fulllist) {
                if (meal.getMealName().toLowerCase().contains(query)) {
                    mealList.add(meal);
                }
            }
        }
        notifyDataSetChanged();
    }
    public void updateData(List<Meal> newMealList) {
        this.mealList = newMealList;
        this.fulllist = new ArrayList<>(newMealList);
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public MainAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MainAdapter.ViewHolder holder, int position) {
        Meal meal = mealList.get(position);
        Glide.with(holder.itemView.getContext())
                .load(meal.getImageUrl())
                .placeholder(R.drawable.ic_image_placeholder) // optional placeholder
                .error(R.drawable.ic_image_placeholder)      // optional error image
                .into(holder.imageView);
        holder.tvName.setText(meal.getMealName());
        String spicinessText = meal.getSpiciness() != null ? String.valueOf(meal.getSpiciness()) : "N/A";
        holder.tvSpiciness.setText("Spiciness: " + spicinessText);
        holder.tvPrice.setText("Price: " + meal.getPrice());
        holder.tvDescription.setText("Description: " + meal.getDescription());

        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                listener.onItemClick(adapterPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView tvName, tvSpiciness, tvPrice, tvDescription;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img_meal);
            tvName = itemView.findViewById(R.id.tv_name);
            tvSpiciness = itemView.findViewById(R.id.tv_spiciness);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvDescription = itemView.findViewById(R.id.tv_description);
        }
    }

}



