package com.groupproject.justeat.feature.login;

public class SignUpUiState {
    private final int firstNameErrorResId;
    private final int lastNameErrorResId;
    private final int emailErrorResId;
    private final int passwordErrorResId;

    public SignUpUiState() {
        this(0, 0, 0, 0);
    }

    public SignUpUiState(int firstNameErrorResId, int lastNameErrorResId, int emailErrorResId, int passwordErrorResId) {
        this.firstNameErrorResId = firstNameErrorResId;
        this.lastNameErrorResId = lastNameErrorResId;
        this.emailErrorResId = emailErrorResId;
        this.passwordErrorResId = passwordErrorResId;
    }

    public int getFirstNameErrorResId() { return firstNameErrorResId; }
    public int getLastNameErrorResId() { return lastNameErrorResId; }
    public int getEmailErrorResId() { return emailErrorResId; }
    public int getPasswordErrorResId() { return passwordErrorResId; }
}