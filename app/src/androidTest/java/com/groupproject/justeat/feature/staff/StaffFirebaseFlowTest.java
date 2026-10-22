package com.groupproject.justeat.feature.staff;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.groupproject.justeat.R;
import com.groupproject.justeat.feature.dashboard.StaffDashBoardActivity;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.junit.Assert.*;

/**
 * Real Android UI + Auth + RTDB integration; fixture writes are restricted to a demo emulator.
 * Start tools/staff-emulator/firebase.json with project demo-justeat-staff (auth,database),
 * build assembleDebug and assembleDebugAndroidTest, install both APKs on a dedicated AVD,
 * then run this class with androidx.test.runner.AndroidJUnitRunner.
 * Screenshots: target app external-files/staff-qa. No production credentials are stored here.
 * The emulator rules are test fixtures, NOT a replacement for the team's production rules.
 */
@RunWith(AndroidJUnit4.class)
public class StaffFirebaseFlowTest {
    private final Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private String uid;
    private void configureDemo() throws Exception {
        FirebaseApp.getInstance().delete();
        FirebaseApp.initializeApp(context,new FirebaseOptions.Builder()
            .setApplicationId("1:123456:android:staffqa").setApiKey("fake-api-key")
            .setProjectId("demo-justeat-staff")
            .setDatabaseUrl("http://10.0.2.2:9000?ns=demo-justeat-staff-default-rtdb").build());
        auth=FirebaseAuth.getInstance(); auth.useEmulator("10.0.2.2",9099);
        db=FirebaseDatabase.getInstance(); db.useEmulator("10.0.2.2",9000);
        try { Tasks.await(auth.createUserWithEmailAndPassword("staff@example.test","StaffTest123!"),15,TimeUnit.SECONDS); }
        catch(Exception ignored) { Tasks.await(auth.signInWithEmailAndPassword("staff@example.test","StaffTest123!"),15,TimeUnit.SECONDS); }
        uid=auth.getUid(); assertNotNull(uid);
    }
    private void seed(String status) throws Exception {
        // Emulator administrative endpoint only. No configurable host / project can redirect this write.
        JSONObject staff=new JSONObject().put("userId",uid).put("firstName","Alex").put("lastName","Staff")
            .put("email","staff@example.test").put("phone","0211234567").put("role","STAFF").put("status","ACTIVE");
        JSONObject guest=new JSONObject().put("firstName","Taylor").put("lastName","Guest").put("role","CUSTOMER").put("status","ACTIVE");
        JSONObject order=new JSONObject().put("orderId","qa-order").put("customerId","qa-guest").put("guestCount","2")
            .put("tableId","T1").put("bookingDate","22/10/2026").put("createdAt",1).put("status",status);
        JSONObject meal=new JSONObject().put("mealName","Grilled salmon").put("price","24.00").put("menuItemId","qa-meal");
        JSONObject table=new JSONObject().put("tableNumber","T1").put("capacity",4).put("status","AVAILABLE");
        JSONObject root=new JSONObject().put("users",new JSONObject().put(uid,staff).put("qa-guest",guest))
            .put("Orders",new JSONObject().put("qa-order",order))
            .put("OrderItems",new JSONObject().put("qa-order",new JSONObject().put("0",meal)))
            .put("tables",new JSONObject().put("qa-table-key",table));
        admin("",root.toString());
    }
    private void admin(String path,String json) throws Exception {
        HttpURLConnection connection=(HttpURLConnection)new URL("http://10.0.2.2:9000/"+path+".json?ns=demo-justeat-staff-default-rtdb").openConnection();
        connection.setRequestMethod("PUT"); connection.setRequestProperty("Authorization","Bearer owner");
        connection.setRequestProperty("Content-Type","application/json"); connection.setDoOutput(true);
        connection.setConnectTimeout(10000); connection.setReadTimeout(10000);
        try(java.io.OutputStream out=connection.getOutputStream()) { out.write(json.getBytes(StandardCharsets.UTF_8)); }
        assertEquals(200,connection.getResponseCode()); connection.disconnect();
    }
    private void waitValue(String path,Object expected) throws Exception {
        long end=System.currentTimeMillis()+15000;
        Object actual=null;
        do {
            actual=Tasks.await(db.getReference(path).get(),5,TimeUnit.SECONDS).getValue();
            if(expected.equals(actual)) return;
            Thread.sleep(200);
        } while(System.currentTimeMillis()<end);
        assertEquals(path,expected,actual);
    }
    private void settle() throws Exception { Thread.sleep(1500); InstrumentationRegistry.getInstrumentation().waitForIdleSync(); }
    private void screenshot(String name) throws Exception {
        settle();
        File folder=new File(context.getExternalFilesDir(null),"staff-qa"); assertTrue(folder.exists() || folder.mkdirs());
        Bitmap image=null;
        // Software-rendered AVDs may return null during a surface transition.
        for(int attempt=0; attempt<5 && image==null; attempt++) {
            image=InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
            if(image==null) Thread.sleep(500);
        }
        assertNotNull("Screenshot surface available for "+name,image);
        try(FileOutputStream out=new FileOutputStream(new File(folder,name+".png"))) { assertTrue(image.compress(Bitmap.CompressFormat.PNG,100,out)); }
    }
    @Test public void staffEndToEnd() throws Exception {
        // Optional real-account check is strictly read-only, including profile. Password is an ephemeral runner argument.
        android.os.Bundle args=InstrumentationRegistry.getArguments();
        if("true".equals(args.getString("liveReadOnly"))) {
            auth=FirebaseAuth.getInstance(); db=FirebaseDatabase.getInstance();
            Tasks.await(auth.signInWithEmailAndPassword(args.getString("email"),args.getString("password")),30,TimeUnit.SECONDS);
            String role=Tasks.await(db.getReference("users").child(auth.getUid()).child("role").get(),15,TimeUnit.SECONDS).getValue(String.class);
            assertEquals("STAFF",role);
            assertEquals("ACTIVE",Tasks.await(db.getReference("users").child(auth.getUid()).child("status").get(),15,TimeUnit.SECONDS).getValue(String.class));
            try(ActivityScenario<StaffDashBoardActivity> screen=ActivityScenario.launch(StaffDashBoardActivity.class)) { screenshot("live-dashboard"); }
            try(ActivityScenario<StaffOrderActivity> screen=ActivityScenario.launch(StaffOrderActivity.class)) { screenshot("live-orders"); }
            try(ActivityScenario<StaffTableActivity> screen=ActivityScenario.launch(StaffTableActivity.class)) { screenshot("live-tables"); }
            try(ActivityScenario<StaffProfileActivity> screen=ActivityScenario.launch(StaffProfileActivity.class)) { screenshot("live-profile"); }
            auth.signOut(); return;
        }
        configureDemo(); seed("PENDING"); waitValue("users/"+uid+"/role","STAFF");
        try(ActivityScenario<StaffOrderActivity> screen=ActivityScenario.launch(StaffOrderActivity.class)) {
            settle(); screenshot("01-pending");
            onView(withId(R.id.btn_staff_accept)).perform(click()); waitValue("Orders/qa-order/status","ACCEPTED");
            screenshot("02-accepted");
        }
        Intent detail=new Intent(context,StaffOrderDetailsActivity.class).putExtra("orderId","qa-order");
        try(ActivityScenario<StaffOrderDetailsActivity> screen=ActivityScenario.launch(detail)) {
            settle(); screenshot("03a-details");
            onView(withText("Complete order")).check(matches(org.hamcrest.Matchers.not(isEnabled())));
            onView(withText("Serve")).perform(scrollTo(),click()); waitValue("Orders/qa-order/servedItems/0",true);
            screenshot("03-served");
            onView(withText("Complete order")).perform(scrollTo(),click()); waitValue("Orders/qa-order/status","COMPLETED");
            screenshot("04-completed"); screen.recreate(); settle();
            onView(withText("Served")).check(matches(org.hamcrest.Matchers.not(isEnabled())));
        }
        seed("PENDING");
        try(ActivityScenario<StaffOrderActivity> screen=ActivityScenario.launch(StaffOrderActivity.class)) {
            settle(); onView(withId(R.id.btn_staff_reject)).perform(click()); onView(withText("Confirm")).perform(click());
            waitValue("Orders/qa-order/status","REJECTED"); screenshot("05-rejected");
        }
        seed("ACCEPTED");
        try(ActivityScenario<StaffTableActivity> screen=ActivityScenario.launch(StaffTableActivity.class)) {
            settle(); screenshot("06-tables-booking");
            onView(withId(R.id.btn_staff_table_toggle)).perform(click()); waitValue("tables/qa-table-key/status","UNAVAILABLE");
            screenshot("07-unavailable"); screen.recreate(); settle();
            onView(withId(R.id.btn_staff_table_toggle)).perform(click()); waitValue("tables/qa-table-key/status","AVAILABLE");
            settle(); onView(withId(R.id.btn_staff_table_occupancy)).perform(click()); onView(withText("Confirm")).perform(click());
            waitValue("tables/qa-table-key/status","OCCUPIED"); screenshot("07b-occupied");
            onView(withId(R.id.btn_staff_table_occupancy)).perform(click()); onView(withText("Confirm")).perform(click());
            waitValue("tables/qa-table-key/status","AVAILABLE");
        }
        try(ActivityScenario<StaffOrderActivity> screen=ActivityScenario.launch(StaffOrderActivity.class)) {
            settle(); onView(withId(R.id.btn_staff_cancel)).perform(click()); onView(withText("Confirm")).perform(click());
            waitValue("Orders/qa-order/status","CANCELLED"); screenshot("08-cancelled");
        }
        try(ActivityScenario<StaffProfileActivity> screen=ActivityScenario.launch(StaffProfileActivity.class)) {
            settle(); screenshot("09a-profile");
            // Exercise the same gesture as a user; nested form containers can make
            // Espresso's requestRectangleOnScreen-based scrollTo stop too early.
            onView(isAssignableFrom(android.widget.ScrollView.class)).perform(swipeUp());
            onView(withId(R.id.btn_staff_password)).check(matches(isDisplayed()));
            screenshot("09b-profile-security");
            onView(withId(R.id.et_staff_first_name)).perform(scrollTo(),replaceText(""),closeSoftKeyboard());
            onView(withId(R.id.btn_staff_profile_save)).perform(scrollTo(),click());
            screenshot("09-profile-validation");
            onView(withId(R.id.et_staff_first_name)).perform(scrollTo(),replaceText("Alex Updated"),closeSoftKeyboard());
            onView(withId(R.id.btn_staff_profile_save)).perform(scrollTo(),click()); waitValue("users/"+uid+"/firstName","Alex Updated");
            screen.recreate(); settle();
            onView(withId(R.id.et_staff_first_name)).check(matches(withText("Alex Updated")));
            onView(withId(R.id.et_staff_first_name)).perform(scrollTo()); screenshot("10-profile-saved");
            onView(isAssignableFrom(android.widget.ScrollView.class)).perform(swipeUp());
            onView(withId(R.id.btn_staff_password)).perform(click()); screenshot("11-password-dialog");
            onView(withHint("Current password")).perform(replaceText("StaffTest123!"));
            onView(withHint("New password (at least 6 characters)")).perform(replaceText("StaffChanged123!"));
            onView(withHint("Confirm password")).perform(replaceText("StaffChanged123!"),closeSoftKeyboard());
            onView(withText("Update")).perform(click()); settle();
            try {
                Tasks.await(auth.signInWithEmailAndPassword("staff@example.test","StaffChanged123!"),15,TimeUnit.SECONDS);
            } finally {
                Tasks.await(auth.getCurrentUser().updatePassword("StaffTest123!"),15,TimeUnit.SECONDS);
            }
            onView(withId(R.id.btn_staff_avatar)).perform(scrollTo(),click());
            onView(withHint("https://…")).perform(replaceText("https://example.test/staff-avatar.png"),closeSoftKeyboard());
            onView(withText("Save")).perform(click()); waitValue("users/"+uid+"/avatarUrl","https://example.test/staff-avatar.png");
            onView(isAssignableFrom(android.widget.ScrollView.class)).perform(swipeUp());
            onView(withId(R.id.btn_staff_email)).perform(click());
            screenshot("12-email-verification-dialog");
            onView(withHint("Current password")).perform(replaceText("StaffTest123!"));
            onView(withHint("New email address")).perform(replaceText("updated@example.test"));
            onView(withHint("Confirm email")).perform(replaceText("updated@example.test"),closeSoftKeyboard());
            onView(withText("Update")).perform(click()); settle();
            HttpURLConnection connection=(HttpURLConnection)new URL("http://10.0.2.2:9099/emulator/v1/projects/demo-justeat-staff/oobCodes").openConnection();
            connection.setConnectTimeout(10000); connection.setReadTimeout(10000);
            String codes;
            try(java.util.Scanner scanner=new java.util.Scanner(connection.getInputStream()).useDelimiter("\\A")) { codes=scanner.hasNext()?scanner.next():""; }
            connection.disconnect();
            assertTrue("Email verification request emitted by Auth emulator", codes.contains("updated@example.test"));
        }
        admin("users/"+uid+"/status","\"INACTIVE\"");
        try {
            Tasks.await(db.getReference("tables/qa-table-key/status").setValue("UNAVAILABLE"),10,TimeUnit.SECONDS);
            fail("Inactive Staff must not be able to update tables");
        } catch(java.util.concurrent.ExecutionException expected) { /* Expected server rejection. */ }
        admin("users/"+uid+"/status","\"ACTIVE\"");
        admin("users/"+uid+"/role","\"CUSTOMER\"");
        try {
            Tasks.await(db.getReference("Orders/qa-order/status").setValue("ACCEPTED"),10,TimeUnit.SECONDS);
            fail("Customer must not perform Staff updates");
        } catch(java.util.concurrent.ExecutionException expected) { /* Expected server rejection. */ }
        auth.signOut();
    }
}
