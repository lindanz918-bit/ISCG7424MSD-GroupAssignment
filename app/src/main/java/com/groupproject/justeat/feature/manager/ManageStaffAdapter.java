package com.groupproject.justeat.feature.manager;

import android.os.Bundle;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.LayoutInflater;
import com.bumptech.glide.Glide;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.groupproject.justeat.R;
import com.groupproject.justeat.data.model.Staff;

import java.util.List;
import com.groupproject.justeat.base.BaseActivity;
import com.groupproject.justeat.data.model.Staff;
import com.groupproject.justeat.databinding.ActivityManageMenuBinding;
import com.groupproject.justeat.databinding.ActivityManageStaffBinding;

public class ManageStaffAdapter
        extends RecyclerView.Adapter<ManageStaffAdapter.ViewHolder> {

    private List<Staff> staffList;
    private onItemClickListener listener;

    public interface onItemClickListener {
        void onItemClick(int position);
    }

    public ManageStaffAdapter(
            List<Staff> staffList,
            onItemClickListener listener) {

        this.staffList = staffList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.staff_card, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {

        Staff staff = staffList.get(position);

        // Staff Image
        Glide.with(holder.itemView.getContext())
                .load(staff.getImageUrl())
                .placeholder(R.drawable.ic_restaurant_brand)
                .error(R.drawable.ic_restaurant_brand)
                .into(holder.imgStaff);

        // Staff Last Name
        holder.tvLastName.setText(
                "Last Name: " + staff.getLastName()
        );

        // Staff First Name
        holder.tvFirstName.setText(
                "First Name: " + staff.getFirstName()
        );

        // Staff Email
        holder.tvStaffEmail.setText(
                "Email: " + staff.getEmail()
        );
        //image delete staff
        holder.imgDeleteStaff.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        int position = holder.getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION
                                && listener != null) {

                            listener.onItemClick(position);
                        }
                    }
                }
        );
        // Hide Password because Staff does not use Authentication
        if (holder.tvStaffPassword != null) {
            holder.tvStaffPassword.setVisibility(View.GONE);
        }

        // Edit Staff
//        holder.imgEditStaff.setOnClickListener(
//                new View.OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//
//                        int position = holder.getAdapterPosition();
//
//                        if (position != RecyclerView.NO_POSITION
//                                && listener != null) {
//
//                            listener.onItemClick(position);
//                        }
//                    }
//                }
//        );
    }



    @Override
    public int getItemCount() {
        return staffList.size();
    }

    public void updateData(List<Staff> newStaffList) {
        this.staffList = newStaffList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgStaff, imgDeleteStaff;

        TextView tvLastName, tvFirstName, tvStaffEmail, tvStaffPassword;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgStaff = itemView.findViewById(R.id.imgStaff);
            imgDeleteStaff = itemView.findViewById(R.id.imgDeleteStaff);
            tvLastName = itemView.findViewById(R.id.tvLastName);
            tvFirstName = itemView.findViewById(R.id.tvFirstName);
            tvStaffEmail = itemView.findViewById(R.id.tvStaffEmail);
            tvStaffPassword = itemView.findViewById(R.id.tvStaffPassword);
//            imgEditStaff = itemView.findViewById(R.id.imgEditStaff);
        }
    }
}