package com.fongmi.android.tv.ui.dialog;

import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.DialogThemeBinding;
import com.fongmi.android.tv.setting.Setting;
import com.fongmi.android.tv.ui.adapter.ThemeAdapter;
import com.fongmi.android.tv.utils.Theme;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ThemeDialog extends BaseAlertDialog implements ThemeAdapter.OnClickListener {

    /** 与全局主题共用同一套预设色板，保证每个取色器颜色一致且齐全 */
    private static final int[] COLORS = Theme.PRESETS;
    private DialogThemeBinding binding;

    public static void show(Fragment fragment) {
        new ThemeDialog().show(fragment.getChildFragmentManager(), null);
    }

    @Override
    protected ViewBinding getBinding() {
        return binding = DialogThemeBinding.inflate(getLayoutInflater());
    }

    @Override
    protected MaterialAlertDialogBuilder getBuilder() {
        return builder().setTitle(R.string.setting_theme_color).setView(getBinding().getRoot());
    }

    @Override
    protected void initView() {
        binding.recycler.setAdapter(new ThemeAdapter(this, COLORS, Setting.getThemeColor()));
    }

    @Override
    public void onItemClick(int color) {
        ((Listener) requireParentFragment()).setTheme(color);
        dismiss();
    }

    public interface Listener {

        void setTheme(int color);
    }
}

