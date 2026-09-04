package com.groupproject.justeat.base;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewbinding.ViewBinding;

/**
 * 使用 ViewBinding 的 DialogFragment 基类。
 *
 * @param <VB> 当前对话框对应的 ViewBinding 类型
 */
public abstract class BaseDialog<VB extends ViewBinding> extends DialogFragment {

    /** 仅在对话框 View 生命周期内有效的 Binding。 */
    private VB binding;

    /**
     * 创建当前对话框的 ViewBinding。
     *
     * @param inflater 布局加载器
     * @param container 父容器，可能为 null
     * @return 已创建的 Binding
     */
    protected abstract VB inflateBinding(LayoutInflater inflater, @Nullable ViewGroup container);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = inflateBinding(inflater, container);
        return binding.getRoot();
    }

    /**
     * 获取当前对话框 Binding。
     *
     * @return 当前有效 Binding
     */
    protected final VB getBinding() {
        if (binding == null) {
            throw new IllegalStateException("Dialog binding is unavailable");
        }
        return binding;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        Window window = dialog == null ? null : dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}

