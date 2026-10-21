package com.groupproject.justeat.feature.staff;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.groupproject.justeat.domain.constant.OrderStatus;

import org.junit.Test;

public class StaffOrderActionPolicyTest {

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
