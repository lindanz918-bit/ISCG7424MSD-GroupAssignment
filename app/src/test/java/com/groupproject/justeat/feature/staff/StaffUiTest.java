package com.groupproject.justeat.feature.staff;

import android.content.Context;
import android.widget.TextView;
import com.groupproject.justeat.R;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

/** Recycled status badges must reset both their text and semantic color. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class StaffUiTest {
    @Test public void pendingUsesAmberAndRetainsText() {
        Context context=RuntimeEnvironment.getApplication();
        TextView badge=new TextView(context);
        StaffUi.badge(badge,"PENDING");
        assertEquals("PENDING",badge.getText().toString());
        assertEquals(context.getColor(R.color.staff_pending),badge.getCurrentTextColor());
    }
    @Test public void recyclingFromCancelledToAcceptedResetsColor() {
        Context context=RuntimeEnvironment.getApplication();
        TextView badge=new TextView(context);
        StaffUi.badge(badge,"CANCELLED");
        assertEquals(context.getColor(R.color.staff_error),badge.getCurrentTextColor());
        StaffUi.badge(badge,"ACCEPTED");
        assertEquals(context.getColor(R.color.staff_primary),badge.getCurrentTextColor());
        assertEquals("ACCEPTED",badge.getText().toString());
    }
    @Test public void absentStatusIsExplicitNotBlank() {
        TextView badge=new TextView(RuntimeEnvironment.getApplication());
        StaffUi.badge(badge,null);
        assertEquals("UNKNOWN",badge.getText().toString());
    }
}
