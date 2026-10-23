package com.groupproject.justeat.feature.staff;
import org.junit.Test;
import static org.junit.Assert.*;
public class StaffBookingPolicyTest {
    @Test public void datesAreStrictLocalCalendarDays() {
        assertTrue(StaffBookingPolicy.validDate("2028-02-29"));
        assertFalse(StaffBookingPolicy.validDate("2026-02-29"));
        assertFalse(StaffBookingPolicy.validDate("24/10/2026"));
        assertFalse(StaffBookingPolicy.validDate("2026-1-4"));
    }
    @Test public void completedStillHoldsTheDay() {
        for(String state:new String[]{"PENDING","ACCEPTED","PREPARING","READY","COMPLETED"})
            assertTrue(StaffBookingPolicy.holdsDay(state));
        assertFalse(StaffBookingPolicy.holdsDay("CANCELLED"));
        assertFalse(StaffBookingPolicy.holdsDay("REJECTED"));
        assertFalse(StaffBookingPolicy.releasesDay("COMPLETED"));
        assertTrue(StaffBookingPolicy.releasesDay("CANCELLED"));
        assertTrue(StaffBookingPolicy.releasesDay("REJECTED"));
    }
    @Test public void invalidPathsAreRejected() {
        assertTrue(StaffBookingPolicy.validKey("-firebase-key"));
        for(String key:new String[]{"","a/b","a.b","a#b","a$b","a[b","a]b","a\n"})
            assertFalse(StaffBookingPolicy.validKey(key));
    }
}
