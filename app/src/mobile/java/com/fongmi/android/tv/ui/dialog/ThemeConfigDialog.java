package com.fongmi.android.tv.ui.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.fongmi.android.tv.utils.ThemeColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 全局主题配置弹窗：强调色 / 全局文字颜色 / 全局背景颜色 / 按钮样式 四块。
 * 与繁花TV 交互一致：色点选择器，点选即时生效并持久化。
 */
public class ThemeConfigDialog {

    public static void show(FragmentActivity activity) {
        Context ctx = activity;
        int pad = dp(ctx, 24);
        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad / 2, pad, pad);

        root.addView(section(ctx, "主题强调色", ThemeColors.accentColors(), ThemeColors.getAccentKey(), key -> {
            ThemeColors.putAccent(key);
            ThemeColors.apply(activity.getWindow().getDecorView());
        }));
        root.addView(section(ctx, "全局文字颜色", ThemeColors.TEXT_COLORS, ThemeColors.getTextKey(), key -> {
            ThemeColors.putText(key);
            ThemeColors.apply(activity.getWindow().getDecorView());
        }));
        root.addView(section(ctx, "全局背景颜色", ThemeColors.BG_COLORS, ThemeColors.getBgKey(), key -> {
            ThemeColors.putBg(key);
            ThemeColors.apply(activity.getWindow().getDecorView());
        }));
        root.addView(buttonSection(ctx, ThemeColors.getButtonKey(), key -> {
            ThemeColors.putButton(key);
            ThemeColors.apply(activity.getWindow().getDecorView());
        }));

        ScrollView scroll = new ScrollView(ctx);
        scroll.addView(root);
        new MaterialAlertDialogBuilder(activity).setTitle("全局主题配置").setView(scroll).show();
    }

    private interface SelectListener {
        void onSelect(String key);
    }

    private static LinearLayout section(Context ctx, String title, Map<String, Integer> colors, String selected, SelectListener listener) {
        LinearLayout box = new LinearLayout(ctx);
        box.setOrientation(LinearLayout.VERTICAL);

        TextView label = new TextView(ctx);
        label.setText(title + " · " + labelOf(title, selected));
        label.setTextColor(Color.WHITE);
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        label.setPadding(0, dp(ctx, 12), 0, dp(ctx, 8));
        box.addView(label);

        List<TextView> dots = new ArrayList<>();
        List<List<String>> rows = chunked(new ArrayList<>(colors.keySet()), 8);
        for (List<String> rowKeys : rows) {
            LinearLayout row = new LinearLayout(ctx);
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (String key : rowKeys) {
                TextView dot = dot(ctx, colors.get(key), key.equals(selected));
                dot.setTag(key);
                dot.setOnClickListener(v -> {
                    listener.onSelect(key);
                    label.setText(title + " · " + labelOf(title, key));
                    for (TextView other : dots) {
                        String otherKey = (String) other.getTag();
                        other.setBackground(dotBg(ctx, colors.get(otherKey), otherKey.equals(key)));
                        other.setText(otherKey.equals(key) ? "✓" : "");
                    }
                });
                dots.add(dot);
                row.addView(dot);
            }
            box.addView(row);
        }
        return box;
    }

    private static TextView dot(Context ctx, int color, boolean selected) {
        TextView dot = new TextView(ctx);
        dot.setText(selected ? "✓" : "");
        dot.setTextColor(Color.WHITE);
        dot.setGravity(Gravity.CENTER);
        dot.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(ctx, 40), dp(ctx, 40));
        lp.setMargins(dp(ctx, 4), dp(ctx, 4), dp(ctx, 4), dp(ctx, 4));
        dot.setLayoutParams(lp);
        dot.setBackground(dotBg(ctx, color, selected));
        return dot;
    }

    private static GradientDrawable dotBg(Context ctx, int color, boolean selected) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColor(color);
        if (selected) d.setStroke(dp(ctx, 2), Color.WHITE);
        return d;
    }

    private static LinearLayout buttonSection(Context ctx, String selected, SelectListener listener) {
        LinearLayout box = new LinearLayout(ctx);
        box.setOrientation(LinearLayout.VERTICAL);

        TextView label = new TextView(ctx);
        label.setText("按钮样式 · " + ThemeColors.BUTTON_STYLES.get(selected));
        label.setTextColor(Color.WHITE);
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        label.setPadding(0, dp(ctx, 12), 0, dp(ctx, 8));
        box.addView(label);

        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        List<TextView> chips = new ArrayList<>();
        for (Map.Entry<String, String> e : ThemeColors.BUTTON_STYLES.entrySet()) {
            TextView chip = new TextView(ctx);
            chip.setText(e.getValue());
            chip.setTextColor(Color.WHITE);
            chip.setGravity(Gravity.CENTER);
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            chip.setPadding(dp(ctx, 20), dp(ctx, 8), dp(ctx, 20), dp(ctx, 8));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, dp(ctx, 4), dp(ctx, 12), dp(ctx, 4));
            chip.setLayoutParams(lp);
            chip.setBackground(chipBg(ctx, e.getKey().equals(selected)));
            chip.setTag(e.getKey());
            chip.setOnClickListener(v -> {
                listener.onSelect(e.getKey());
                label.setText("按钮样式 · " + ThemeColors.BUTTON_STYLES.get(e.getKey()));
                for (TextView other : chips) other.setBackground(chipBg(ctx, e.getKey().equals(other.getTag())));
            });
            chips.add(chip);
            row.addView(chip);
        }
        box.addView(row);
        return box;
    }

    private static GradientDrawable chipBg(Context ctx, boolean selected) {
        GradientDrawable d = new GradientDrawable();
        d.setCornerRadius(dp(ctx, 20));
        d.setColor(selected ? 0x66FFFFFF : 0x33FFFFFF);
        if (selected) d.setStroke(dp(ctx, 2), Color.WHITE);
        return d;
    }

    private static String labelOf(String title, String key) {
        if (title.contains("强调色")) return ThemeColors.accentLabel(key);
        if (title.contains("文字")) return ThemeColors.textLabel(key);
        return ThemeColors.bgLabel(key);
    }

    private static List<List<String>> chunked(List<String> list, int size) {
        List<List<String>> out = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) out.add(list.subList(i, Math.min(list.size(), i + size)));
        return out;
    }

    private static int dp(Context ctx, int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, ctx.getResources().getDisplayMetrics());
    }
}
