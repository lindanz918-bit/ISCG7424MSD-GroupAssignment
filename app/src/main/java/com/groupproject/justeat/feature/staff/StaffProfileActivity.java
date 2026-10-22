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
        setTheme(com.groupproject.justeat.R.style.Theme_JustEat_Staff);
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(StaffProfileViewModel.class);
        getBinding().etStaffEmail.setEnabled(false);
        viewModel.getLoaded().observe(this, user -> {
            if(user==null) return;
            getBinding().etStaffFirstName.setText(user.getFirstName());
            getBinding().etStaffLastName.setText(user.getLastName());
            getBinding().etStaffEmail.setText(user.getEmail());
            getBinding().etStaffPhone.setText(user.getPhone()==null?"":user.getPhone().toString());
            com.bumptech.glide.Glide.with(this).load(user.getAvatarUrl()).placeholder(com.groupproject.justeat.R.drawable.staff_avatar_placeholder)
                .error(com.groupproject.justeat.R.drawable.staff_avatar_placeholder).circleCrop().into(getBinding().staffAvatar);
        });
        viewModel.getLoading().observe(this, loading -> getBinding().btnStaffProfileSave.setEnabled(!Boolean.TRUE.equals(loading)));
        getBinding().btnStaffAvatar.setOnClickListener(v -> changeAvatar());
        getBinding().btnStaffPassword.setOnClickListener(v -> changeCredentials(false));
        getBinding().btnStaffEmail.setOnClickListener(v -> changeCredentials(true));
        getBinding().btnStaffProfileCancel.setOnClickListener(v -> finish());
        viewModel.load();
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
                Toast.makeText(this, "Profile saved.", Toast.LENGTH_SHORT).show();
                viewModel.consumeSaved();
            }
        });
        viewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                viewModel.consumeError();
            }
        });
    }

    /** Store a public HTTPS avatar URL; no storage bucket or local file permission required. */
    private void changeAvatar() {
        android.widget.EditText input=new android.widget.EditText(this);
        input.setHint("https://…"); input.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_URI);
        new androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Avatar image URL").setView(input)
            .setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w) -> {
                String url=input.getText().toString().trim();
                if(!url.startsWith("https://") || !android.util.Patterns.WEB_URL.matcher(url).matches()) { message("Enter a valid HTTPS image URL."); return; }
                StaffAccess.check(() -> {
                    String uid=com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
                    com.google.firebase.database.FirebaseDatabase.getInstance().getReference("users").child(uid).child("avatarUrl")
                        .setValue(url).addOnSuccessListener(v -> {
                            if(viewModel.getLoaded().getValue()!=null) viewModel.getLoaded().getValue().setAvatarUrl(url);
                            if(isFinishing() || isDestroyed()) return;
                            com.bumptech.glide.Glide.with(this).load(url).placeholder(com.groupproject.justeat.R.drawable.staff_avatar_placeholder)
                                .error(com.groupproject.justeat.R.drawable.staff_avatar_placeholder).circleCrop().into(getBinding().staffAvatar); message("Avatar saved.");
                        })
                        .addOnFailureListener(e -> message(e.getMessage()));
                },this::message);
            }).show();
    }
    private void changeCredentials(boolean email) {
        android.widget.LinearLayout form=new android.widget.LinearLayout(this); form.setOrientation(1); form.setPadding(32,16,32,16);
        android.widget.EditText current=new android.widget.EditText(this); current.setHint("Current password");
        current.setInputType(129); form.addView(current);
        android.widget.EditText replacement=new android.widget.EditText(this);
        replacement.setHint(email?"New email address":"New password (at least 6 characters)");
        replacement.setInputType(email?33:129); form.addView(replacement);
        android.widget.EditText confirm=new android.widget.EditText(this); confirm.setHint(email?"Confirm email":"Confirm password"); confirm.setInputType(email?33:129); form.addView(confirm);
        androidx.appcompat.app.AlertDialog dialog=new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(email?"Change email":"Change password").setView(form).setNegativeButton("Cancel",null)
            .setPositiveButton("Update",null).create();
        dialog.setOnShowListener(v -> dialog.getButton(-1).setOnClickListener(button -> {
            String next=replacement.getText().toString(), password=current.getText().toString();
            if(password.isEmpty() || !next.equals(confirm.getText().toString()) || (email?!android.util.Patterns.EMAIL_ADDRESS.matcher(next).matches():next.length()<6)) {
                message("Check your current password and matching new values."); return;
            }
            dialog.getButton(-1).setEnabled(false);
            StaffAccess.check(() -> {
                com.google.firebase.auth.FirebaseUser user=com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                if(user==null || user.getEmail()==null) { dialog.getButton(-1).setEnabled(true); message("Please sign in again."); return; }
                user.reauthenticate(com.google.firebase.auth.EmailAuthProvider.getCredential(user.getEmail(),password))
                    .addOnSuccessListener(ignored -> {
                        com.google.android.gms.tasks.Task<Void> task=email?user.verifyBeforeUpdateEmail(next):user.updatePassword(next);
                        task.addOnSuccessListener(done -> { dialog.dismiss(); message(email?"Verification sent to your new email. Verify it, then sign in again.":"Password updated."); })
                            .addOnFailureListener(e -> { dialog.getButton(-1).setEnabled(true); message(e.getMessage()); });
                    }).addOnFailureListener(e -> { dialog.getButton(-1).setEnabled(true); message("Could not verify current password."); });
            }, error -> { dialog.getButton(-1).setEnabled(true); message(error); });
        }));
        dialog.show();
    }
    private void message(String text) { if(!isFinishing() && !isDestroyed()) Toast.makeText(this,text,Toast.LENGTH_LONG).show(); }
}
