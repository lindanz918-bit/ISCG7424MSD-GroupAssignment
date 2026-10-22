package com.groupproject.justeat.feature.staff;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.groupproject.justeat.base.BaseViewModel;

/** 员工资料校验与 Firebase 持久化，不包含 Repository 层。 */
public class StaffProfileViewModel extends BaseViewModel {
    private final MutableLiveData<StaffProfileUiState> profile = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saved = new MutableLiveData<>(false);
    private final MutableLiveData<com.groupproject.justeat.data.model.User> loaded = new MutableLiveData<>();
    public LiveData<com.groupproject.justeat.data.model.User> getLoaded() { return loaded; }
    public void load() {
        if (loaded.getValue()!=null) return;
        setLoading(true);
        StaffAccess.check(() -> {
            String uid=com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
            com.google.firebase.database.FirebaseDatabase.getInstance().getReference("users").child(uid).get()
                .addOnSuccessListener(s -> {
                    try {
                        com.groupproject.justeat.data.model.User user=s.getValue(com.groupproject.justeat.data.model.User.class);
                        com.google.firebase.auth.FirebaseUser auth=com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                        if(user!=null && auth!=null) user.setEmail(auth.getEmail());
                        loaded.setValue(user);
                    }
                    catch(RuntimeException e) { setErrorMessage("Cannot read profile."); }
                    setLoading(false);
                }).addOnFailureListener(e -> failed(e.getMessage()));
        }, this::failed);
    }
    private void failed(String message) { setLoading(false); setErrorMessage(message); }

    public LiveData<StaffProfileUiState> getProfile() {
        return profile;
    }

    public LiveData<Boolean> getSaved() {
        return saved;
    }

    public void validateAndSave(String firstName, String lastName, String email, String phone) {
        StaffProfileUiState state = StaffProfileValidator.validate(firstName, lastName, email, phone);
        profile.setValue(state);
        saved.setValue(false);
        if (!state.isValid() || Boolean.TRUE.equals(getLoading().getValue())) return;
        setLoading(true);
        StaffAccess.check(() -> {
            com.google.firebase.auth.FirebaseUser auth=com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if(auth==null) { failed("Please sign in again."); return; }
            java.util.Map<String,Object> changes=new java.util.HashMap<>();
            changes.put("firstName",state.getFirstName()); changes.put("lastName",state.getLastName());
            changes.put("phone",state.getPhone()); changes.put("updatedAt",com.google.firebase.database.ServerValue.TIMESTAMP);
            // Email changes use Firebase Authentication verification, not a profile-only overwrite.
            changes.put("email",auth.getEmail());
            com.google.firebase.database.FirebaseDatabase.getInstance().getReference("users").child(auth.getUid())
                .updateChildren(changes).addOnSuccessListener(v -> {
                    com.groupproject.justeat.data.model.User user=loaded.getValue();
                    if(user!=null) {
                        user.setFirstName(state.getFirstName()); user.setLastName(state.getLastName());
                        user.setPhone(state.getPhone()); user.setEmail(auth.getEmail());
                    }
                    setLoading(false); saved.setValue(true);
                })
                .addOnFailureListener(e -> failed(e.getMessage()));
        },this::failed);
    }
    public void consumeSaved() { saved.setValue(false); }

    public void consumeError() {
        setErrorMessage(null);
    }
}
