package com.groupproject.justeat.feature.staff;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.groupproject.justeat.R;
import com.groupproject.justeat.data.model.User;
import com.groupproject.justeat.databinding.ActivityStaffProfileBinding;
import com.groupproject.justeat.feature.dashboard.StaffDashBoardActivity;

import java.util.HashMap;
import java.util.Map;

/** Restaurant Staff 个人资料编辑页面。 */
public class StaffProfileFragment extends Fragment {
    private boolean busy;
    private boolean profilePopulated;
    private AlertDialog activeDialog;

    private ActivityStaffProfileBinding binding;

    private ActivityStaffProfileBinding getBinding() {
        return binding;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        binding = ActivityStaffProfileBinding.inflate(inflater, container, false);
        binding.getRoot().setFitsSystemWindows(false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedState) {
        super.onViewCreated(view, savedState);
        profilePopulated = savedState != null && savedState.getBoolean("staff.profilePopulated");
        binding.etStaffEmail.setEnabled(false);
        binding.btnStaffAvatar.setOnClickListener(v -> changeAvatar());
        binding.btnStaffPassword.setOnClickListener(v -> changeCredentials(false));
        binding.btnStaffEmail.setOnClickListener(v -> changeCredentials(true));
        binding.btnStaffProfileCancel.setOnClickListener(v -> returnHome());
        binding.btnStaffProfileBack.setVisibility(
                getActivity() instanceof StaffDashBoardActivity ? View.GONE : View.VISIBLE);
        binding.btnStaffProfileBack.setOnClickListener(v -> returnHome());
        binding.btnStaffProfileSave.setOnClickListener(v -> saveProfile());
        loadProfile();
    }

    private void loadProfile() {
        setBusy(true);
        StaffAccess.check(
                () -> {
                    if (binding == null) return;
                    String uid = FirebaseAuth.getInstance().getUid();
                    FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(uid)
                            .get()
                            .addOnSuccessListener(
                                    snapshot -> {
                                        if (binding == null) return;
                                        User user = snapshot.getValue(User.class);
                                        FirebaseUser auth =
                                                FirebaseAuth.getInstance().getCurrentUser();
                                        if (user == null
                                                || auth == null
                                                || !uid.equals(auth.getUid())) {
                                            failed("Cannot read profile. Please sign in again.");
                                            return;
                                        }
                                        if (!profilePopulated) {
                                            binding.etStaffFirstName.setText(user.getFirstName());
                                            binding.etStaffLastName.setText(user.getLastName());
                                            binding.etStaffPhone.setText(
                                                    user.getPhone() == null
                                                            ? ""
                                                            : user.getPhone().toString());
                                            profilePopulated = true;
                                        }
                                        binding.etStaffEmail.setText(auth.getEmail());
                                        Glide.with(this)
                                                .load(user.getAvatarUrl())
                                                .placeholder(R.drawable.staff_avatar_placeholder)
                                                .error(R.drawable.staff_avatar_placeholder)
                                                .circleCrop()
                                                .into(binding.staffAvatar);
                                        setBusy(false);
                                    })
                            .addOnFailureListener(e -> failed(e.getMessage()));
                },
                this::failed);
    }

    private void saveProfile() {
        if (busy || binding == null) return;
        String first = binding.etStaffFirstName.getText().toString().trim();
        String last = binding.etStaffLastName.getText().toString().trim();
        String phone = binding.etStaffPhone.getText().toString().trim();
        String email = binding.etStaffEmail.getText().toString().trim();
        binding.tilStaffFirstName.setError(
                first.isEmpty() ? getString(R.string.error_profile_first_name_required) : null);
        binding.tilStaffLastName.setError(
                last.isEmpty() ? getString(R.string.error_profile_last_name_required) : null);
        binding.tilStaffPhone.setError(
                phone.isEmpty() ? getString(R.string.error_profile_phone_required) : null);
        boolean validEmail = Patterns.EMAIL_ADDRESS.matcher(email).matches();
        binding.tilStaffEmail.setError(
                email.isEmpty()
                        ? getString(R.string.error_profile_email_required)
                        : validEmail ? null : getString(R.string.error_profile_email_invalid));
        if (first.isEmpty() || last.isEmpty() || phone.isEmpty() || !validEmail) return;
        setBusy(true);
        StaffAccess.check(
                () -> {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user == null) {
                        failed("Please sign in again.");
                        return;
                    }
                    Map<String, Object> changes = new HashMap<>();
                    changes.put("firstName", first);
                    changes.put("lastName", last);
                    changes.put("phone", phone);
                    changes.put("email", user.getEmail());
                    changes.put("updatedAt", ServerValue.TIMESTAMP);
                    FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(user.getUid())
                            .updateChildren(changes)
                            .addOnSuccessListener(
                                    v -> {
                                        setBusy(false);
                                        message("Profile saved.");
                                    })
                            .addOnFailureListener(e -> failed(e.getMessage()));
                },
                this::failed);
    }

    private void setBusy(boolean loading) {
        busy = loading;
        if (binding != null) binding.btnStaffProfileSave.setEnabled(!loading);
    }

    private void failed(String error) {
        setBusy(false);
        message(error);
    }

    /** Store a public HTTPS avatar URL; no storage bucket or local file permission required. */
    private void changeAvatar() {
        EditText input = new EditText(requireContext());
        input.setHint("https://…");
        input.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT
                        | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        activeDialog =
                new AlertDialog.Builder(requireContext())
                        .setTitle("Avatar image URL")
                        .setView(input)
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton(
                                "Save",
                                (d, w) -> {
                                    String url = input.getText().toString().trim();
                                    if (!url.startsWith("https://")
                                            || !Patterns.WEB_URL.matcher(url).matches()) {
                                        message("Enter a valid HTTPS image URL.");
                                        return;
                                    }
                                    StaffAccess.check(
                                            () -> {
                                                String uid = FirebaseAuth.getInstance().getUid();
                                                FirebaseDatabase.getInstance()
                                                        .getReference("users")
                                                        .child(uid)
                                                        .child("avatarUrl")
                                                        .setValue(url)
                                                        .addOnSuccessListener(
                                                                v -> {
                                                                    if (binding == null
                                                                            || !isAdded()) return;
                                                                    Glide.with(this)
                                                                            .load(url)
                                                                            .placeholder(
                                                                                    R.drawable
                                                                                            .staff_avatar_placeholder)
                                                                            .error(
                                                                                    R.drawable
                                                                                            .staff_avatar_placeholder)
                                                                            .circleCrop()
                                                                            .into(
                                                                                    getBinding()
                                                                                            .staffAvatar);
                                                                    message("Avatar saved.");
                                                                })
                                                        .addOnFailureListener(
                                                                e -> message(e.getMessage()));
                                            },
                                            this::message);
                                })
                        .show();
    }

    private void changeCredentials(boolean email) {
        LinearLayout form = new LinearLayout(requireContext());
        form.setOrientation(1);
        form.setPadding(32, 16, 32, 16);
        EditText current = new EditText(requireContext());
        current.setHint("Current password");
        current.setInputType(129);
        form.addView(current);
        EditText replacement = new EditText(requireContext());
        replacement.setHint(email ? "New email address" : "New password (at least 6 characters)");
        replacement.setInputType(email ? 33 : 129);
        form.addView(replacement);
        EditText confirm = new EditText(requireContext());
        confirm.setHint(email ? "Confirm email" : "Confirm password");
        confirm.setInputType(email ? 33 : 129);
        form.addView(confirm);
        AlertDialog dialog =
                new AlertDialog.Builder(requireContext())
                        .setTitle(email ? "Change email" : "Change password")
                        .setView(form)
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Update", null)
                        .create();
        dialog.setOnShowListener(
                v ->
                        dialog.getButton(-1)
                                .setOnClickListener(
                                        button -> {
                                            String next = replacement.getText().toString(),
                                                    password = current.getText().toString();
                                            if (password.isEmpty()
                                                    || !next.equals(confirm.getText().toString())
                                                    || (email
                                                            ? !Patterns.EMAIL_ADDRESS
                                                                    .matcher(next)
                                                                    .matches()
                                                            : next.length() < 6)) {
                                                message(
                                                        "Check your current password and matching"
                                                            + " new values.");
                                                return;
                                            }
                                            dialog.getButton(-1).setEnabled(false);
                                            StaffAccess.check(
                                                    () -> {
                                                        FirebaseUser user =
                                                                FirebaseAuth.getInstance()
                                                                        .getCurrentUser();
                                                        if (user == null
                                                                || user.getEmail() == null) {
                                                            dialog.getButton(-1).setEnabled(true);
                                                            message("Please sign in again.");
                                                            return;
                                                        }
                                                        user.reauthenticate(
                                                                        EmailAuthProvider
                                                                                .getCredential(
                                                                                        user
                                                                                                .getEmail(),
                                                                                        password))
                                                                .addOnSuccessListener(
                                                                        ignored -> {
                                                                            com.google.android.gms
                                                                                                    .tasks
                                                                                                    .Task<
                                                                                            Void>
                                                                                    task =
                                                                                            email
                                                                                                    ? user
                                                                                                            .verifyBeforeUpdateEmail(
                                                                                                                    next)
                                                                                                    : user
                                                                                                            .updatePassword(
                                                                                                                    next);
                                                                            task.addOnSuccessListener(
                                                                                            done -> {
                                                                                                dialog
                                                                                                        .dismiss();
                                                                                                message(
                                                                                                        email
                                                                                                                ? "Verification"
                                                                                                                      + " sent"
                                                                                                                      + " to your"
                                                                                                                      + " new email."
                                                                                                                      + " Verify"
                                                                                                                      + " it, then"
                                                                                                                      + " sign"
                                                                                                                      + " in again."
                                                                                                                : "Password"
                                                                                                                      + " updated.");
                                                                                            })
                                                                                    .addOnFailureListener(
                                                                                            e -> {
                                                                                                dialog.getButton(
                                                                                                                -1)
                                                                                                        .setEnabled(
                                                                                                                true);
                                                                                                message(
                                                                                                        e
                                                                                                                .getMessage());
                                                                                            });
                                                                        })
                                                                .addOnFailureListener(
                                                                        e -> {
                                                                            dialog.getButton(-1)
                                                                                    .setEnabled(
                                                                                            true);
                                                                            message(
                                                                                    "Could not"
                                                                                        + " verify"
                                                                                        + " current"
                                                                                        + " password.");
                                                                        });
                                                    },
                                                    error -> {
                                                        dialog.getButton(-1).setEnabled(true);
                                                        message(error);
                                                    });
                                        }));
        activeDialog = dialog;
        dialog.show();
    }

    private void message(String text) {
        if (binding != null && isAdded())
            Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show();
    }

    private void returnHome() {
        if (getActivity() instanceof StaffDashBoardActivity)
            ((StaffDashBoardActivity) requireActivity()).selectTab(R.id.staff_nav_home);
        else requireActivity().finish();
    }

    @Override
    public void onSaveInstanceState(Bundle out) {
        out.putBoolean("staff.profilePopulated", profilePopulated);
        super.onSaveInstanceState(out);
    }

    @Override
    public void onDestroyView() {
        if (activeDialog != null) {
            activeDialog.dismiss();
            activeDialog = null;
        }
        binding = null;
        super.onDestroyView();
    }
}
