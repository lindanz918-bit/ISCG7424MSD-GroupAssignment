package com.groupproject.justeat.feature.staff;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.groupproject.justeat.base.BaseActivity;
import com.groupproject.justeat.databinding.ActivityStaffProfileBinding;
import com.groupproject.justeat.R;

/** Restaurant Staff 个人资料编辑页面。 */
public class StaffProfileActivity extends BaseActivity<ActivityStaffProfileBinding> {
    private StaffProfileViewModel viewModel;

    @Override protected ActivityStaffProfileBinding inflateBinding(LayoutInflater inflater) {
        return ActivityStaffProfileBinding.inflate(inflater);
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(StaffProfileViewModel.class);
        getBinding().btnStaffProfileBack.setOnClickListener(v -> finish());
        getBinding().btnStaffProfileSave.setOnClickListener(v -> viewModel.validateAndSave(
                getBinding().etStaffFirstName.getText().toString(),
                getBinding().etStaffLastName.getText().toString(),
                getBinding().etStaffEmail.getText().toString(),
                getBinding().etStaffPhone.getText().toString()));
        viewModel.getProfile().observe(this, state -> {
            if (state == null) return;
            getBinding().tilStaffFirstName.setError(state.getFirstNameErrorResId() == 0 ? null
                    : getString(state.getFirstNameErrorResId()));
            getBinding().tilStaffLastName.setError(state.getLastNameErrorResId() == 0 ? null
                    : getString(state.getLastNameErrorResId()));
            getBinding().tilStaffEmail.setError(state.getEmailErrorResId() == 0 ? null
                    : getString(state.getEmailErrorResId()));
            getBinding().tilStaffPhone.setError(state.getPhoneErrorResId() == 0 ? null
                    : getString(state.getPhoneErrorResId()));
        });
        viewModel.getSaved().observe(this, saved -> {
            if (Boolean.TRUE.equals(saved)) {
                Toast.makeText(this, R.string.staff_profile_save_pending, Toast.LENGTH_SHORT).show();
            }
        });
        viewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                viewModel.consumeError();
            }
        });
    }

}
