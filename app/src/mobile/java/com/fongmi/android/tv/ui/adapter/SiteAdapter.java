package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Site;
import com.fongmi.android.tv.databinding.AdapterSiteBinding;
import com.fongmi.android.tv.databinding.AdapterSiteGroupBinding;
import com.fongmi.android.tv.setting.Setting;
import com.fongmi.android.tv.utils.Pinyin;
import com.fongmi.android.tv.utils.SiteCategory;

import java.util.ArrayList;
import java.util.List;

public class SiteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_SITE = 1;

    private final OnClickListener listener;
    private final List<Object> mItems;
    private boolean search;
    private boolean change;

    public SiteAdapter(OnClickListener listener) {
        this.listener = listener;
        this.mItems = new ArrayList<>();
        this.addAll();
    }

    public interface OnClickListener {

        void onTextClick(Site item);

        void onSearchClick(int position, Site item);

        void onChangeClick(int position, Site item);

        boolean onSearchLongClick(Site item);

        boolean onChangeLongClick(Site item);
    }

    public SiteAdapter search(boolean search) {
        this.search = search;
        return this;
    }

    public SiteAdapter change(boolean change) {
        this.change = change;
        return this;
    }

    private void addAll() {
        List<Site> sites = getVisibleSites();
        mItems.clear();
        if (Setting.isSmartCategory()) {
            mItems.addAll(SiteCategory.group(sites));
        } else {
            mItems.addAll(sites);
        }
    }

    private List<Site> getVisibleSites() {
        List<Site> sites = new ArrayList<>();
        for (Site site : VodConfig.get().getSites()) if (!site.isHide()) sites.add(site);
        return sites;
    }

    public void filter(String query) {
        List<Site> sites = new ArrayList<>();
        if (query.isEmpty()) {
            sites.addAll(getVisibleSites());
        } else {
            for (Site site : VodConfig.get().getSites()) {
                if (site.isHide()) continue;
                if (site.getName().toLowerCase().contains(query.toLowerCase())
                        || Pinyin.getInitials(site.getName()).contains(query.toLowerCase())) {
                    sites.add(site);
                }
            }
        }
        mItems.clear();
        if (Setting.isSmartCategory()) {
            mItems.addAll(SiteCategory.group(sites));
        } else {
            mItems.addAll(sites);
        }
        notifyDataSetChanged();
    }

    /** 当前列表里的全部站点（不含分类标题） */
    public List<Site> getItems() {
        List<Site> result = new ArrayList<>();
        for (Object item : mItems) if (item instanceof Site) result.add((Site) item);
        return result;
    }

    /** 当前选中站在列表中的位置（含分类标题偏移），没有则 -1 */
    public int getSelectedPosition() {
        for (int i = 0; i < mItems.size(); i++) {
            if (mItems.get(i) instanceof Site && ((Site) mItems.get(i)).isSelected()) return i;
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position) instanceof Site ? TYPE_SITE : TYPE_HEADER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            return new HeaderHolder(AdapterSiteGroupBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
        return new SiteHolder(AdapterSiteBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_HEADER) {
            ((HeaderHolder) holder).binding.text.setText(getCategoryName((Integer) mItems.get(position)));
            return;
        }
        Site item = (Site) mItems.get(position);
        SiteHolder siteHolder = (SiteHolder) holder;
        boolean on = !search || change;
        siteHolder.binding.text.setText(item.getName());
        siteHolder.binding.text.setEnabled(on);
        siteHolder.binding.text.setFocusable(on);
        siteHolder.binding.text.setSelected(on && item.isSelected());
        siteHolder.binding.search.setImageResource(getSearchIcon(item));
        siteHolder.binding.change.setImageResource(getChangeIcon(item));
        siteHolder.binding.search.setVisibility(search ? View.VISIBLE : View.GONE);
        siteHolder.binding.change.setVisibility(change ? View.VISIBLE : View.GONE);
        siteHolder.binding.text.setOnClickListener(v -> listener.onTextClick(item));
        siteHolder.binding.search.setOnClickListener(v -> listener.onSearchClick(position, item));
        siteHolder.binding.change.setOnClickListener(v -> listener.onChangeClick(position, item));
        siteHolder.binding.search.setOnLongClickListener(v -> listener.onSearchLongClick(item));
        siteHolder.binding.change.setOnLongClickListener(v -> listener.onChangeLongClick(item));
    }

    private int getCategoryName(int category) {
        switch (category) {
            case SiteCategory.PY:
                return R.string.site_category_py;
            case SiteCategory.MUSIC:
                return R.string.site_category_music;
            case SiteCategory.DRAMA:
                return R.string.site_category_drama;
            default:
                return R.string.site_category_jar;
        }
    }

    private int getSearchIcon(Site item) {
        return item.isSearchable() ? R.drawable.ic_site_search : R.drawable.ic_site_block;
    }

    private int getChangeIcon(Site item) {
        return item.isChangeable() ? R.drawable.ic_site_change : R.drawable.ic_site_block;
    }

    public class HeaderHolder extends RecyclerView.ViewHolder {

        private final AdapterSiteGroupBinding binding;

        HeaderHolder(@NonNull AdapterSiteGroupBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public class SiteHolder extends RecyclerView.ViewHolder {

        private final AdapterSiteBinding binding;

        SiteHolder(@NonNull AdapterSiteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
