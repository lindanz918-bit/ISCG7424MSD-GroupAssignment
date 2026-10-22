package com.groupproject.justeat.util;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.groupproject.justeat.data.model.User;

/**
 * 轻量 Firebase 认证客户端。
 *
 * 页面和 ViewModel 直接使用这个客户端，保持认证流程扁平。
 */
public class FirebaseAuthClient {
    public interface Callback<T> {
        void onSuccess(T value);
        void onError(String message);
    }

    public void signIn(String email, String password, Callback<User> callback) {
        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(result -> loadCurrentUser(callback))
                    .addOnFailureListener(error -> {
                        auth.signOut();
                        String message = "Unable to sign in. Check your email and password.";
                        if (error instanceof FirebaseNetworkException) {
                            message = "Network unavailable. Please try again.";
                        }
                        if (error instanceof FirebaseTooManyRequestsException) {
                            message = "Too many attempts. Please try again later.";
                        }
                        callback.onError(message);
                    });
        } catch (IllegalStateException error) {
            callback.onError("Firebase is not configured. Check google-services.json.");
        }
    }

    /** 重新读取当前用户资料，用于登录后和 Dashboard 进入时的角色校验。 */
    public void loadCurrentUser(Callback<User> callback) {
        try {
            String uid = getCurrentUserId();
            if (uid == null) {
                callback.onError("Please sign in again.");
                return;
            }
            FirebaseDatabase.getInstance().getReference("users").child(uid).get()
                    .addOnSuccessListener(snapshot -> {
                        if (!uid.equals(getCurrentUserId())) {
                            callback.onError("Your session changed. Please sign in again.");
                            return;
                        }
                        try {
                            User user = snapshot.getValue(User.class);
                            if (user == null) {
                                reject(callback, "User profile is missing. Contact the administrator.");
                                return;
                            }
                            user.setUserId(uid);
                            callback.onSuccess(user);
                        } catch (RuntimeException error) {
                            reject(callback, "Invalid user profile. Contact the administrator.");
                        }
                    })
                    .addOnFailureListener(error -> reject(callback,
                            "Unable to read your profile. Check your connection or contact the administrator."));
        } catch (IllegalStateException | com.google.firebase.database.DatabaseException error) {
            reject(callback, "Firebase Database is not configured correctly.");
        }
    }

    public void signOut(Callback<Void> callback) {
        try {
            FirebaseAuth.getInstance().signOut();
            callback.onSuccess(null);
        } catch (IllegalStateException error) {
            callback.onError("Firebase is not configured.");
        }
    }

    public String getCurrentUserId() {
        return FirebaseAuth.getInstance().getUid();
    }

    public boolean isSignedIn() {
        return getCurrentUserId() != null;
    }

    private void reject(Callback<User> callback, String message) {
        signOut(new Callback<Void>() {
            @Override public void onSuccess(Void ignored) { callback.onError(message); }
            @Override public void onError(String ignored) { callback.onError(message); }
        });
    }
}
