package com.fongmi.android.tv.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
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
    private static volatile int backgroundColor = Theme.DEFAULT_BACKGROUND;
    private static volatile boolean loaded;

    /** 防抖重套用的间隔，避免布局回调里频繁刷新 */
    private static final long APPLY_DELAY = 150L;

    /** 每个 Activity 挂的监听（弱引用，随 Activity 一起回收） */
    private static final Map<Activity, Watch> watched = new WeakHashMap<>();

    /** 被全局背景覆盖过的页面根，关闭主题时用来还原 */
    private static final Map<View, Drawable> originalBackgrounds = new WeakHashMap<>();

    /** App 启动时调用：把本地配置读进内存，之后全局共用这一份 */
    public static void load(Context context) {
        enabled = Theme.isEnabled(context);
        textColor = Theme.getTextColor(context);
        buttonColor = Theme.getButtonColor(context);
        pressedColor = Theme.getPressedColor(context);
        radiusDp = Theme.getRadiusDp(context);
        backgroundColor = Theme.getBackgroundColor(context);
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

    public static int getBackgroundColor() {
        return backgroundColor;
    }

    /** 保存配置：更新内存态 + 写本地 + 立即套用到当前界面 */
    public static void save(Context context, boolean value, int text, int button, int pressed, int radius, int background) {
        enabled = value;
        textColor = text;
        buttonColor = button;
        pressedColor = pressed;
        radiusDp = radius;
        backgroundColor = background;
        Theme.save(context, value, text, button, pressed, radius, background);
        applyCurrent();
    }

    /** 恢复默认：清空本地配置并回到默认值（含还原页面背景） */
    public static void reset(Context context) {
        enabled = false;
        textColor = Theme.DEFAULT_TEXT;
        buttonColor = Theme.DEFAULT_BUTTON;
        pressedColor = Theme.DEFAULT_PRESSED;
        radiusDp = Theme.DEFAULT_RADIUS;
        backgroundColor = Theme.DEFAULT_BACKGROUND;
        Theme.reset(context);
        restoreAll();
        applyCurrent();
    }

    /** 套用到某个页面（文字/按钮，以及页面根背景） */
    public static void apply(Activity activity) {
        if (activity == null || !Util.isMobile()) return;
        if (activity.getWindow() != null) {
            Theme.apply(activity.getWindow().getDecorView());
        }
        applyBackground(findRoot(activity));
    }

    public static void apply(View view) {
        if (!loaded) return;
        Theme.apply(view);
    }

    /**
     * 背景作用在页面根布局上。
     * 开启主题且选了不透明色 -> 记录原背景并覆盖；关闭主题或选了「无」-> 还原。
     */
    public static void applyBackground(View root) {
        if (root == null || !Util.isMobile()) return;
        boolean want = enabled && backgroundColor != 0;
        if (want) {
            if (!originalBackgrounds.containsKey(root)) originalBackgrounds.put(root, root.getBackground());
            root.setBackgroundColor(backgroundColor);
        } else {
            Drawable original = originalBackgrounds.remove(root);
            if (original != null) root.setBackground(original);
        }
    }

    public static void restoreAll() {
        for (Map.Entry<View, Drawable> entry : originalBackgrounds.entrySet()) {
            View view = entry.getKey();
            if (view != null) view.setBackground(entry.getValue());
        }
        originalBackgrounds.clear();
    }

    /** 给 Activity 的 DecorView 挂全局布局监听：页面/列表内容变化后自动重新套用 */
    public static void watch(Activity activity) {
        if (activity == null || activity.isFinishing() || !Util.isMobile()) return;
        View decor = activity.getWindow() != null ? activity.getWindow().getDecorView() : null;
        if (decor == null || watched.containsKey(activity)) return;
        Runnable task = () -> {
            if (enabled) {
                Theme.apply(decor);
                applyBackground(findRoot(activity));
            }
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

    /** 页面根：Activity 用内容根（各 Fragment 的根由 BaseFragment 单独处理） */
    private static View findRoot(Activity activity) {
        try {
            return activity.findViewById(android.R.id.content);
        } catch (Exception e) {
            return activity.getWindow() != null ? activity.getWindow().getDecorView() : null;
        }
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
