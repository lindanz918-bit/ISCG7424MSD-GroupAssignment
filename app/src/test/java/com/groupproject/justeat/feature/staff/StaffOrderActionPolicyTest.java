package com.groupproject.justeat.feature.staff;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.groupproject.justeat.domain.constant.OrderStatus;

import org.junit.Test;

public class StaffOrderActionPolicyTest {
    @Test public void completionRequiresAllMealsServed() {
        assertTrue(StaffOrderActionPolicy.canComplete("ACCEPTED",true));
        assertTrue(StaffOrderActionPolicy.canComplete("PREPARING",true));
        assertTrue(StaffOrderActionPolicy.canComplete("READY",true));
        assertFalse(StaffOrderActionPolicy.canComplete("ACCEPTED",false));
        assertFalse(StaffOrderActionPolicy.canComplete("PENDING",true));
        assertFalse(StaffOrderActionPolicy.canComplete("CANCELLED",true));
        assertFalse(StaffOrderActionPolicy.canComplete("COMPLETED",true));
    }
    @Test public void unknownAndNullStatusesFailClosed() {
        assertFalse(StaffOrderActionPolicy.isActive(null));
        assertFalse(StaffOrderActionPolicy.canApply(null,"ACCEPTED"));
        assertFalse(StaffOrderActionPolicy.canApply("PENDING",null));
        assertFalse(StaffOrderActionPolicy.canApply("REJECTED","ACCEPTED"));
    }

    @Test
    public void pendingOrderCanBeAcceptedOrRejected() {
        assertTrue(StaffOrderActionPolicy.canApply(OrderStatus.PENDING, OrderStatus.ACCEPTED));
        assertTrue(StaffOrderActionPolicy.canApply(OrderStatus.PENDING, OrderStatus.REJECTED));
        assertFalse(StaffOrderActionPolicy.canApply(OrderStatus.PENDING, OrderStatus.COMPLETED));
    }

    @Test
    public void acceptedOrderCanBeCancelledButTerminalOrderCannotChange() {
        assertTrue(StaffOrderActionPolicy.canApply(OrderStatus.ACCEPTED, OrderStatus.CANCELLED));
        assertFalse(StaffOrderActionPolicy.canApply(OrderStatus.COMPLETED, OrderStatus.CANCELLED));
        assertFalse(StaffOrderActionPolicy.canApply(OrderStatus.CANCELLED, OrderStatus.ACCEPTED));
    }

    @Test
    public void actionLabelMatchesTargetStatus() {
        assertEquals(StaffOrderActionPolicy.ACTION_ACCEPT,
                StaffOrderActionPolicy.actionLabel(OrderStatus.ACCEPTED));
        assertEquals(StaffOrderActionPolicy.ACTION_REJECT,
                StaffOrderActionPolicy.actionLabel(OrderStatus.REJECTED));
        assertEquals(StaffOrderActionPolicy.ACTION_CANCEL,
                StaffOrderActionPolicy.actionLabel(OrderStatus.CANCELLED));
    }
}
