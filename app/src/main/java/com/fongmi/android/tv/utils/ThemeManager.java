package com.fongmi.android.tv.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * 全局主题状态管理。
 *
 * 配置只在这里存一份（内存 + 本地），所有页面读同一份；
 * 改动后立刻写入本地并套用到当前界面，做到全局一致、实时生效。
 */
public class ThemeManager {

    private static volatile boolean enabled;
    private static volatile int textColor = Theme.DEFAULT_TEXT;
    private static volatile int buttonColor = Theme.DEFAULT_BUTTON;
    private static volatile int pressedColor = Theme.DEFAULT_PRESSED;
    private static volatile int radiusDp = Theme.DEFAULT_RADIUS;
    private static volatile boolean loaded;

    /** 防抖重套用的间隔，避免布局回调里频繁刷新 */
    private static final long APPLY_DELAY = 150L;

    /** 每个 Activity 挂的监听（弱引用，随 Activity 一起回收） */
    private static final Map<Activity, Watch> watched = new WeakHashMap<>();

    /** App 启动时调用：把本地配置读进内存，之后全局共用这一份 */
    public static void load(Context context) {
        enabled = Theme.isEnabled(context);
        textColor = Theme.getTextColor(context);
        buttonColor = Theme.getButtonColor(context);
        pressedColor = Theme.getPressedColor(context);
        radiusDp = Theme.getRadiusDp(context);
        loaded = true;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static int getTextColor() {
        return textColor;
    }

    public static int getButtonColor() {
        return buttonColor;
    }

    public static int getPressedColor() {
        return pressedColor;
    }

    public static int getRadiusDp() {
        return radiusDp;
    }

    /** 保存配置：更新内存态 + 写本地 + 立即套用到当前界面 */
    public static void save(Context context, boolean value, int text, int button, int pressed, int radius) {
        enabled = value;
        textColor = text;
        buttonColor = button;
        pressedColor = pressed;
        radiusDp = radius;
        Theme.save(context, value, text, button, pressed, radius);
        applyCurrent();
    }

    /** 恢复默认：清空本地配置并回到默认值 */
    public static void reset(Context context) {
        enabled = false;
        textColor = Theme.DEFAULT_TEXT;
        buttonColor = Theme.DEFAULT_BUTTON;
        pressedColor = Theme.DEFAULT_PRESSED;
        radiusDp = Theme.DEFAULT_RADIUS;
        Theme.reset(context);
        applyCurrent();
    }

    /** 套用到某个页面 */
    public static void apply(Activity activity) {
        if (activity == null || activity.getWindow() == null) return;
        Theme.apply(activity.getWindow().getDecorView());
    }

    public static void apply(View view) {
        if (!loaded) return;
        Theme.apply(view);
    }

    /** 给 Activity 的 DecorView 挂全局布局监听：页面/列表内容变化后自动重新套用 */
    public static void watch(Activity activity) {
        if (activity == null || activity.isFinishing() || !Util.isMobile()) return;
        View decor = activity.getWindow() != null ? activity.getWindow().getDecorView() : null;
        if (decor == null || watched.containsKey(activity)) return;
        Runnable task = () -> {
            if (enabled) Theme.apply(decor);
        };
        ViewTreeObserver.OnGlobalLayoutListener listener = () -> com.fongmi.android.tv.App.post(task, APPLY_DELAY);
        Watch watch = new Watch(listener, task);
        watched.put(activity, watch);
        decor.getViewTreeObserver().addOnGlobalLayoutListener(listener);
    }

    public static void unwatch(Activity activity) {
        Watch watch = watched.remove(activity);
        if (watch == null || activity == null || activity.getWindow() == null) return;
        View decor = activity.getWindow().getDecorView();
        decor.getViewTreeObserver().removeOnGlobalLayoutListener(watch.listener);
        com.fongmi.android.tv.App.removeCallbacks(watch.task);
    }

    private static void applyCurrent() {
        Activity activity = com.fongmi.android.tv.App.activity();
        if (activity != null) apply(activity);
    }

    private static class Watch {
        final ViewTreeObserver.OnGlobalLayoutListener listener;
        final Runnable task;

        Watch(ViewTreeObserver.OnGlobalLayoutListener listener, Runnable task) {
            this.listener = listener;
            this.task = task;
        }
    }
}
