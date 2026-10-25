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
    public void authenticationFailureShowsOneDialogAfterRecreation() {
        ActivityController<LoginActivity> controller = Robolectric.buildActivity(LoginActivity.class).create();
        LoginViewModelTest.FakeAuth auth = new LoginViewModelTest.FakeAuth();
        LoginViewModel vm = new LoginViewModel(new LoginValidator(), auth);
        controller.get().getViewModelStore().put("androidx.lifecycle.ViewModelProvider.DefaultKey:" + LoginViewModel.class.getCanonicalName(), vm);
        controller.recreate().start().resume().visible();
        ActivityLoginBinding binding = bindingFor(controller.get());
        binding.etEmail.setText("user@example.com");
        binding.etPassword.setText("123456");
        binding.btnLogin.performClick();
        assertTrue(!binding.btnLogin.isEnabled());
        auth.callback.onError("Invalid email or password");
        assertTrue(binding.btnLogin.isEnabled());
        assertEquals(1, countMessageDialogs(controller.get()));
        assertTrue(org.robolectric.Shadows.shadowOf(controller.get()).getNextStartedActivity() == null);
        controller.recreate();
        assertEquals(1, countMessageDialogs(controller.get()));
        controller.destroy();
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
