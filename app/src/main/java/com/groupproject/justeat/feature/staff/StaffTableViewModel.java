package com.groupproject.justeat.feature.staff;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.groupproject.justeat.base.BaseViewModel;
import com.groupproject.justeat.data.model.RestaurantTable;
import com.groupproject.justeat.domain.constant.TableStatus;

import java.util.Collections;
import java.util.List;

/** 员工餐桌可用状态页面的状态层。 */
public class StaffTableViewModel extends BaseViewModel {
    private final MutableLiveData<List<RestaurantTable>> tables =
            new MutableLiveData<>(sampleTables());

    public LiveData<List<RestaurantTable>> getTables() {
        return tables;
    }

    public void loadTables() {
        setLoading(false);
        tables.setValue(sampleTables());
    }

    public void toggleAvailability(RestaurantTable table) {
        if (table == null) return;
        if (!TableStatus.AVAILABLE.equals(table.getStatus())
                && !TableStatus.UNAVAILABLE.equals(table.getStatus())) {
            setErrorMessage("Reserved or occupied tables cannot be changed here.");
            return;
        }
        String next = TableStatus.AVAILABLE.equals(table.getStatus())
                ? TableStatus.UNAVAILABLE : TableStatus.AVAILABLE;
        table.setStatus(next);
        List<RestaurantTable> current = tables.getValue();
        tables.setValue(current == null ? Collections.singletonList(table) : current);
    }

    public void consumeError() {
        setErrorMessage(null);
    }

    private static List<RestaurantTable> sampleTables() {
        List<RestaurantTable> sample = new java.util.ArrayList<>();
        sample.add(new RestaurantTable("T01", "01", 4, "Main dining", TableStatus.AVAILABLE,
                System.currentTimeMillis(), System.currentTimeMillis()));
        sample.add(new RestaurantTable("T02", "02", 2, "Window", TableStatus.UNAVAILABLE,
                System.currentTimeMillis(), System.currentTimeMillis()));
        sample.add(new RestaurantTable("T03", "03", 6, "Patio", TableStatus.RESERVED,
                System.currentTimeMillis(), System.currentTimeMillis()));
        return sample;
    }
}
