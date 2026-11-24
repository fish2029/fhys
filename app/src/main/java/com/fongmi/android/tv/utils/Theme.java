package com.fongmi.android.tv.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.widget.CompoundButtonCompat;

import com.fongmi.android.tv.R;
import com.github.catvod.utils.Prefers;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
        return states(context, radius);
    }

    private static StateListDrawable states(Context context, float radiusPx) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed}, roundRect(getPressedColor(context), radiusPx));
        states.addState(new int[]{android.R.attr.state_selected}, roundRect(getPressedColor(context), radiusPx));
        states.addState(new int[]{-android.R.attr.state_enabled}, roundRect(getButtonColor(context), radiusPx));
        states.addState(new int[]{}, roundRect(getButtonColor(context), radiusPx));
        return states;
    }

    /** 文字色：背景为不透明色时若对比不足，自动换成黑/白保证可读 */
    public static int readableTextColor(Context context) {
        int background = getBackgroundColor(context);
        int text = getTextColor(context);
        if (background == 0 || (background >>> 24) < 220) return text;
        return contrast(text, background) >= 3f ? text : autoTextColor(background);
    }

    /** 供 Adapter 绑定时使用：开主题用全局文字色，否则沿用原来的默认值 */
    public static int textOr(Context context, int fallback) {
        return isEnabled(context) ? readableTextColor(context) : fallback;
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

    /** 递归套用到整棵视图树（仅手机版生效）。root 表示自己就是页面/弹窗根，不接管自身底色 */
    public static void apply(View view) {
        if (view == null || !Util.isMobile() || !isEnabled(view.getContext())) return;
        apply(view, view.getContext(), true);
    }

    private static void apply(View view, Context context, boolean root) {
        // 打了 theme_skip 标记的视图树整体跳过（站源弹窗保持上一版样式）
        if (Boolean.TRUE.equals(view.getTag(R.id.theme_skip))) return;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                apply(group.getChildAt(i), context, false);
            }
        }

        int text = readableTextColor(context);

        // Chip 继承自 CheckBox：文字 + 底色一并接管
        if (view instanceof Chip) {
            Chip chip = (Chip) view;
            chip.setTextColor(text);
            chip.setChipBackgroundColor(ColorStateList.valueOf(getButtonColor(context)));
            return;
        }

        if (view instanceof TextView) {
            if (!(view instanceof EditText)) ((TextView) view).setTextColor(text);
            // 开关 / 复选 / 单选：文字跟着主题，滑块、轨道、按钮图标用主题色，不覆盖自身背景
            if (view instanceof CompoundButton) {
                tintCompound((CompoundButton) view, context);
                return;
            }
            if (view instanceof MaterialButton) {
                // MaterialButton 的背景 tint 会在状态变化时盖回原来颜色，清掉
                ((MaterialButton) view).setBackgroundTintList(null);
            }
            // 有底色或可点击的 TextView 都视为按钮，套用全局按钮样式
            if (!root && (view.getBackground() != null || view.isClickable())) {
                view.setBackground(buttonBackground(context));
            }
            return;
        }

        if (view instanceof SeekBar) {
            tintSeekBar((SeekBar) view, context);
            return;
        }
        if (view instanceof ProgressBar) {
            tintProgress((ProgressBar) view, context);
            return;
        }
        if (view instanceof BottomNavigationView) {
            tintNavigation((BottomNavigationView) view, context);
            return;
        }

        // 悬浮球：底色 + 描边 + 图标色一并接管（首页右下角三个）
        if (view instanceof FloatingActionButton) {
            FloatingActionButton fab = (FloatingActionButton) view;
            // 注：本工程解析到的 Material 版本没有 setStrokeColor，只接管底色和图标色
            fab.setBackgroundTintList(ColorStateList.valueOf(getButtonColor(context)));
            fab.setImageTintList(ColorStateList.valueOf(text));
            return;
        }

        // 带底色的图标按钮（搜索栏右侧收藏/历史、顶栏操作按钮等）接管底色和图标色
        if (view instanceof android.widget.ImageView) {
            android.widget.ImageView image = (android.widget.ImageView) view;
            if (!root && (view.getBackground() != null || view.isClickable())) {
                Drawable background = view.getBackground();
                view.setBackground(states(context, cornerRadius(background, context)));
                image.setImageTintList(null);
                image.setColorFilter(text);
            } else if (!root && (image.getColorFilter() != null || image.getImageTintList() != null)) {
                // 本身是着色的小图标（如搜索框里的放大镜）：跟着主题文字色走
                image.setImageTintList(ColorStateList.valueOf(text));
                image.clearColorFilter();
            }
            return;
        }

        // 其余控件/容器：接近不透明的纯色底（shape_item 之类）统一成主题按钮色
        // 页面/弹窗根自己的背景交给「全局背景」处理；ImageView 多是海报/封面，不接管
        if (!root && !(view instanceof android.widget.ImageView)) {
            Drawable background = view.getBackground();
            if (background != null && opaqueFill(background)) {
                view.setBackground(states(context, cornerRadius(background, context)));
            }
        }
    }

    private static void tintCompound(CompoundButton view, Context context) {
        ColorStateList on = ColorStateList.valueOf(getPressedColor(context));
        if (view instanceof SwitchCompat) {
            ((SwitchCompat) view).setThumbTintList(on);
            ((SwitchCompat) view).setTrackTintList(trackColor(context));
        } else if (view instanceof Switch) {
            ((Switch) view).setThumbTintList(on);
            ((Switch) view).setTrackTintList(trackColor(context));
        } else {
            CompoundButtonCompat.setButtonTintList(view, on);
        }
    }

    private static void tintSeekBar(SeekBar view, Context context) {
        ColorStateList on = ColorStateList.valueOf(getPressedColor(context));
        view.setProgressTintList(on);
        view.setThumbTintList(on);
    }

    private static void tintProgress(ProgressBar view, Context context) {
        ColorStateList on = ColorStateList.valueOf(getButtonColor(context));
        if (view.isIndeterminate()) view.setIndeterminateTintList(on);
        else view.setProgressTintList(on);
    }

    /** 底部导航：选中用主题文字色，未选中用同色低透明度 */
    private static void tintNavigation(BottomNavigationView view, Context context) {
        int on = readableTextColor(context);
        int off = Color.argb(150, Color.red(on), Color.green(on), Color.blue(on));
        ColorStateList colors = new ColorStateList(
                new int[][]{{android.R.attr.state_checked}, new int[0]},
                new int[]{on, off});
        view.setItemIconTintList(colors);
        view.setItemTextColor(colors);
    }

    /** 轨道色：主题色的低透明度版本 */
    private static ColorStateList trackColor(Context context) {
        int color = getPressedColor(context);
        return ColorStateList.valueOf(Color.argb(96, Color.red(color), Color.green(color), Color.blue(color)));
    }

    /** 是否接近不透明的纯色底（透明/半透明的玻璃、遮罩不算） */
    private static boolean opaqueFill(Drawable drawable) {
        if (drawable instanceof ColorDrawable) {
            return alphaOf(((ColorDrawable) drawable).getColor()) >= 200;
        }
        if (drawable instanceof GradientDrawable) {
            GradientDrawable gradient = (GradientDrawable) drawable;
            int[] colors = gradient.getColors();
            if (colors != null) {
                for (int color : colors) if (alphaOf(color) >= 200) return true;
                return false;
            }
            ColorStateList solid = gradient.getColor();
            return solid == null || alphaOf(solid.getDefaultColor()) >= 200;
        }
        if (drawable instanceof LayerDrawable) {
            LayerDrawable layer = (LayerDrawable) drawable;
            for (int i = 0; i < layer.getNumberOfLayers(); i++) {
                // ripple 的 mask 是纯白实心，只看内容层
                if (layer.getId(i) == android.R.id.mask) continue;
                if (opaqueFill(layer.getDrawable(i))) return true;
            }
            return false;
        }
        // selector / ripple：取当前显示的那个状态再判断
        Drawable current = drawable.getCurrent();
        return current != null && current != drawable && opaqueFill(current);
    }

    private static int alphaOf(int color) {
        return (color >>> 24) & 0xFF;
    }

    /** 尽量沿用原来的圆角，没有就用全局圆角 */
    private static float cornerRadius(Drawable drawable, Context context) {
        Drawable current = drawable.getCurrent();
        Drawable target = current != null && current != drawable ? current : drawable;
        float radius = findCornerRadius(target);
        return radius > 0 ? radius : getRadiusDp(context) * context.getResources().getDisplayMetrics().density;
    }

    /** 从 shape / layer-list / ripple 里挖出圆角；圆形返回一个大值（GradientDrawable 会自动收到半高） */
    private static float findCornerRadius(Drawable drawable) {
        if (drawable instanceof GradientDrawable) {
            GradientDrawable gradient = (GradientDrawable) drawable;
            try {
                if (gradient.getShape() == GradientDrawable.OVAL) return 9999f;
                float radius = gradient.getCornerRadius();
                if (radius > 0) return radius;
            } catch (Exception ignored) {
            }
            return -1f;
        }
        if (drawable instanceof LayerDrawable) {
            LayerDrawable layer = (LayerDrawable) drawable;
            for (int i = 0; i < layer.getNumberOfLayers(); i++) {
                if (layer.getId(i) == android.R.id.mask) continue;
                float radius = findCornerRadius(layer.getDrawable(i));
                if (radius > 0) return radius;
            }
            return -1f;
        }
        Drawable current = drawable.getCurrent();
        return current != null && current != drawable ? findCornerRadius(current) : -1f;
    }
}
