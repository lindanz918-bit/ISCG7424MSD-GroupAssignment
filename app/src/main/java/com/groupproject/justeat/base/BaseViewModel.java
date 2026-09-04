package com.groupproject.justeat.base;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * 所有页面 ViewModel 的基础类。
 * 统一提供加载状态和通用错误信息，但不会持有 Activity、View 或 Context。
 */
public abstract class BaseViewModel extends ViewModel {

    /** 页面是否正在执行耗时操作。 */
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    /** 无法归属到具体输入框的通用错误信息。 */
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    /**
     * 获取只读加载状态。
     *
     * @return 加载状态 LiveData
     */
    public LiveData<Boolean> getLoading() {
        return loading;
    }

    /**
     * 获取只读通用错误信息。
     *
     * @return 错误信息 LiveData；没有错误时值为 null
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * 供子类更新加载状态。
     *
     * @param value true 表示正在加载
     */
    protected void setLoading(boolean value) {
        loading.setValue(value);
    }

    /**
     * 供子类更新通用错误信息。
     *
     * @param value 要展示的错误信息；传入 null 可清除错误
     */
    protected void setErrorMessage(String value) {
        errorMessage.setValue(value);
    }
}
