package com.groupproject.justeat.feature.staff;

import com.groupproject.justeat.R;

import java.util.regex.Pattern;

/** 员工资料页面的本地输入规则。 */
public final class StaffProfileValidator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private StaffProfileValidator() {
    }

    public static StaffProfileUiState validate(String firstName, String lastName,
                                               String email, String phone) {
        String safeFirstName = trim(firstName);
        String safeLastName = trim(lastName);
        String safeEmail = trim(email);
        String safePhone = trim(phone);
        int firstError = safeFirstName.isEmpty() ? R.string.error_profile_first_name_required : 0;
        int lastError = safeLastName.isEmpty() ? R.string.error_profile_last_name_required : 0;
        int emailError = safeEmail.isEmpty()
                ? R.string.error_profile_email_required
                : (EMAIL_PATTERN.matcher(safeEmail).matches()
                ? 0 : R.string.error_profile_email_invalid);
        int phoneError = safePhone.isEmpty() ? R.string.error_profile_phone_required : 0;
        return new StaffProfileUiState(safeFirstName, safeLastName, safeEmail, safePhone,
                firstError, lastError, emailError, phoneError);
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
