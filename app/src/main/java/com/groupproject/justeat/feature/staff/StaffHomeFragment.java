package com.groupproject.justeat.feature.staff;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.groupproject.justeat.R;
import com.groupproject.justeat.databinding.FragmentStaffHomeBinding;
import com.groupproject.justeat.feature.dashboard.StaffDashBoardActivity;

/** Staff 工作台：只展示实际功能入口，不伪造业务统计。 */
public class StaffHomeFragment extends Fragment {
    private FragmentStaffHomeBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        binding = FragmentStaffHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle state) {
        StaffDashBoardActivity host = (StaffDashBoardActivity) requireActivity();
        binding.staffHomeOrders.setOnClickListener(v -> host.selectTab(R.id.staff_nav_orders));
        binding.staffHomeTables.setOnClickListener(v -> host.selectTab(R.id.staff_nav_tables));
        binding.staffHomeProfile.setOnClickListener(v -> host.selectTab(R.id.staff_nav_profile));
        binding.staffSignOut.setOnClickListener(v -> host.signOut());
        refreshAccount();
    }

    public void refreshAccount() {
        if (binding != null)
            binding.staffAccountEmail.setText(
                    ((StaffDashBoardActivity) requireActivity()).getAccountEmail());
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
