package com.groupproject.justeat.feature.staff;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.groupproject.justeat.data.model.RestaurantTable;
import com.groupproject.justeat.databinding.ActivityStaffTablesBinding;

/** Restaurant Staff 餐桌可用/不可用管理页面。 */
public class StaffTableFragment extends androidx.fragment.app.Fragment {
    private androidx.appcompat.app.AlertDialog activeDialog;
    private StaffTableViewModel viewModel;
    private StaffTableAdapter adapter;

    private ActivityStaffTablesBinding binding;
    private ActivityStaffTablesBinding getBinding() { return binding; }

    @Override public android.view.View onCreateView(LayoutInflater inflater,
            android.view.ViewGroup container, Bundle state) {
        binding = ActivityStaffTablesBinding.inflate(inflater, container, false);
        binding.getRoot().setFitsSystemWindows(false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(android.view.View view, Bundle savedState) {
        super.onViewCreated(view, savedState);
        viewModel = new ViewModelProvider(this).get(StaffTableViewModel.class);
        adapter = new StaffTableAdapter((table,occupancy) -> {
            if(!occupancy) viewModel.toggleAvailability(table);
            else activeDialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setMessage("AVAILABLE".equalsIgnoreCase(table.getStatus())?"Mark this table occupied?":"Release this table for new guests? Existing orders will not be changed.")
                .setNegativeButton("Cancel",null)
                .setPositiveButton("Confirm",(dialog,which) -> viewModel.toggleOccupancy(table)).show();
        });
        getBinding().rvStaffTables.setLayoutManager(new LinearLayoutManager(requireContext()));
        getBinding().rvStaffTables.setAdapter(adapter);
        getBinding().btnStaffTablesBack.setVisibility(getActivity() instanceof com.groupproject.justeat.feature.dashboard.StaffDashBoardActivity ? android.view.View.GONE : android.view.View.VISIBLE);
        getBinding().btnStaffTablesBack.setOnClickListener(v -> returnHome());
        viewModel.getTables().observe(getViewLifecycleOwner(), data -> {
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
        viewModel.getBookingLabels().observe(getViewLifecycleOwner(),adapter::setBookings);
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            adapter.setBusy(Boolean.TRUE.equals(loading));
            getBinding().progressStaffTables.setVisibility(Boolean.TRUE.equals(loading) ? android.view.View.VISIBLE : android.view.View.GONE);
        });
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                viewModel.consumeError();
            }
        });
        viewModel.loadTables();
    }

    private void returnHome() {
        if (getActivity() instanceof com.groupproject.justeat.feature.dashboard.StaffDashBoardActivity)
            ((com.groupproject.justeat.feature.dashboard.StaffDashBoardActivity) requireActivity())
                .selectTab(com.groupproject.justeat.R.id.staff_nav_home);
        else requireActivity().finish();
    }

    @Override public void onDestroyView() {
        if (activeDialog != null) { activeDialog.dismiss(); activeDialog = null; }
        binding.rvStaffTables.setAdapter(null);
        binding = null;
        super.onDestroyView();
    }
}
