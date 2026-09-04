package com.groupproject.justeat.feature.login;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.groupproject.justeat.R;

import org.junit.Before;
import org.junit.Test;

public class LoginValidatorTest {

    private LoginValidator validator;

    @Before
    public void setUp() {
        validator = new LoginValidator();
    }

    @Test
    public void emptyEmailReturnsRequiredError() {
        LoginUiState state = validator.validate("   ", "123456");

        assertEquals(R.string.error_email_required, state.getEmailErrorResId());
        assertFalse(state.isValidationSuccessful());
    }

    @Test
    public void malformedEmailReturnsFormatError() {
        LoginUiState state = validator.validate("not-an-email", "123456");

        assertEquals(R.string.error_email_invalid, state.getEmailErrorResId());
    }

    @Test
    public void emptyPasswordReturnsRequiredError() {
        LoginUiState state = validator.validate("customer@example.com", " ");

        assertEquals(R.string.error_password_required, state.getPasswordErrorResId());
    }

    @Test
    public void shortPasswordReturnsLengthError() {
        LoginUiState state = validator.validate("customer@example.com", "12345");

        assertEquals(R.string.error_password_too_short, state.getPasswordErrorResId());
    }

    @Test
    public void nullCredentialsReturnBothRequiredErrors() {
        LoginUiState state = validator.validate(null, null);

        assertEquals(R.string.error_email_required, state.getEmailErrorResId());
        assertEquals(R.string.error_password_required, state.getPasswordErrorResId());
    }

    @Test
    public void validCredentialsReturnSuccess() {
        LoginUiState state = validator.validate(" customer@example.com ", "123456");

        assertEquals(0, state.getEmailErrorResId());
        assertEquals(0, state.getPasswordErrorResId());
        assertTrue(state.isValidationSuccessful());
        assertEquals(R.string.validation_success_message, state.getMessageResId());
    }
}
