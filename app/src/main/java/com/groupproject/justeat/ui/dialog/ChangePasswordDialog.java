package com.groupproject.justeat.ui.dialog;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.groupproject.justeat.databinding.DialogChangePasswordBinding;


public class ChangePasswordDialog extends BottomSheetDialogFragment {

    public static final String TAG = "ChangePasswordDialog";

    private DialogChangePasswordBinding binding;

    public static ChangePasswordDialog newInstance() {
        return new ChangePasswordDialog();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogChangePasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnCancel.setOnClickListener(v -> dismiss());

        binding.btnUpdatePassword.setOnClickListener(v -> attemptUpdatePassword());
    }

    private void attemptUpdatePassword() {
        String currentPassword = binding.etCurrentPassword.getText() != null ? binding.etCurrentPassword.getText().toString().trim() : "";
        String newPassword = binding.etNewPassword.getText() != null ? binding.etNewPassword.getText().toString().trim() : "";
        String confirmPassword = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(currentPassword)) {
            binding.tilCurrentPassword.setError("Please enter your current password");
            return;
        } else {
            binding.tilCurrentPassword.setError(null);
        }

        if (TextUtils.isEmpty(newPassword)) {
            binding.tilNewPassword.setError("Please enter a new password");
            return;
        } else if (newPassword.length() < 6) {
            binding.tilNewPassword.setError("Password must be at least 6 characters");
            return;
        } else {
            binding.tilNewPassword.setError(null);
        }

        if (!newPassword.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError("Passwords do not match");
            return;
        } else {
            binding.tilConfirmPassword.setError(null);
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Toast.makeText(getContext(), "User session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        binding.btnUpdatePassword.setEnabled(false);

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    user.updatePassword(newPassword)
                            .addOnSuccessListener(unused -> {
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "Password updated successfully!", Toast.LENGTH_SHORT).show();
                                }
                                dismiss();
                            })
                            .addOnFailureListener(e -> {
                                binding.btnUpdatePassword.setEnabled(true);
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "Failed to update password: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    binding.btnUpdatePassword.setEnabled(true);
                    binding.tilCurrentPassword.setError("Incorrect current password");
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}