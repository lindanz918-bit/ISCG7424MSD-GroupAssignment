package com.groupproject.justeat.util;

import android.widget.ImageView;

import androidx.annotation.DrawableRes;

import com.bumptech.glide.Glide;

/** 对 Glide 常用图片请求的轻量封装。 */
public final class GlideImageLoader {

    private GlideImageLoader() {
        throw new AssertionError("No instances");
    }

    /**
     * 加载本地资源、文件或网络地址。
     *
     * @param view 目标 ImageView
     * @param source Glide 支持的图片数据源
     */
    public static void load(ImageView view, Object source) {
        Glide.with(view).load(source).into(view);
    }

    /**
     * 加载带占位图和错误图的图片。
     *
     * @param view 目标 ImageView
     * @param source Glide 支持的图片数据源
     * @param placeholder 加载过程显示的 drawable
     * @param error 加载失败显示的 drawable
     */
    public static void load(ImageView view, Object source,
                            @DrawableRes int placeholder, @DrawableRes int error) {
        Glide.with(view)
                .load(source)
                .placeholder(placeholder)
                .error(error)
                .into(view);
    }

    /**
     * 以圆形裁剪方式加载头像。
     *
     * @param view 目标 ImageView
     * @param source Glide 支持的图片数据源
     * @param placeholder 加载和失败时显示的 drawable
     */
    public static void loadCircle(ImageView view, Object source,
                                  @DrawableRes int placeholder) {
        Glide.with(view)
                .load(source)
                .placeholder(placeholder)
                .error(placeholder)
                .circleCrop()
                .into(view);
    }
}

