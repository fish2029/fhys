package com.fongmi.android.tv.ui.dialog;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.SeekBar;

import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.DialogThemeStyleBinding;
import com.fongmi.android.tv.utils.ResUtil;
import com.fongmi.android.tv.utils.Theme;
import com.fongmi.android.tv.utils.ThemeManager;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * 全局主题配置弹窗：文字颜色 / 按钮颜色 / 点击态颜色 / 圆角。
 * 每次改动即时写入本地并实时套用到当前界面。
 */
public class ThemeStyleDialog extends BaseAlertDialog {

    private static final int TYPE_TEXT = 0;
    private static final int TYPE_BUTTON = 1;
    private static final int TYPE_PRESSED = 2;

    private DialogThemeStyleBinding binding;
    private boolean enabled;
    private int textColor;
    private int buttonColor;
    private int pressedColor;
    private int radius;

    public static void show(Fragment fragment) {
        new ThemeStyleDialog().show(fragment.getChildFragmentManager(), null);
    }

    @Override
    protected ViewBinding getBinding() {
        return binding = DialogThemeStyleBinding.inflate(getLayoutInflater());
    }

    @Override
    protected MaterialAlertDialogBuilder getBuilder() {
        return builder().setTitle(R.string.setting_global_theme).setView(getBinding().getRoot());
    }

    @Override
    protected void initView() {
        Context context = requireContext();
        enabled = Theme.isEnabled(context);
        textColor = Theme.getTextColor(context);
        buttonColor = Theme.getButtonColor(context);
        pressedColor = Theme.getPressedColor(context);
        radius = Theme.getRadiusDp(context);

        binding.enable.setChecked(enabled);
        binding.enable.setOnCheckedChangeListener((view, value) -> {
            enabled = value;
            save();
        });

        buildPalette(binding.textColors, TYPE_TEXT);
        buildPalette(binding.buttonColors, TYPE_BUTTON);
        buildPalette(binding.pressedColors, TYPE_PRESSED);

        binding.radius.setProgress(radius);
        binding.radiusValue.setText(String.valueOf(radius));
        binding.radius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radius = progress;
                binding.radiusValue.setText(String.valueOf(progress));
                save();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        binding.reset.setOnClickListener(view -> onReset());
        binding.done.setOnClickListener(view -> dismiss());
    }

    /** 色板：用预设色动态生成圆形色块，选中的加白圈 */
    private void buildPalette(FlexboxLayout container, int type) {
        container.removeAllViews();
        int size = ResUtil.dp2px(32);
        int margin = ResUtil.dp2px(6);
        for (int preset : Theme.PRESETS) {
            View item = new View(requireContext());
            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(size, size);
            params.setMargins(margin, margin, margin, margin);
            item.setLayoutParams(params);
            item.setTag(preset);
            item.setBackground(swatch(preset, preset == getColor(type)));
            item.setOnClickListener(view -> {
                setColor(type, (Integer) view.getTag());
                buildPalette(container, type);
                save();
            });
            container.addView(item);
        }
    }

    private Drawable swatch(int color, boolean selected) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(color);
        drawable.setStroke(ResUtil.dp2px(selected ? 3 : 1), selected ? 0xFFFFFFFF : 0x55FFFFFF);
        return drawable;
    }

    private int getColor(int type) {
        if (type == TYPE_TEXT) return textColor;
        if (type == TYPE_BUTTON) return buttonColor;
        return pressedColor;
    }

    private void setColor(int type, int color) {
        if (type == TYPE_TEXT) textColor = color;
        else if (type == TYPE_BUTTON) buttonColor = color;
        else pressedColor = color;
    }

    /** 持久化并实时套用 */
    private void save() {
        Context context = requireContext();
        ThemeManager.save(context, enabled, textColor, buttonColor, pressedColor, radius);
        if (!enabled) return;
        Theme.apply(binding.getRoot());
        ThemeManager.apply(requireActivity());
    }

    private void onReset() {
        ThemeManager.reset(requireContext());
        enabled = false;
        textColor = Theme.DEFAULT_TEXT;
        buttonColor = Theme.DEFAULT_BUTTON;
        pressedColor = Theme.DEFAULT_PRESSED;
        radius = Theme.DEFAULT_RADIUS;
        binding.enable.setChecked(false);
        binding.radius.setProgress(radius);
        binding.radiusValue.setText(String.valueOf(radius));
        buildPalette(binding.textColors, TYPE_TEXT);
        buildPalette(binding.buttonColors, TYPE_BUTTON);
        buildPalette(binding.pressedColors, TYPE_PRESSED);
    }
}
