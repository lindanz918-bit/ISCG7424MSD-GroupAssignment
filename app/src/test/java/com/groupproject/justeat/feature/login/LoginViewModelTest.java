package com.groupproject.justeat.feature.login;

import static org.junit.Assert.*;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import com.groupproject.justeat.data.model.User;
import com.groupproject.justeat.data.repository.AuthRepository;
import com.groupproject.justeat.data.repository.RepositoryCallback;
import org.junit.Rule;
import org.junit.Test;

public class LoginViewModelTest {
    @Rule public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();
    private final FakeAuth auth = new FakeAuth();
    private final LoginViewModel vm = new LoginViewModel(new LoginValidator(), auth);

    @Test public void invalidInputDoesNotAuthenticate() {
        vm.validateLogin("", "");
        assertNull(auth.callback);
        assertFalse(vm.getLoading().getValue());
    }
    @Test public void waitsForAuthenticationAndPreventsDuplicateRequests() {
        vm.validateLogin(" user@example.com ", "123456");
        vm.validateLogin("user@example.com", "123456");
        assertTrue(vm.getLoading().getValue());
        assertEquals(1, auth.calls);
        assertEquals("user@example.com", auth.email);
        assertNull(vm.getDestinationRole().getValue());
    }
    @Test public void activeRolesCanNavigateAndEventIsConsumed() {
        for (String role : new String[]{"MANAGER", "CUSTOMER", "STAFF"}) {
            vm.validateLogin("user@example.com", "123456");
            auth.callback.onSuccess(user(role, "ACTIVE"));
            assertEquals(role, vm.getDestinationRole().getValue());
            assertFalse(vm.getLoading().getValue());
            vm.onNavigationHandled();
            assertNull(vm.getDestinationRole().getValue());
        }
    }
    @Test public void rejectsMissingUnknownAndInactiveProfiles() {
        for (User user : new User[]{null, user("ADMIN", "ACTIVE"), user("STAFF", "SUSPENDED"), user("CUSTOMER", null)}) {
            vm.validateLogin("user@example.com", "123456");
            auth.callback.onSuccess(user);
            assertNull(vm.getDestinationRole().getValue());
            assertNotNull(vm.getErrorMessage().getValue());
            assertFalse(vm.getLoading().getValue());
        }
        assertEquals(4, auth.signOuts);
    }
    @Test public void failureAllowsRetryAndErrorCanBeConsumed() {
        vm.validateLogin("user@example.com", "123456");
        auth.callback.onError("Login failed");
        assertEquals("Login failed", vm.getErrorMessage().getValue());
        assertFalse(vm.getLoading().getValue());
        vm.onMessageShown();
        assertNull(vm.getErrorMessage().getValue());
        vm.validateLogin("user@example.com", "123456");
        assertEquals(2, auth.calls);
    }
    private static User user(String role, String status) {
        User user = new User(); user.setRole(role); user.setStatus(status); return user;
    }
    static class FakeAuth implements AuthRepository {
        RepositoryCallback<User> callback; int calls; int signOuts; String email;
        public void signIn(String email, String password, RepositoryCallback<User> callback) {
            calls++; this.email = email; this.callback = callback;
        }
        public void signOut(RepositoryCallback<Void> callback) { signOuts++; callback.onSuccess(null); }
        public String getCurrentUserId() { return null; }
        public boolean isSignedIn() { return false; }
    }
}
