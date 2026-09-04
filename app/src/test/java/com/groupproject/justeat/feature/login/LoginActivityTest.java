package com.groupproject.justeat.feature.login;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.groupproject.justeat.R;
import com.groupproject.justeat.databinding.ActivityLoginBinding;
import com.groupproject.justeat.ui.dialog.MessageDialog;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.android.controller.ActivityController;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {24, 35})
public class LoginActivityTest {

    @Test
    public void emptySubmitShowsBothRequiredErrors() {
        LoginActivity activity = Robolectric.buildActivity(LoginActivity.class).setup().get();
        ActivityLoginBinding binding = bindingFor(activity);

        binding.btnLogin.performClick();

        assertEquals(activity.getString(R.string.error_email_required),
                binding.tilEmail.getError().toString());
        assertEquals(activity.getString(R.string.error_password_required),
                binding.tilPassword.getError().toString());
    }

    @Test
    public void invalidSubmitFocusesFirstInvalidField() {
        LoginActivity activity = Robolectric.buildActivity(LoginActivity.class).setup().get();
        ActivityLoginBinding binding = bindingFor(activity);
        binding.etPassword.requestFocus();

        binding.btnLogin.performClick();

        assertTrue(binding.etEmail.hasFocus());
    }

    @Test
    public void validSubmitShowsMessageDialog() {
        LoginActivity activity = Robolectric.buildActivity(LoginActivity.class).setup().get();
        ActivityLoginBinding binding = bindingFor(activity);
        binding.etEmail.setText("customer@example.com");
        binding.etPassword.setText("123456");

        binding.btnLogin.performClick();
        activity.getSupportFragmentManager().executePendingTransactions();

        assertTrue(activity.getSupportFragmentManager()
                .findFragmentByTag(MessageDialog.TAG) instanceof MessageDialog);
    }

    @Test
    public void recreationDoesNotDuplicateSuccessDialog() {
        ActivityController<LoginActivity> controller =
                Robolectric.buildActivity(LoginActivity.class).setup();
        LoginActivity activity = controller.get();
        ActivityLoginBinding binding = bindingFor(activity);
        binding.etEmail.setText("customer@example.com");
        binding.etPassword.setText("123456");
        binding.btnLogin.performClick();
        activity.getSupportFragmentManager().executePendingTransactions();

        controller.recreate();
        LoginActivity recreated = controller.get();
        recreated.getSupportFragmentManager().executePendingTransactions();

        assertEquals(1, countMessageDialogs(recreated));
    }

    @Test
    public void repeatedValidSubmitWhileDialogIsVisibleKeepsSingleDialog() {
        LoginActivity activity = Robolectric.buildActivity(LoginActivity.class).setup().get();
        ActivityLoginBinding binding = bindingFor(activity);
        binding.etEmail.setText("customer@example.com");
        binding.etPassword.setText("123456");

        binding.btnLogin.performClick();
        activity.getSupportFragmentManager().executePendingTransactions();
        binding.btnLogin.performClick();
        activity.getSupportFragmentManager().executePendingTransactions();

        assertEquals(1, countMessageDialogs(activity));
    }

    @Test
    public void signUpShowsNotImplementedDialog() {
        LoginActivity activity = Robolectric.buildActivity(LoginActivity.class).setup().get();

        activity.findViewById(R.id.tvSignUp).performClick();
        activity.getSupportFragmentManager().executePendingTransactions();

        assertTrue(activity.getSupportFragmentManager()
                .findFragmentByTag(MessageDialog.TAG) instanceof MessageDialog);
    }

    private static ActivityLoginBinding bindingFor(LoginActivity activity) {
        ViewGroup content = activity.findViewById(android.R.id.content);
        return ActivityLoginBinding.bind(content.getChildAt(0));
    }

    private static int countMessageDialogs(LoginActivity activity) {
        int count = 0;
        for (Fragment fragment : activity.getSupportFragmentManager().getFragments()) {
            if (fragment instanceof MessageDialog) {
                count++;
            }
        }
        return count;
    }
}
