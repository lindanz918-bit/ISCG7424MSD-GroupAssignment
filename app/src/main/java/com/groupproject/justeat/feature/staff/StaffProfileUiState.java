package com.groupproject.justeat.feature.staff;

/** 员工资料校验和保存页面的不可变状态。 */
public final class StaffProfileUiState {
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phone;
    private final int firstNameErrorResId;
    private final int lastNameErrorResId;
    private final int emailErrorResId;
    private final int phoneErrorResId;

    public StaffProfileUiState(String firstName, String lastName, String email, String phone,
                               int firstNameErrorResId, int lastNameErrorResId,
                               int emailErrorResId, int phoneErrorResId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.firstNameErrorResId = firstNameErrorResId;
        this.lastNameErrorResId = lastNameErrorResId;
        this.emailErrorResId = emailErrorResId;
        this.phoneErrorResId = phoneErrorResId;
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public int getFirstNameErrorResId() { return firstNameErrorResId; }
    public int getLastNameErrorResId() { return lastNameErrorResId; }
    public int getEmailErrorResId() { return emailErrorResId; }
    public int getPhoneErrorResId() { return phoneErrorResId; }

    public boolean isValid() {
        return firstNameErrorResId == 0 && lastNameErrorResId == 0
                && emailErrorResId == 0 && phoneErrorResId == 0;
    }
}
