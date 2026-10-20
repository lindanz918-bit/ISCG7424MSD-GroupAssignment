package com.groupproject.justeat.feature.login;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.groupproject.justeat.R;
import com.groupproject.justeat.data.model.User;
import com.groupproject.justeat.domain.constant.UserStatus;

public class SignUpViewModel extends AndroidViewModel {

    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference usersDatabaseRef;
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
    private final MutableLiveData<String> destinationRole = new MutableLiveData<>(null);
    private final MutableLiveData<SignUpUiState> uiState = new MutableLiveData<>(new SignUpUiState());
    public SignUpViewModel(@NonNull Application application) {
        super(application);
        firebaseAuth = FirebaseAuth.getInstance();
        usersDatabaseRef = FirebaseDatabase.getInstance().getReference("users");
    }

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<String> getDestinationRole() { return destinationRole; }

    public void registerUser(String firstName, String lastName, String email, String password, String role) {
        uiState.setValue(new SignUpUiState());

        int firstNameErr = firstName.isEmpty() ? R.string.error_first_name_required : 0;
        int lastNameErr = lastName.isEmpty() ? R.string.error_last_name_required : 0;
        int emailErr = email.isEmpty() ? R.string.error_email_required : 0;
        int passwordErr = 0;

        if (password.isEmpty()) {
            passwordErr = R.string.error_password_required;
        } else if (password.length() < 6) {
            passwordErr = R.string.error_password_length; // "Password must be at least 6 characters"
        }

        if (firstNameErr != 0 || lastNameErr != 0 || emailErr != 0 || passwordErr != 0) {
            uiState.setValue(new SignUpUiState(firstNameErr, lastNameErr, emailErr, passwordErr));
            return;
        }

        loading.setValue(true);

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser() != null ? authResult.getUser().getUid() : "";
                    long currentTime = System.currentTimeMillis();

                    User newUser = new User(
                            uid, firstName, lastName, email,
                            "", "", role, UserStatus.ACTIVE,
                            currentTime, currentTime
                    );

                    usersDatabaseRef.child(uid).setValue(newUser)
                            .addOnSuccessListener(unused -> {
                                loading.setValue(false);
                                destinationRole.setValue(role);
                            })
                            .addOnFailureListener(e -> {
                                loading.setValue(false);
                                errorMessage.setValue("Failed to save user data: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    loading.setValue(false);
                    errorMessage.setValue(e.getMessage());
                });
    }

    public void onMessageShown() {
        errorMessage.setValue(null);
    }

    public void onNavigationHandled() {
        destinationRole.setValue(null);
    }
    public LiveData<SignUpUiState> getUiState() {
        return uiState;
    }
}