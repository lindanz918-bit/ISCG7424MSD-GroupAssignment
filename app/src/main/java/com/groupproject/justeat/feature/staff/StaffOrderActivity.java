package com.groupproject.justeat.feature.staff;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.groupproject.justeat.base.BaseActivity;
import com.groupproject.justeat.data.model.Order;
import com.groupproject.justeat.databinding.ActivityStaffOrdersBinding;

/** Restaurant Staff 订单确认、拒绝和取消页面。 */
public class StaffOrderActivity extends BaseActivity<ActivityStaffOrdersBinding> {
    private StaffOrderViewModel viewModel;
    private StaffOrderAdapter adapter;

    @Override protected ActivityStaffOrdersBinding inflateBinding(LayoutInflater inflater) {
        return ActivityStaffOrdersBinding.inflate(inflater);
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(StaffOrderViewModel.class);
        adapter = new StaffOrderAdapter((order, target) -> viewModel.applyStatus(order, target));
        getBinding().rvStaffOrders.setLayoutManager(new LinearLayoutManager(this));
        getBinding().rvStaffOrders.setAdapter(adapter);
        getBinding().btnStaffOrdersBack.setOnClickListener(v -> finish());
        viewModel.getOrders().observe(this, data -> {
            adapter.submitList(data);
            getBinding().layoutStaffOrdersEmpty.setVisibility(
                    data == null || data.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
        });
        viewModel.getLoading().observe(this, loading -> getBinding().progressStaffOrders
                .setVisibility(Boolean.TRUE.equals(loading) ? android.view.View.VISIBLE : android.view.View.GONE));
        viewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                viewModel.consumeError();
            }
        });
        viewModel.loadPendingOrders();
    }
}
