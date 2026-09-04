package com.groupproject.justeat.feature.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.groupproject.justeat.base.BaseViewModel;

/**
 * 登录页面 ViewModel。
 * 当前仅协调本地输入校验，后续可在此调用 AuthRepository。
 */
public class LoginViewModel extends BaseViewModel {

    /** 无 Android Context 依赖的输入校验器。 */
    private final LoginValidator validator;

    /** 登录页面的完整渲染状态。 */
    private final MutableLiveData<LoginUiState> uiState =
            new MutableLiveData<>(LoginUiState.idle());

    /** Android ViewModelProvider 使用的公开无参构造方法。 */
    public LoginViewModel() {
        this(new LoginValidator());
    }

    /** 测试和未来依赖注入使用的构造方法。 */
    LoginViewModel(LoginValidator validator) {
        this.validator = validator;
    }

    /** @return 供 Activity 观察的只读页面状态 */
    public LiveData<LoginUiState> getUiState() {
        return uiState;
    }

    /**
     * 校验登录输入并按顺序发布加载与结果状态。
     *
     * @param email 用户输入的邮箱
     * @param password 用户输入的密码；不会被保存或记录
     */
    public void validateLogin(String email, String password) {
        setLoading(true);
        uiState.setValue(LoginUiState.loading());

        LoginUiState result = validator.validate(email, password);

        setLoading(false);
        uiState.setValue(result);
    }

    /**
     * 标记一次性提示已经由页面处理，避免 Activity 重建时重复展示。
     */
    public void onMessageShown() {
        LoginUiState currentState = uiState.getValue();
        if (currentState != null && currentState.getMessageResId() != 0) {
            uiState.setValue(LoginUiState.idle());
        }
    }
}
