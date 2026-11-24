package com.fongmi.android.tv.ui.dialog;

import android.os.Build;
import android.view.View;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Site;
import com.fongmi.android.tv.databinding.AdapterCategoryBinding;
import com.fongmi.android.tv.databinding.AdapterSiteBinding;
import com.fongmi.android.tv.databinding.DialogSiteBinding;
import com.fongmi.android.tv.impl.SiteListener;
import com.fongmi.android.tv.setting.Setting;
import com.fongmi.android.tv.utils.ResUtil;
import com.fongmi.android.tv.utils.SiteCategory;
import com.fongmi.android.tv.utils.Theme;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 手机版站源界面：
 * 第一排是动态生成的分类按钮（名称来自接口返回的站源，不写死），
 * 下面用垂直 LinearLayout 依次排列该分类下的站点。
 */
public class SiteDialog extends BaseAlertDialog {

    private static final float MAX_HEIGHT_RATIO = 0.55f;
    private static final int MARGIN_DP = 4;

    private DialogSiteBinding binding;
    private SiteListener listener;
    private boolean search;
    private boolean change;
    private final List<Site> sites = new ArrayList<>();
    private final List<Site> shown = new ArrayList<>();
    private int category = -1;

    public static SiteDialog create() {
        return new SiteDialog();
    }

    public SiteDialog search() {
        search = true;
        return this;
    }

    public SiteDialog change() {
        change = true;
        return this;
    }

    public void show(Fragment fragment) {
        show(fragment.getChildFragmentManager(), null);
        if (fragment instanceof SiteListener) listener = (SiteListener) fragment;
    }

    @Override
    protected ViewBinding getBinding() {
        return binding = DialogSiteBinding.inflate(getLayoutInflater());
    }

    @Override
    protected MaterialAlertDialogBuilder getBuilder() {
        return builder().setView(getBinding().getRoot());
    }

    @Override
    protected void initView() {
        // 站源弹窗保持上一版样式，不参与全局主题刷色
        Theme.skip(binding.getRoot());
        collectSites();
        if (Setting.isSmartCategory()) {
            binding.categoryContainer.setVisibility(View.VISIBLE);
            buildCategories();
            Integer def = getDefaultCategory();
            selectCategory(def == null ? -1 : def);
        } else {
            binding.categoryContainer.setVisibility(View.GONE);
            showSites(sites);
        }
    }

    /** 只取未隐藏的站内站点 */
    private void collectSites() {
        sites.clear();
        for (Site site : VodConfig.get().getSites()) if (!site.isHide()) sites.add(site);
    }

    /** 第一排分类按钮：运行时按实际存在的分类动态添加 */
    private void buildCategories() {
        binding.categoryContainer.removeAllViews();
        for (int item : SiteCategory.categories(sites)) {
            AdapterCategoryBinding b = AdapterCategoryBinding.inflate(getLayoutInflater(), binding.categoryContainer, false);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            int m = dp(MARGIN_DP);
            p.setMargins(m, 0, m, 0);
            b.getRoot().setLayoutParams(p);
            b.text.setText(SiteCategory.getLabel(item));
            b.text.setTag(item);
            b.text.setSelected(item == category);
            b.text.setOnClickListener(v -> selectCategory((Integer) v.getTag()));
            binding.categoryContainer.addView(b.getRoot());
        }
    }

    /** 默认选中：当前站在的分类，否则第一个分类 */
    private Integer getDefaultCategory() {
        List<Integer> categories = SiteCategory.categories(sites);
        if (categories.isEmpty()) return null;
        for (Site site : sites) {
            if (!site.isSelected()) continue;
            int item = SiteCategory.of(site);
            if (categories.contains(item)) return item;
            break;
        }
        return categories.get(0);
    }

    /** 点击分类：高亮该按钮，并在下方容器里加载对应站点 */
    private void selectCategory(int target) {
        category = target;
        for (int i = 0; i < binding.categoryContainer.getChildCount(); i++) {
            View child = binding.categoryContainer.getChildAt(i);
            Object tag = child.getTag();
            child.setSelected(tag != null && (Integer) tag == target);
        }
        showSites(target == -1 ? new ArrayList<>() : SiteCategory.find(sites, target));
    }

    /** 下方垂直容器：一排一排往下排站点 */
    private void showSites(List<Site> list) {
        shown.clear();
        shown.addAll(list);
        binding.siteContainer.removeAllViews();
        for (Site site : list) addSite(site);
    }

    private void addSite(Site site) {
        AdapterSiteBinding b = AdapterSiteBinding.inflate(getLayoutInflater(), binding.siteContainer, false);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int m = dp(MARGIN_DP);
        p.setMargins(m, m, m, m);
        b.getRoot().setLayoutParams(p);
        boolean on = !search || change;
        b.text.setText(site.getName());
        b.text.setEnabled(on);
        b.text.setFocusable(on);
        b.text.setSelected(on && site.isSelected());
        b.search.setImageResource(getSearchIcon(site));
        b.change.setImageResource(getChangeIcon(site));
        b.search.setVisibility(search ? View.VISIBLE : View.GONE);
        b.change.setVisibility(change ? View.VISIBLE : View.GONE);
        b.text.setOnClickListener(v -> onTextClick(site));
        b.search.setOnClickListener(v -> {
            site.setSearchable(!site.isSearchable()).save();
            b.search.setImageResource(getSearchIcon(site));
        });
        b.change.setOnClickListener(v -> {
            site.setChangeable(!site.isChangeable()).save();
            b.change.setImageResource(getChangeIcon(site));
        });
        b.search.setOnLongClickListener(v -> {
            toggleAll(true);
            return true;
        });
        b.change.setOnLongClickListener(v -> {
            toggleAll(false);
            return true;
        });
        binding.siteContainer.addView(b.getRoot());
    }

    private void onTextClick(Site item) {
        if (listener != null) listener.setSite(item);
        dismiss();
    }

    /** 长按图标：当前分类里的站点统一开/关 */
    private void toggleAll(boolean searchable) {
        boolean result = searchable ? !firstShown().isSearchable() : !firstShown().isChangeable();
        for (Site site : shown) {
            if (searchable) site.setSearchable(result);
            else site.setChangeable(result);
            site.save();
        }
        showSites(new ArrayList<>(shown));
    }

    private Site firstShown() {
        return shown.isEmpty() ? sites.get(0) : shown.get(0);
    }

    private int getSearchIcon(Site item) {
        return item.isSearchable() ? R.drawable.ic_site_search : R.drawable.ic_site_block;
    }

    private int getChangeIcon(Site item) {
        return item.isChangeable() ? R.drawable.ic_site_change : R.drawable.ic_site_block;
    }

    private int dp(int value) {
        return Math.round(value * requireContext().getResources().getDisplayMetrics().density);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                getDialog().getWindow().setBackgroundBlurRadius(80);
            }
        }
        int maxHeight = Math.round(requireContext().getResources().getDisplayMetrics().heightPixels * MAX_HEIGHT_RATIO);
        binding.siteScroll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, maxHeight));
        if (sites.isEmpty()) dismiss();
        else if (ResUtil.isLand(requireContext())) setWidth(0.5f);
    }
}
