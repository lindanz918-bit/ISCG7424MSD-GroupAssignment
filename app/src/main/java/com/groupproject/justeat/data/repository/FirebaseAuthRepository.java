package com.groupproject.justeat.data.repository;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.groupproject.justeat.data.model.User;

/** Auth 验证密码，Realtime Database 提供用户资料；不保存密码。 */
public final class FirebaseAuthRepository implements AuthRepository {
    @Override public void signIn(String email, String password, RepositoryCallback<User> callback) {
        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(result -> loadCurrentUser(callback))
                    .addOnFailureListener(error -> {
                        auth.signOut();
                        String message = "Unable to sign in. Check your email and password.";
                        if (error instanceof FirebaseNetworkException) message = "Network unavailable. Please try again.";
                        if (error instanceof FirebaseTooManyRequestsException) message = "Too many attempts. Please try again later.";
                        callback.onError(message);
                    });
        } catch (IllegalStateException error) {
            callback.onError("Firebase is not configured. Check google-services.json.");
        }
    }

    /** 同样用于 Dashboard 重新检查当前用户的角色。 */
    public void loadCurrentUser(RepositoryCallback<User> callback) {
        try {
            String uid = getCurrentUserId();
            if (uid == null) { callback.onError("Please sign in again."); return; }
            FirebaseDatabase.getInstance().getReference("users").child(uid).get()
                    .addOnSuccessListener(snapshot -> {
                        if (!uid.equals(getCurrentUserId())) {
                            callback.onError("Your session changed. Please sign in again.");
                            return;
                        }
                        try {
                            User user = snapshot.getValue(User.class);
                            if (user == null) { reject(callback, "User profile is missing. Contact the administrator."); return; }
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
    private void reject(RepositoryCallback<User> callback, String message) {
        signOut(new RepositoryCallback<Void>() {
            public void onSuccess(Void ignored) { callback.onError(message); }
            public void onError(String ignored) { callback.onError(message); }
        });
    }
    @Override public void signOut(RepositoryCallback<Void> callback) {
        try { FirebaseAuth.getInstance().signOut(); callback.onSuccess(null); }
        catch (IllegalStateException error) { callback.onError("Firebase is not configured."); }
    }
    @Override public String getCurrentUserId() {
        return FirebaseAuth.getInstance().getUid();
    }
    @Override public boolean isSignedIn() { return getCurrentUserId() != null; }
}
