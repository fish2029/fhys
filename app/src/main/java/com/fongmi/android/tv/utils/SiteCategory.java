package com.fongmi.android.tv.utils;

import com.fongmi.android.tv.bean.Site;

import java.util.ArrayList;
import java.util.List;

/**
 * 站源智能分类：按 key / 名称 / api / type 自动归类。
 * 分类固定为 JAR内站点、PY站点、音乐、短剧，输出时 JAR内站点强制排第一。
 */
public class SiteCategory {

    public static final int JAR = 0;
    public static final int PY = 1;
    public static final int MUSIC = 2;
    public static final int DRAMA = 3;

    private static final int[] ORDER = {JAR, PY, MUSIC, DRAMA};

    /** 单个站点的分类 */
    public static int of(Site site) {
        String name = site.getName().toLowerCase();
        String api = site.getApi().toLowerCase();
        String key = site.getKey().toLowerCase();
        if (contains(name, "短剧", "短劇", "微剧", "微劇", "红果", "河马剧场") || contains(api, "duanju", "minidrama")) return DRAMA;
        if (contains(name, "音乐", "音樂", "酷我", "酷狗", "qq音乐", "網易雲", "网易云", "喜马拉雅", "蜻蜓", "music") || contains(api, "music")) return MUSIC;
        if (site.getType() == 3 || api.endsWith(".py") || key.startsWith("py") || key.contains("_py") || name.startsWith("py")) return PY;
        return JAR;
    }

    /** 分组结果：分类标题(以 {@link Integer} 存放) 与 站点(Site) 混合的有序列表 */
    public static List<Object> group(List<Site> sites) {
        List<Object> result = new ArrayList<>();
        for (int category : ORDER) {
            List<Site> items = find(sites, category);
            if (items.isEmpty()) continue;
            result.add(category);
            result.addAll(items);
        }
        return result;
    }

    /** 某一分类下的站点，保持原始顺序 */
    public static List<Site> find(List<Site> sites, int category) {
        List<Site> result = new ArrayList<>();
        for (Site site : sites) if (of(site) == category) result.add(site);
        return result;
    }

    private static boolean contains(String text, String... keywords) {
        for (String keyword : keywords) if (text.contains(keyword)) return true;
        return false;
    }
}
