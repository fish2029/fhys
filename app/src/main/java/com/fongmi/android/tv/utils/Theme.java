package com.fongmi.android.tv.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.widget.TextView;

import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.fongmi.android.tv.R;
import com.github.catvod.utils.Prefers;

import java.util.ArrayList;
import java.util.List;

/**
 * 全局主题：自定义按钮样式（背景色 / 圆角 / 点击态）+ 全局文字颜色。
 * 配置本地持久化，开启后全局实时生效。
 */
public class Theme {

    private static final String KEY_ENABLE = "theme_enabled";
    private static final String KEY_TEXT = "theme_text_color";
    private static final String KEY_BUTTON = "theme_button_color";
    private static final String KEY_PRESSED = "theme_pressed_color";
    private static final String KEY_RADIUS = "theme_radius";
    private static final String KEY_BACKGROUND = "theme_background";

    /** 默认背景：0 表示不覆盖（用页面自身背景） */
    public static final int DEFAULT_BACKGROUND = 0;

    /** 首页紫色胶囊同色系，作为默认按钮色 */
    public static final int DEFAULT_TEXT = 0xFFFFFFFF;
    public static final int DEFAULT_BUTTON = 0xFF8B5CF6;
    public static final int DEFAULT_PRESSED = 0xFF6D28D9;
    public static final int DEFAULT_RADIUS = 20;

    /** 预设色板（picker 用）：无彩色 + 12 色相各三档深浅，默认值一并收录 */
    public static final int[] PRESETS = buildPresets();

    private static int[] buildPresets() {
        List<Integer> colors = new ArrayList<>();
        // 无彩色：白 -> 黑
        int[] gray = {0xFFFFFFFF, 0xFFF5F5F5, 0xFFBDBDBD, 0xFF9E9E9E, 0xFF757575, 0xFF616161, 0xFF424242, 0xFF212121, 0xFF000000};
        for (int color : gray) colors.add(color);
        // 有彩色：12 个色相（每 30°）× 浅 / 标准 / 深 三档
        float[] hsv = new float[3];
        for (int i = 0; i < 12; i++) {
            hsv[0] = i * 30f;
            hsv[1] = 0.45f; hsv[2] = 1.00f; colors.add(Color.HSVToColor(hsv));
            hsv[1] = 0.80f; hsv[2] = 0.92f; colors.add(Color.HSVToColor(hsv));
            hsv[1] = 0.88f; hsv[2] = 0.66f; colors.add(Color.HSVToColor(hsv));
        }
        // 应用默认的紫色系也放进去，方便直接选回
        colors.add(DEFAULT_BUTTON);
        colors.add(DEFAULT_PRESSED);
        int[] result = new int[colors.size()];
        for (int i = 0; i < result.length; i++) result[i] = colors.get(i);
        return result;
    }

    public static boolean isEnabled(Context context) {
        return Prefers.getBoolean(KEY_ENABLE);
    }

    public static int getTextColor(Context context) {
        return Prefers.getInt(KEY_TEXT, DEFAULT_TEXT);
    }

    public static int getButtonColor(Context context) {
        return Prefers.getInt(KEY_BUTTON, DEFAULT_BUTTON);
    }

    public static int getPressedColor(Context context) {
        return Prefers.getInt(KEY_PRESSED, DEFAULT_PRESSED);
    }

    public static int getRadiusDp(Context context) {
        return Prefers.getInt(KEY_RADIUS, DEFAULT_RADIUS);
    }

    public static int getBackgroundColor(Context context) {
        return Prefers.getInt(KEY_BACKGROUND, DEFAULT_BACKGROUND);
    }

    public static void save(Context context, boolean enabled, int text, int button, int pressed, int radius, int background) {
        Prefers.put(KEY_ENABLE, enabled);
        Prefers.put(KEY_TEXT, text);
        Prefers.put(KEY_BUTTON, button);
        Prefers.put(KEY_PRESSED, pressed);
        Prefers.put(KEY_RADIUS, radius);
        Prefers.put(KEY_BACKGROUND, background);
    }

    public static void reset(Context context) {
        Prefers.remove(KEY_ENABLE);
        Prefers.remove(KEY_TEXT);
        Prefers.remove(KEY_BUTTON);
        Prefers.remove(KEY_PRESSED);
        Prefers.remove(KEY_RADIUS);
        Prefers.remove(KEY_BACKGROUND);
    }

    /** 圆角矩形底（单个状态） */
    private static Drawable roundRect(int color, float radiusPx) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(radiusPx);
        drawable.setColor(color);
        return drawable;
    }

    /** 按钮底：默认 / 按下 / 选中 三态 */
    public static Drawable buttonBackground(Context context) {
        float radius = getRadiusDp(context) * context.getResources().getDisplayMetrics().density;
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed}, roundRect(getPressedColor(context), radius));
        states.addState(new int[]{android.R.attr.state_selected}, roundRect(getPressedColor(context), radius));
        states.addState(new int[]{-android.R.attr.state_enabled}, roundRect(getButtonColor(context), radius));
        states.addState(new int[]{}, roundRect(getButtonColor(context), radius));
        return states;
    }

    /** 文字色：背景为不透明色时若对比不足，自动换成黑/白保证可读 */
    public static int readableTextColor(Context context) {
        int background = getBackgroundColor(context);
        int text = getTextColor(context);
        if (background == 0 || (background >>> 24) < 220) return text;
        return contrast(text, background) >= 3f ? text : autoTextColor(background);
    }

    private static int autoTextColor(int background) {
        return luminance(background) > 0.45f ? 0xFF000000 : 0xFFFFFFFF;
    }

    private static float luminance(int color) {
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        return 0.2126f * r + 0.7152f * g + 0.0722f * b;
    }

    private static float contrast(int a, int b) {
        float la = luminance(a) + 0.05f;
        float lb = luminance(b) + 0.05f;
        return Math.max(la, lb) / Math.min(la, lb);
    }

    /** 标记整棵视图树不参与全局主题（如站源弹窗） */
    public static void skip(View view) {
        if (view != null) view.setTag(R.id.theme_skip, Boolean.TRUE);
    }

    /** 递归套用到整棵视图树（仅手机版生效） */
    public static void apply(View view) {
        if (view == null || !Util.isMobile() || !isEnabled(view.getContext())) return;
        apply(view, view.getContext());
    }

    private static void apply(View view, Context context) {
        // 打了 theme_skip 标记的视图树整体跳过（站源弹窗保持上一版样式）
        if (Boolean.TRUE.equals(view.getTag(R.id.theme_skip))) return;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                apply(group.getChildAt(i), context);
            }
        }
        if (!(view instanceof TextView) || view instanceof EditText) return;
        TextView target = (TextView) view;
        target.setTextColor(readableTextColor(context));
        // 开关/进度条等控件只改文字色，不覆盖自身轨道/滑块背景；Chip 算按钮要刷
        if (isSwitchLike(view)) return;
        // 有底色或可点击的 TextView 都视为按钮，套用全局按钮样式
        if (view.getBackground() != null || view.isClickable()) {
            view.setBackground(buttonBackground(context));
            if (view instanceof com.google.android.material.button.MaterialButton) {
                // MaterialButton 的背景 tint 会在状态变化时盖回原来颜色，清掉
                ((com.google.android.material.button.MaterialButton) view).setBackgroundTintList(null);
            }
        }
    }

    private static boolean isSwitchLike(View view) {
        for (Class<?> type = view.getClass(); type != null && type != View.class; type = type.getSuperclass()) {
            String name = type.getName();
            if (name.equals("com.google.android.material.chip.Chip")) return false;
        }
        return view instanceof android.widget.Switch
                || view instanceof android.widget.CheckBox
                || view instanceof android.widget.RadioButton
                || view instanceof android.widget.ToggleButton
                || view instanceof android.widget.SeekBar
                || view instanceof android.widget.ProgressBar;
    }
}
