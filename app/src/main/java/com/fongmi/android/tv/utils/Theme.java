package com.fongmi.android.tv.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.widget.TextView;

import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.fongmi.android.tv.R;
import com.github.catvod.utils.Prefers;

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

    /** 预设色板（ picker 用） */
    public static final int[] PRESETS = {
            0xFFFFFFFF, 0xFF000000,
            0xFFE53935, 0xFFD81B60, 0xFF8E24AA, 0xFF5E35B1,
            0xFF3949AB, 0xFF1E88E5, 0xFF00ACC1, 0xFF00897B,
            0xFF43A047, 0xFF7CB342, 0xFFFB8C00, 0xFFF4511E,
            0xFF8D6E63, 0xFF546E7A,
    };

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
