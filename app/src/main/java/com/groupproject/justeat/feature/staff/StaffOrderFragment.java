package com.groupproject.justeat.feature.staff;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.groupproject.justeat.data.model.Order;
import com.groupproject.justeat.databinding.ActivityStaffOrdersBinding;

/** Restaurant Staff 订单确认、拒绝和取消页面。 */
public class StaffOrderFragment extends androidx.fragment.app.Fragment {
    private androidx.appcompat.app.AlertDialog activeDialog;
    private StaffOrderViewModel viewModel;
    private StaffOrderAdapter adapter;

    private ActivityStaffOrdersBinding binding;
    private ActivityStaffOrdersBinding getBinding() { return binding; }

    @Override public android.view.View onCreateView(LayoutInflater inflater,
            android.view.ViewGroup container, Bundle state) {
        binding = ActivityStaffOrdersBinding.inflate(inflater, container, false);
        binding.getRoot().setFitsSystemWindows(false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(android.view.View view, Bundle savedState) {
        super.onViewCreated(view, savedState);
        viewModel = new ViewModelProvider(this).get(StaffOrderViewModel.class);
        adapter = new StaffOrderAdapter((order, target) -> {
            if ("ACCEPTED".equals(target)) viewModel.applyStatus(order,target);
            else activeDialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setMessage("REJECTED".equals(target)?"Reject this order?":"Cancel this order?")
                .setNegativeButton("Keep order",null)
                .setPositiveButton("Confirm",(dialog,which) -> viewModel.applyStatus(order,target)).show();
        });
        getBinding().rvStaffOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        getBinding().rvStaffOrders.setAdapter(adapter);
        getBinding().btnStaffOrdersBack.setVisibility(getActivity() instanceof com.groupproject.justeat.feature.dashboard.StaffDashBoardActivity ? android.view.View.GONE : android.view.View.VISIBLE);
        getBinding().btnStaffOrdersBack.setOnClickListener(v -> returnHome());
        viewModel.getOrders().observe(getViewLifecycleOwner(), data -> {
            adapter.submitList(data);
            int pending=0, active=0;
            if(data!=null) for(Order order:data) {
                if("PENDING".equals(order.getStatus())) pending++;
                if(StaffOrderActionPolicy.isActive(order.getStatus())) active++;
            }
            getBinding().tvStaffPendingCount.setText(getString(com.groupproject.justeat.R.string.staff_ui_pending_count,pending));
            getBinding().tvStaffActiveCount.setText(getString(com.groupproject.justeat.R.string.staff_ui_active_count,active));
            getBinding().layoutStaffOrdersEmpty.setVisibility(
                    data == null || data.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
        });
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            adapter.setBusy(Boolean.TRUE.equals(loading));
            getBinding().progressStaffOrders.setVisibility(Boolean.TRUE.equals(loading) ? android.view.View.VISIBLE : android.view.View.GONE);
        });
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                viewModel.consumeError();
            }
        });
        viewModel.loadPendingOrders();
    }

    private void returnHome() {
        if (getActivity() instanceof com.groupproject.justeat.feature.dashboard.StaffDashBoardActivity)
            ((com.groupproject.justeat.feature.dashboard.StaffDashBoardActivity) requireActivity())
                .selectTab(com.groupproject.justeat.R.id.staff_nav_home);
        else requireActivity().finish();
    }

    @Override public void onDestroyView() {
        if (activeDialog != null) { activeDialog.dismiss(); activeDialog = null; }
        binding.rvStaffOrders.setAdapter(null);
        binding = null;
        super.onDestroyView();
    }
}
