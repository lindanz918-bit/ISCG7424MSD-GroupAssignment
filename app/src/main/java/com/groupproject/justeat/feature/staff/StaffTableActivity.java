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
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(StaffTableViewModel.class);
        adapter = new StaffTableAdapter(viewModel::toggleAvailability);
        getBinding().rvStaffTables.setLayoutManager(new LinearLayoutManager(this));
        getBinding().rvStaffTables.setAdapter(adapter);
        getBinding().btnStaffTablesBack.setOnClickListener(v -> finish());
        viewModel.getTables().observe(this, data -> {
            adapter.submitList(data);
            getBinding().layoutStaffTablesEmpty.setVisibility(
                    data == null || data.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
        });
        viewModel.getLoading().observe(this, loading -> getBinding().progressStaffTables
                .setVisibility(Boolean.TRUE.equals(loading) ? android.view.View.VISIBLE : android.view.View.GONE));
        viewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                viewModel.consumeError();
            }
        });
        viewModel.loadTables();
    }
}
