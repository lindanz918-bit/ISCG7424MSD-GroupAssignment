package com.groupproject.justeat.feature.staff;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.groupproject.justeat.base.BaseActivity;
import com.groupproject.justeat.data.model.RestaurantTable;
import com.groupproject.justeat.databinding.ActivityStaffTablesBinding;

/** Restaurant Staff 餐桌可用/不可用管理页面。 */
public class StaffTableActivity extends BaseActivity<ActivityStaffTablesBinding> {
    private StaffTableViewModel viewModel;
    private StaffTableAdapter adapter;

    @Override protected ActivityStaffTablesBinding inflateBinding(LayoutInflater inflater) {
        return ActivityStaffTablesBinding.inflate(inflater);
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        setTheme(com.groupproject.justeat.R.style.Theme_JustEat_Staff);
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(StaffTableViewModel.class);
        adapter = new StaffTableAdapter((table,occupancy) -> {
            if(!occupancy) viewModel.toggleAvailability(table);
            else new androidx.appcompat.app.AlertDialog.Builder(this)
                .setMessage("AVAILABLE".equalsIgnoreCase(table.getStatus())?"Mark this table occupied?":"Release this table for new guests? Existing orders will not be changed.")
                .setNegativeButton("Cancel",null)
                .setPositiveButton("Confirm",(dialog,which) -> viewModel.toggleOccupancy(table)).show();
        });
        getBinding().rvStaffTables.setLayoutManager(new LinearLayoutManager(this));
        getBinding().rvStaffTables.setAdapter(adapter);
        getBinding().btnStaffTablesBack.setOnClickListener(v -> finish());
        viewModel.getTables().observe(this, data -> {
            adapter.submitList(data);
            int available=0, unavailable=0, occupied=0, reserved=0;
            if(data!=null) for(RestaurantTable table:data) {
                if("AVAILABLE".equalsIgnoreCase(table.getStatus())) available++;
                else if("UNAVAILABLE".equalsIgnoreCase(table.getStatus())) unavailable++;
                else if("OCCUPIED".equalsIgnoreCase(table.getStatus())) occupied++;
                else if("RESERVED".equalsIgnoreCase(table.getStatus())) reserved++;
            }
            getBinding().tvStaffTableSummary.setText("Available: "+available+"   Unavailable: "+unavailable
                +"\nOccupied: "+occupied+"   Reserved: "+reserved);
            getBinding().layoutStaffTablesEmpty.setVisibility(
                    data == null || data.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
        });
        viewModel.getBookingLabels().observe(this,adapter::setBookings);
        viewModel.getLoading().observe(this, loading -> {
            adapter.setBusy(Boolean.TRUE.equals(loading));
            getBinding().progressStaffTables.setVisibility(Boolean.TRUE.equals(loading) ? android.view.View.VISIBLE : android.view.View.GONE);
        });
        viewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                viewModel.consumeError();
            }
        });
        viewModel.loadTables();
    }
}
