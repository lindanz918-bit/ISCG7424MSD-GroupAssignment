package com.groupproject.justeat.base;

import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

/**
 * 使用 ViewBinding 的 Activity 基类。
 * 子类只负责提供具体 Binding 的 inflate 方法，不需要重复 setContentView。
 *
 * @param <VB> 当前 Activity 对应的 ViewBinding 类型
 */
public abstract class BaseActivity<VB extends ViewBinding> extends AppCompatActivity {

    /** 仅在 Activity 创建后、销毁前有效的页面 Binding。 */
    private VB binding;

    /**
     * 创建当前页面的 ViewBinding。
     *
     * @param inflater Activity 的布局加载器
     * @return 已创建的 Binding
     */
    protected abstract VB inflateBinding(LayoutInflater inflater);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = inflateBinding(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    /**
     * 获取当前页面 Binding。
     *
     * @return 当前有效 Binding
     * @throws IllegalStateException 在 Binding 尚未创建或已经释放时抛出
     */
    protected final VB getBinding() {
        if (binding == null) {
            throw new IllegalStateException("Binding is unavailable");
        }
        return binding;
    }

    @Override
    protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }
}

