package com.groupproject.justeat.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.groupproject.justeat.base.BaseDialog;
import com.groupproject.justeat.databinding.DialogMessageBinding;

/** 显示标题、正文和一个确认按钮的通用信息对话框。 */
public class MessageDialog extends BaseDialog<DialogMessageBinding> {

    /** FragmentManager 中使用的稳定标签。 */
    public static final String TAG = "MessageDialog";

    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_MESSAGE = "arg_message";

    /** Android 重建 Fragment 所需的公开无参构造方法。 */
    public MessageDialog() {
    }

    /**
     * 创建可被 Android 安全重建的信息对话框。
     *
     * @param title 对话框标题
     * @param message 对话框正文
     * @return 已写入 Fragment arguments 的对话框实例
     */
    public static MessageDialog newInstance(String title, String message) {
        MessageDialog dialog = new MessageDialog();
        Bundle arguments = new Bundle();
        arguments.putString(ARG_TITLE, title);
        arguments.putString(ARG_MESSAGE, message);
        dialog.setArguments(arguments);
        return dialog;
    }

    @Override
    protected DialogMessageBinding inflateBinding(LayoutInflater inflater,
                                                   @Nullable ViewGroup container) {
        return DialogMessageBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle arguments = getArguments();
        String title = arguments == null ? "" : arguments.getString(ARG_TITLE, "");
        String message = arguments == null ? "" : arguments.getString(ARG_MESSAGE, "");
        getBinding().tvDialogTitle.setText(title);
        getBinding().tvDialogMessage.setText(message);
        getBinding().btnDialogPositive.setOnClickListener(ignored -> dismiss());
    }
}

