package com.groupproject.justeat.feature.staff;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.groupproject.justeat.base.BaseViewModel;
import com.groupproject.justeat.data.model.Order;
import com.groupproject.justeat.domain.constant.OrderStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 员工订单状态层；默认不假定 Firebase 实现已经存在。 */
public class StaffOrderViewModel extends BaseViewModel {
    private final MutableLiveData<List<Order>> orders =
            new MutableLiveData<>(sampleOrders());

    public LiveData<List<Order>> getOrders() {
        return orders;
    }

    public void loadPendingOrders() {
        setLoading(false);
        orders.setValue(sampleOrders());
    }

    public void applyStatus(Order order, String targetStatus) {
        if (order == null || !StaffOrderActionPolicy.canApply(order.getStatus(), targetStatus)) {
            setErrorMessage("This order cannot be moved to that status.");
            return;
        }
        order.setStatus(targetStatus);
        List<Order> current = orders.getValue();
        List<Order> updated = current == null ? new ArrayList<>() : new ArrayList<>(current);
        updated.remove(order);
        orders.setValue(updated);
    }

    public void consumeError() {
        setErrorMessage(null);
    }

    private static List<Order> sampleOrders() {
        List<Order> sample = new ArrayList<>();
        sample.add(new Order("ORD-1001", "John Smith", "2", "20/10/2026",
                "T03", OrderStatus.PENDING, System.currentTimeMillis()));
        sample.add(new Order("ORD-1002", "Emily Chen", "4", "20/10/2026",
                "T01", OrderStatus.PENDING, System.currentTimeMillis()));
        return sample;
    }
}
