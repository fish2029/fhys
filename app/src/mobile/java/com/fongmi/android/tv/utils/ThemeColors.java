package com.fongmi.android.tv.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.ui.custom.CustomWallView;
import com.github.catvod.utils.Prefers;
import com.google.android.material.button.MaterialButton;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 全局主题配置：强调色 / 全局文字颜色 / 全局背景颜色
 * 色板与交互从繁花TV 移植：色点选择器点选即时生效，选择存 SharedPreferences。
 * View 体系应用方式：遍历界面，把使用紫色胶囊样式（shape_item 等注册表内的资源）的背景
 * 换成按强调色重建的同结构胶囊，把纯白文字/图标换成所选文字颜色。
 */
public abstract class ThemeColors {

    /** 强调色方案：base=默认态，light=渐变亮端，deep=渐变深端 */
    public static class Accent {
        public final int base, light, deep;
        Accent(int base, int light, int deep) { this.base = base; this.light = light; this.deep = deep; }
    }

    /** 手工调校的 6 档强调色（与设置页原紫色观感同源），其余档位由文字色板程序生成 */
    private static final Map<String, Accent> ACCENTS = new LinkedHashMap<>();

    /** 可选全局文字颜色，28 档全色系（含纯红/纯黄/纯绿/纯蓝） */
    public static final Map<String, Integer> TEXT_COLORS = new LinkedHashMap<>();

    /** 可选全局背景颜色，16 档深色系 */
    public static final Map<String, Integer> BG_COLORS = new LinkedHashMap<>();

    /** 可选按钮样式：方角 / 圆角 / 胶囊 */
    public static final Map<String, String> BUTTON_STYLES = new LinkedHashMap<>();

    /** 中文名：强调色（前 6 档专属名，其余回落文字颜色名） */
    private static final Map<String, String> ACCENT_LABELS = new LinkedHashMap<>();

    /** 中文名：文字颜色 / 背景颜色共用 */
    private static final Map<String, String> LABELS = new LinkedHashMap<>();

    static {
        ACCENTS.put("purple", new Accent(0xFF9D5CE0, 0xFFA78BFA, 0xFF7C3AED));
        ACCENTS.put("blue", new Accent(0xFF4D7CF6, 0xFF86A9FF, 0xFF3560D9));
        ACCENTS.put("cyan", new Accent(0xFF2BB3C0, 0xFF74D9E2, 0xFF1E8E99));
        ACCENTS.put("pink", new Accent(0xFFE05CA8, 0xFFF08CC8, 0xFFC0458C));
        ACCENTS.put("orange", new Accent(0xFFE08A3C, 0xFFF5B96E, 0xFFC26E22));
        ACCENTS.put("green", new Accent(0xFF4CAF7D, 0xFF8AD9B4, 0xFF389062));
        ACCENT_LABELS.put("purple", "典雅紫");
        ACCENT_LABELS.put("blue", "深海蓝");
        ACCENT_LABELS.put("cyan", "湖水青");
        ACCENT_LABELS.put("pink", "绯红玫粉");
        ACCENT_LABELS.put("orange", "暖阳橙");
        ACCENT_LABELS.put("green", "松石绿");

        TEXT_COLORS.put("white", 0xFFFFFFFF);
        TEXT_COLORS.put("silver", 0xFFCED4DA);
        TEXT_COLORS.put("red", 0xFFFF5C5C);
        TEXT_COLORS.put("purered", 0xFFFF0000);
        TEXT_COLORS.put("crimson", 0xFFE03131);
        TEXT_COLORS.put("rose", 0xFFF06595);
        TEXT_COLORS.put("orange", 0xFFFF9F43);
        TEXT_COLORS.put("tangerine", 0xFFF76707);
        TEXT_COLORS.put("yellow", 0xFFFCC419);
        TEXT_COLORS.put("pureyellow", 0xFFFFFF00);
        TEXT_COLORS.put("lemon", 0xFFFFE066);
        TEXT_COLORS.put("lime", 0xFFA9E34B);
        TEXT_COLORS.put("green", 0xFF51CF66);
        TEXT_COLORS.put("puregreen", 0xFF00FF00);
        TEXT_COLORS.put("forest", 0xFF2F9E44);
        TEXT_COLORS.put("cyan", 0xFF22B8CF);
        TEXT_COLORS.put("teal", 0xFF0CA678);
        TEXT_COLORS.put("sky", 0xFF339AF0);
        TEXT_COLORS.put("azure", 0xFF1C7ED6);
        TEXT_COLORS.put("pureblue", 0xFF0000FF);
        TEXT_COLORS.put("indigo", 0xFF5C7CFA);
        TEXT_COLORS.put("purple", 0xFF9775FA);
        TEXT_COLORS.put("violet", 0xFF7048E8);
        TEXT_COLORS.put("magenta", 0xFFD6409F);
        TEXT_COLORS.put("pink", 0xFFF783AC);
        TEXT_COLORS.put("salmon", 0xFFFF8787);
        TEXT_COLORS.put("brown", 0xFFA9745B);
        TEXT_COLORS.put("wine", 0xFF9A3B3B);
        LABELS.put("white", "纯白");
        LABELS.put("silver", "银灰");
        LABELS.put("red", "正红");
        LABELS.put("purered", "纯红");
        LABELS.put("crimson", "绯红");
        LABELS.put("rose", "玫红");
        LABELS.put("orange", "橙色");
        LABELS.put("tangerine", "橘红");
        LABELS.put("yellow", "金黄");
        LABELS.put("pureyellow", "纯黄");
        LABELS.put("lemon", "柠黄");
        LABELS.put("lime", "青柠");
        LABELS.put("green", "草绿");
        LABELS.put("puregreen", "纯绿");
        LABELS.put("forest", "深绿");
        LABELS.put("cyan", "青色");
        LABELS.put("teal", "湖绿");
        LABELS.put("sky", "天蓝");
        LABELS.put("azure", "湛蓝");
        LABELS.put("pureblue", "纯蓝");
        LABELS.put("indigo", "靛蓝");
        LABELS.put("purple", "紫色");
        LABELS.put("violet", "深紫");
        LABELS.put("magenta", "品红");
        LABELS.put("pink", "桃粉");
        LABELS.put("salmon", "珊瑚");
        LABELS.put("brown", "棕色");
        LABELS.put("wine", "酒红");

        BG_COLORS.put("default", 0xFF000000);
        BG_COLORS.put("graphite", 0xFF16181D);
        BG_COLORS.put("slate", 0xFF1E2128);
        BG_COLORS.put("charcoal", 0xFF222222);
        BG_COLORS.put("navy", 0xFF0D1B2A);
        BG_COLORS.put("steel", 0xFF1B2735);
        BG_COLORS.put("ocean", 0xFF12263F);
        BG_COLORS.put("forest", 0xFF0E1F16);
        BG_COLORS.put("moss", 0xFF18241A);
        BG_COLORS.put("olive", 0xFF1C2314);
        BG_COLORS.put("tealbg", 0xFF0C2222);
        BG_COLORS.put("coffee", 0xFF241A12);
        BG_COLORS.put("wine", 0xFF2A0E14);
        BG_COLORS.put("aubergine", 0xFF241019);
        BG_COLORS.put("plum", 0xFF1E1030);
        BG_COLORS.put("indigo", 0xFF141433);
        LABELS.put("default", "默认纯黑");
        LABELS.put("graphite", "石墨灰");
        LABELS.put("slate", "岩板灰");
        LABELS.put("charcoal", "炭灰");
        LABELS.put("navy", "深海军蓝");
        LABELS.put("steel", "钢蓝");
        LABELS.put("ocean", "深海蓝");
        LABELS.put("forest", "墨绿");
        LABELS.put("moss", "苔绿");
        LABELS.put("olive", "橄榄绿");
        LABELS.put("tealbg", "墨青");
        LABELS.put("coffee", "咖啡棕");
        LABELS.put("aubergine", "茄紫");
        LABELS.put("plum", "深梅紫");

        BUTTON_STYLES.put("square", "方角");
        BUTTON_STYLES.put("rounded", "圆角");
        BUTTON_STYLES.put("capsule", "胶囊");
    }

    /** 由文字色板生成强调色：light=提亮 1.25 倍，deep=加深 0.78 倍 */
    private static Accent accentFrom(int base) {
        return new Accent(base, lighten(base, 1.25f), darken(base, 0.78f));
    }

    private static int darken(int c, float f) {
        return Color.rgb((int) (Color.red(c) * f), (int) (Color.green(c) * f), (int) (Color.blue(c) * f));
    }

    private static int lighten(int c, float f) {
        return Color.rgb(Math.min(255, (int) (Color.red(c) * f)), Math.min(255, (int) (Color.green(c) * f)), Math.min(255, (int) (Color.blue(c) * f)));
    }

    private static int mixWhite(int c, float f) {
        return Color.rgb((int) (Color.red(c) + (255 - Color.red(c)) * f), (int) (Color.green(c) + (255 - Color.green(c)) * f), (int) (Color.blue(c) + (255 - Color.blue(c)) * f));
    }

    public static String getAccentKey() {
        String key = Prefers.getString("theme_accent", "purple");
        return ACCENTS.containsKey(key) || TEXT_COLORS.containsKey(key) ? key : "purple";
    }

    public static String getTextKey() {
        String key = Prefers.getString("theme_text", "white");
        return TEXT_COLORS.containsKey(key) ? key : "white";
    }

    public static String getBgKey() {
        String key = Prefers.getString("theme_bg", "default");
        return BG_COLORS.containsKey(key) ? key : "default";
    }

    public static void putAccent(String key) {
        Prefers.put("theme_accent", key);
    }

    public static void putText(String key) {
        Prefers.put("theme_text", key);
    }

    public static void putBg(String key) {
        Prefers.put("theme_bg", key);
    }

    public static String getButtonKey() {
        String key = Prefers.getString("theme_button", "capsule");
        return BUTTON_STYLES.containsKey(key) ? key : "capsule";
    }

    public static void putButton(String key) {
        Prefers.put("theme_button", key);
    }

    /** 当前强调色方案：手调 6 档优先，其余由文字色板生成 */
    public static Accent getAccent() {
        String key = getAccentKey();
        Accent tuned = ACCENTS.get(key);
        return tuned != null ? tuned : accentFrom(TEXT_COLORS.containsKey(key) ? TEXT_COLORS.get(key) : 0xFF9D5CE0);
    }

    public static int getTextColor() {
        Integer c = TEXT_COLORS.get(getTextKey());
        return c != null ? c : Color.WHITE;
    }

    public static int getBgColor() {
        Integer c = BG_COLORS.get(getBgKey());
        return c != null ? c : 0xFF000000;
    }

    public static String accentLabel(String key) {
        String own = ACCENT_LABELS.get(key);
        return own != null ? own : LABELS.getOrDefault(key, key);
    }

    public static String textLabel(String key) {
        return LABELS.getOrDefault(key, key);
    }

    public static String bgLabel(String key) {
        return LABELS.getOrDefault(key, key);
    }

    /** 注册进主题替换的胶囊样式资源：与 buildPill 的变体一一对应 */
    private static final int[] PILL_DRAWABLES = {
            R.drawable.shape_item, R.drawable.shape_accent, R.drawable.shape_item_glass,
            R.drawable.shape_episode_capsule, R.drawable.shape_search_bar_bg,
            R.drawable.shape_control, R.drawable.shape_action_btn_bg
    };

    /** 可选强调色：手调 6 档优先，其余由文字色板生成（供色板弹窗使用） */
    public static Map<String, Integer> accentColors() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (Map.Entry<String, Accent> e : ACCENTS.entrySet()) map.put(e.getKey(), e.getValue().base);
        for (Map.Entry<String, Integer> e : TEXT_COLORS.entrySet()) if (!map.containsKey(e.getKey())) map.put(e.getKey(), e.getValue());
        return map;
    }

    /** 原始资源 ConstantState → 资源 ID，首次应用时构建 */
    private static Map<Object, Integer> ORIG_STATES;

    private static int sLastText = Color.WHITE;
    private static int sLastTint = Color.WHITE;

    /** 应用全局主题：遍历界面替换胶囊背景与纯白文字/图标（Activity 启动与设置变更时调用） */
    public static void apply(View root) {
        Context ctx = root.getContext();
        Accent a = getAccent();
        int text = getTextColor();
        root.setBackgroundColor(getBgColor());
        applyWall(root, !"default".equals(getBgKey()));
        if (ORIG_STATES == null) {
            ORIG_STATES = new LinkedHashMap<>();
            for (int res : PILL_DRAWABLES) {
                Drawable orig = ContextCompat.getDrawable(ctx, res);
                if (orig != null && orig.getConstantState() != null) ORIG_STATES.put(orig.getConstantState(), res);
            }
        }
        applyRecursive(root, a, text, ctx);
        sLastText = text;
        sLastTint = text;
    }

    /** 选了背景色时收起自带壁纸，选回默认纯黑时恢复 */
    private static void applyWall(View root, boolean showWall) {
        if (!(root instanceof ViewGroup)) return;
        ViewGroup content = root.findViewById(android.R.id.content);
        if (content == null) return;
        for (int i = 0; i < content.getChildCount(); i++) {
            View child = content.getChildAt(i);
            if (child instanceof CustomWallView) child.setVisibility(showWall ? View.VISIBLE : View.GONE);
        }
    }

    /** 界面元素从 XML 创建时调用：出生即套用主题（按钮胶囊/页面底色/文字颜色） */
    public static void onViewCreated(View view, Context ctx, AttributeSet attrs) {
        if (attrs == null) return;
        TypedArray bg = ctx.obtainStyledAttributes(attrs, new int[]{android.R.attr.background});
        if (bg.hasValue(0)) {
            int res = bg.getResourceId(0, 0);
            bg.recycle();
            applyBackgroundById(view, ctx, res);
        } else {
            bg.recycle();
        }
        TypedArray tc = ctx.obtainStyledAttributes(attrs, new int[]{android.R.attr.textColor});
        if (tc.hasValue(0)) {
            android.content.res.ColorStateList csl = tc.getColorStateList(0);
            tc.recycle();
            if (csl != null && csl.getDefaultColor() == Color.WHITE && view instanceof TextView) ((TextView) view).setTextColor(getTextColor());
        } else {
            tc.recycle();
        }
        if (view instanceof MaterialButton) applyMaterialButton((MaterialButton) view, ctx);
    }

    /** MaterialButton 胶囊（背景走 backgroundTint 颜色选择器，无 android:background）：按强调色覆盖选中/未选中态与圆角 */
    private static void applyMaterialButton(MaterialButton btn, Context ctx) {
        Accent a = getAccent();
        int selTint = 0x1A000000 | (a.base & 0xFFFFFF);
        btn.setBackgroundTintList(new android.content.res.ColorStateList(new int[][]{{android.R.attr.state_selected}, {}}, new int[]{selTint, Color.TRANSPARENT}));
        btn.setStrokeColor(new android.content.res.ColorStateList(new int[][]{{android.R.attr.state_selected}, {}}, new int[]{0xFF000000 | (a.base & 0xFFFFFF), outlineColor(ctx)}));
        btn.setTextColor(new android.content.res.ColorStateList(new int[][]{{android.R.attr.state_selected}, {}}, new int[]{0xFF000000 | (a.light & 0xFFFFFF), getTextColor()}));
        btn.setCornerRadius(shapeRadius(ctx, btn.getCornerRadius()));
    }

    /** 未选中态描边：取主题 colorOutline，取不到回落中灰 */
    private static int outlineColor(Context ctx) {
        TypedArray ta = ctx.obtainStyledAttributes(new int[]{com.google.android.material.R.attr.colorOutline});
        int color = ta.getColor(0, 0xFF8A8A8A);
        ta.recycle();
        return color;
    }

    /** 按资源 ID 套背景：胶囊换强调色样式，页面底换背景色 */
    private static void applyBackgroundById(View view, Context ctx, int res) {
        if (res == 0) return;
        if (res == R.drawable.bg_page) {
            view.setTag(R.id.theme_pill_res, res);
            view.setBackground("default".equals(getBgKey()) ? ContextCompat.getDrawable(ctx, res) : new ColorDrawable(getBgColor()));
            return;
        }
        for (int pill : PILL_DRAWABLES) {
            if (pill == res) {
                view.setBackground(pillFor(ctx, res, getAccent()));
                view.setTag(R.id.theme_pill_res, res);
                return;
            }
        }
    }

    private static void applyRecursive(View view, Accent a, int text, Context ctx) {
        Integer res = null;
        Object tagged = view.getTag(R.id.theme_pill_res);
        if (tagged instanceof Integer) {
            res = (Integer) tagged;
        } else {
            Drawable bg = view.getBackground();
            if (bg != null && bg.getConstantState() != null) res = ORIG_STATES.get(bg.getConstantState());
        }
        if (res != null) {
            if (res == R.drawable.bg_page) {
                view.setBackground("default".equals(getBgKey()) ? ContextCompat.getDrawable(ctx, res) : new ColorDrawable(getBgColor()));
            } else {
                view.setBackground(pillFor(ctx, res, a));
            }
            view.setTag(R.id.theme_pill_res, res);
        }
        if (view instanceof MaterialButton) applyMaterialButton((MaterialButton) view, ctx);
        if (view instanceof TextView) {
            TextView tv = (TextView) view;
            int cur = tv.getCurrentTextColor();
            if (cur == Color.WHITE || cur == sLastText) tv.setTextColor(text);
        }
        if (view instanceof ImageView) {
            ImageView iv = (ImageView) view;
            if (iv.getImageTintList() != null) {
                int cur = iv.getImageTintList().getDefaultColor();
                if (cur == Color.WHITE || cur == sLastTint) iv.setImageTintList(android.content.res.ColorStateList.valueOf(text));
            }
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) applyRecursive(group.getChildAt(i), a, text, ctx);
        }
    }

    /** 按钮样式对应的圆角：方角 6dp，圆角 14dp，胶囊保持原值 */
    private static float shapeRadius(Context ctx, float base) {
        String style = getButtonKey();
        if ("square".equals(style)) return dp(ctx, 6);
        if ("rounded".equals(style)) return dp(ctx, 14);
        return base;
    }

    /** 按资源名重建对应结构的强调色胶囊（保持原圆角/形状/描边/高光） */
    private static Drawable pillFor(Context ctx, int res, Accent a) {
        if (res == R.drawable.shape_item || res == R.drawable.shape_item_glass) return pill(ctx, a, shapeRadius(ctx, dp(ctx, 28)), true, true, true);
        if (res == R.drawable.shape_accent) return pill(ctx, a, shapeRadius(ctx, dp(ctx, 28)), false, false, true);
        if (res == R.drawable.shape_episode_capsule) return pill(ctx, a, shapeRadius(ctx, dp(ctx, 20)), true, true, false);
        if (res == R.drawable.shape_search_bar_bg) return pill(ctx, a, shapeRadius(ctx, dp(ctx, 18)), false, false, false);
        if (res == R.drawable.shape_control || res == R.drawable.shape_action_btn_bg) return pillOval(ctx, a, res == R.drawable.shape_control);
        return pill(ctx, a, shapeRadius(ctx, dp(ctx, 28)), false, false, false);
    }

    private static float dp(Context ctx, int v) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, ctx.getResources().getDisplayMetrics());
    }

    /** 圆角矩形胶囊：selector=带选中描边态，stroke=选中描边，gloss=顶部高光 */
    private static Drawable pill(Context ctx, Accent a, float radius, boolean selector, boolean stroke, boolean gloss) {
        GradientDrawable normal = rect(radius);
        normal.setColor(0x33000000 | (a.base & 0xFFFFFF));
        GradientDrawable normalGrad = rect(radius);
        normalGrad.setColors(new int[]{0xE6000000 | (a.light & 0xFFFFFF), 0xE6000000 | (a.deep & 0xFFFFFF)});
        normalGrad.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
        LayerDrawable normalLayers = gloss ? new LayerDrawable(new Drawable[]{normal, normalGrad, glossTop(radius)}) : new LayerDrawable(new Drawable[]{normal, normalGrad});
        GradientDrawable selectedSolid = rect(radius);
        selectedSolid.setColor(0x66000000 | (a.base & 0xFFFFFF));
        GradientDrawable selectedGrad = rect(radius);
        selectedGrad.setColors(new int[]{0xE6000000 | (a.light & 0xFFFFFF), 0xE6000000 | (a.deep & 0xFFFFFF)});
        selectedGrad.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
        Drawable selected;
        if (stroke) {
            GradientDrawable selectedStroke = rect(radius);
            selectedStroke.setStroke((int) dp(ctx, 2), 0xE6000000 | (a.light & 0xFFFFFF));
            selected = new LayerDrawable(new Drawable[]{selectedSolid, selectedGrad, selectedStroke});
        } else {
            selected = new LayerDrawable(new Drawable[]{selectedSolid, selectedGrad});
        }
        RippleDrawable normalRipple = new RippleDrawable(android.content.res.ColorStateList.valueOf(0x40000000 | (a.base & 0xFFFFFF)), normalLayers, rect(radius));
        if (!selector) return normalRipple;
        RippleDrawable selectedRipple = new RippleDrawable(android.content.res.ColorStateList.valueOf(0x40000000 | (a.base & 0xFFFFFF)), selected, rect(radius));
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_selected}, selectedRipple);
        states.addState(new int[0], normalRipple);
        return states;
    }

    /** 椭圆胶囊（播放器控制按钮等） */
    private static Drawable pillOval(Context ctx, Accent a, boolean gloss) {
        GradientDrawable solid = oval();
        solid.setColor(0x33000000 | (a.base & 0xFFFFFF));
        GradientDrawable grad = oval();
        grad.setColors(new int[]{0xE6000000 | (a.light & 0xFFFFFF), 0xE6000000 | (a.deep & 0xFFFFFF)});
        grad.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
        LayerDrawable layers = gloss ? new LayerDrawable(new Drawable[]{solid, grad, glossOval()}) : new LayerDrawable(new Drawable[]{solid, grad});
        return new RippleDrawable(android.content.res.ColorStateList.valueOf(0x33000000 | (a.base & 0xFFFFFF)), layers, oval());
    }

    private static GradientDrawable rect(float radius) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.RECTANGLE);
        d.setCornerRadius(radius);
        return d;
    }

    private static GradientDrawable oval() {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        return d;
    }

    /** 顶部高光：白 35% 渐隐，叠在渐变上模拟玻璃质感 */
    private static Drawable glossTop(float radius) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.RECTANGLE);
        d.setCornerRadii(new float[]{radius, radius, radius, radius, 0, 0, 0, 0});
        d.setColors(new int[]{0x59FFFFFF, 0x00FFFFFF});
        d.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
        return d;
    }

    private static Drawable glossOval() {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColors(new int[]{0x59FFFFFF, 0x00FFFFFF});
        d.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
        return d;
    }
}
