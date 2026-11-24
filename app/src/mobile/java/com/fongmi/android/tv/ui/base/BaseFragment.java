package com.fongmi.android.tv.ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.utils.ThemeManager;

public abstract class BaseFragment extends Fragment {

    private boolean isViewCreated;
    private boolean isDataLoaded;

    protected abstract ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return getBinding(inflater, container).getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        isViewCreated = true;
        // 全局主题：新页面/新视图一律读同一份配置并即时套用
        ThemeManager.apply(view);
        ThemeManager.applyBackground(view);
        if (getUserVisibleHint()) {
            lazyLoad();
            isDataLoaded = true;
        }
    }

    private void lazyLoad() {
        initMenu();
        initView();
        initEvent();
    }

    protected void initMenu() {
    }

    protected void initView() {
    }

    protected void initEvent() {
    }

    public boolean canBack() {
        return true;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isViewCreated && !isDataLoaded) {
            lazyLoad();
            isDataLoaded = true;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isViewCreated = false;
        isDataLoaded = false;
    }
}
