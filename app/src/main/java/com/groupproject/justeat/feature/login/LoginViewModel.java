package com.groupproject.justeat.feature.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.groupproject.justeat.base.BaseViewModel;
import com.groupproject.justeat.data.model.User;
import com.groupproject.justeat.domain.constant.UserRole;
import com.groupproject.justeat.domain.constant.UserStatus;
import com.groupproject.justeat.util.FirebaseAuthClient;

/** 校验输入、执行认证，并发布一次性的角色导航事件。 */
public class LoginViewModel extends BaseViewModel {
    private final LoginValidator validator;
    private final FirebaseAuthClient authClient;
    private final MutableLiveData<LoginUiState> uiState = new MutableLiveData<>(LoginUiState.idle());
    private final MutableLiveData<String> destinationRole = new MutableLiveData<>();
    private boolean cleared;

    public LoginViewModel() { this(new LoginValidator(), new FirebaseAuthClient()); }
    LoginViewModel(LoginValidator validator, FirebaseAuthClient authClient) {
        this.validator = validator; this.authClient = authClient;
    }
    public LiveData<LoginUiState> getUiState() { return uiState; }
    public LiveData<String> getDestinationRole() { return destinationRole; }
    public void validateLogin(String email, String password) {
        if (Boolean.TRUE.equals(getLoading().getValue())) return;
        setErrorMessage(null);
        LoginUiState validation = validator.validate(email, password);
        if (!validation.isValidationSuccessful()) { uiState.setValue(validation); return; }
        setLoading(true);
        uiState.setValue(LoginUiState.loading());
        authClient.signIn(email.trim(), password, new FirebaseAuthClient.Callback<User>() {
            @Override public void onSuccess(User user) {
                if (cleared) return;
                String role = user == null ? null : user.getRole();
                if (user == null || !UserStatus.ACTIVE.equals(user.getStatus()) || !isSupportedRole(role)) {
                    authClient.signOut(new FirebaseAuthClient.Callback<Void>() {
                        public void onSuccess(Void ignored) { fail("Your account is unavailable or has no valid role. Contact the administrator."); }
                        public void onError(String ignored) { fail("Unable to verify your account. Please sign in again."); }
                    });
                    return;
                }
                setLoading(false);
                uiState.setValue(LoginUiState.idle());
                destinationRole.setValue(role);
            }
            @Override public void onError(String message) { fail(message); }
        });
    }
    public static boolean isSupportedRole(String role) {
        return UserRole.MANAGER.equals(role) || UserRole.CUSTOMER.equals(role) || UserRole.STAFF.equals(role);
    }
    private void fail(String message) {
        if (cleared) return;
        setLoading(false); uiState.setValue(LoginUiState.idle()); setErrorMessage(message);
    }
    public void onMessageShown() { setErrorMessage(null); }
    public void onNavigationHandled() { destinationRole.setValue(null); }
    @Override protected void onCleared() { cleared = true; }
}
