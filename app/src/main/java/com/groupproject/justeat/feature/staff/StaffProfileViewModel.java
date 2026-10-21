package com.groupproject.justeat.feature.staff;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.groupproject.justeat.base.BaseViewModel;

/** 员工资料页面状态层；当前保存到页面内存状态，后续可接入持久化。 */
public class StaffProfileViewModel extends BaseViewModel {
    private final MutableLiveData<StaffProfileUiState> profile = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saved = new MutableLiveData<>(false);

    public LiveData<StaffProfileUiState> getProfile() {
        return profile;
    }

    public LiveData<Boolean> getSaved() {
        return saved;
    }

    public void validateAndSave(String firstName, String lastName, String email, String phone) {
        StaffProfileUiState state = StaffProfileValidator.validate(firstName, lastName, email, phone);
        profile.setValue(state);
        saved.setValue(false);
        if (state.isValid()) {
            saved.setValue(true);
        }
    }

    public void consumeError() {
        setErrorMessage(null);
    }
}
