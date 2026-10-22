package com.groupproject.justeat.feature.staff;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.groupproject.justeat.R;

import org.junit.Test;

public class StaffProfileValidatorTest {

    @Test
    public void emptyNamesReturnFieldErrors() {
        StaffProfileUiState state = StaffProfileValidator.validate(" ", "", "staff@example.com", "021000000");

        assertEquals(R.string.error_profile_first_name_required, state.getFirstNameErrorResId());
        assertEquals(R.string.error_profile_last_name_required, state.getLastNameErrorResId());
        assertTrue(!state.isValid());
    }

    @Test
    public void malformedEmailReturnsEmailError() {
        StaffProfileUiState state = StaffProfileValidator.validate("Junhui", "Ye", "not-an-email", "021000000");

        assertEquals(R.string.error_profile_email_invalid, state.getEmailErrorResId());
        assertTrue(!state.isValid());
    }

    @Test
    public void validProfileTrimsValues() {
        StaffProfileUiState state = StaffProfileValidator.validate(" Junhui ", " Ye ",
                " staff@example.com ", " 021000000 ");

        assertTrue(state.isValid());
        assertEquals("Junhui", state.getFirstName());
        assertEquals("Ye", state.getLastName());
        assertEquals("staff@example.com", state.getEmail());
        assertEquals("021000000", state.getPhone());
    }
}
