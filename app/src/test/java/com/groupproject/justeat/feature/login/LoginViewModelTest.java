package com.groupproject.justeat.feature.login;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.groupproject.justeat.R;

import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoginViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Test
    public void startsWithIdleUiState() {
        LoginViewModel viewModel = new LoginViewModel(new LoginValidator());

        LoginUiState state = viewModel.getUiState().getValue();

        assertNotNull(state);
        assertFalse(state.isLoading());
        assertEquals(0, state.getEmailErrorResId());
        assertEquals(0, state.getPasswordErrorResId());
        assertEquals(0, state.getMessageResId());
        assertFalse(state.isValidationSuccessful());
    }

    @Test
    public void invalidCredentialsPublishBothRequiredErrors() {
        LoginViewModel viewModel = new LoginViewModel(new LoginValidator());

        viewModel.validateLogin("", "");
        LoginUiState state = viewModel.getUiState().getValue();

        assertNotNull(state);
        assertEquals(R.string.error_email_required, state.getEmailErrorResId());
        assertEquals(R.string.error_password_required, state.getPasswordErrorResId());
        assertFalse(state.isValidationSuccessful());
    }

    @Test
    public void validationPublishesLoadingAndSuccess() {
        LoginViewModel viewModel = new LoginViewModel(new LoginValidator());
        List<Boolean> loadingValues = new ArrayList<>();
        Observer<Boolean> observer = loadingValues::add;
        viewModel.getLoading().observeForever(observer);

        viewModel.validateLogin("customer@example.com", "123456");
        LoginUiState state = viewModel.getUiState().getValue();

        assertEquals(Arrays.asList(false, true, false), loadingValues);
        assertNotNull(state);
        assertTrue(state.isValidationSuccessful());
        assertEquals(R.string.validation_success_message, state.getMessageResId());
        viewModel.getLoading().removeObserver(observer);
    }

    @Test
    public void shownMessageIsConsumedIntoIdleState() {
        LoginViewModel viewModel = new LoginViewModel(new LoginValidator());
        viewModel.validateLogin("customer@example.com", "123456");

        viewModel.onMessageShown();
        LoginUiState state = viewModel.getUiState().getValue();

        assertNotNull(state);
        assertEquals(0, state.getMessageResId());
        assertFalse(state.isValidationSuccessful());
    }
}
