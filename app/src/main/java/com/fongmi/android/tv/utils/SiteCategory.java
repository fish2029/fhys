package com.fongmi.android.tv.utils;

import android.text.TextUtils;

import com.fongmi.android.tv.bean.Site;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 站源智能分类：按站点特征（key / 名称 / api / jar / ext / type）自动归类。
 * 首页站源切换列表按此分类分组渲染，顺序固定：JAR内站点、PY站点、音乐、短剧。
 */
public class SiteCategory {

    public static final int JAR = 0;
    public static final int PY = 1;
    public static final int MUSIC = 2;
    public static final int DRAMA = 3;

    private static final int[] ORDER = {JAR, PY, MUSIC, DRAMA};

    /** PY 站点标记：py / python 作为独立词出现（避免误伤 php、happy 之类） */
    private static final Pattern PY_TOKEN = Pattern.compile("(?i)(^|[^a-z])(py|python)([^a-z]|$)");

    /** 判断一个字段里是否带 PY 站点特征 */
    private static boolean pyToken(String value) {
        return value != null && PY_TOKEN.matcher(value).find();
    }

    /** 站点归类：先识别短剧/音乐，再识别 PY，其余算 JAR 内站点 */
    public static int of(Site site) {
        String name = lower(site.getName());
        String api = lower(site.getApi());
        String key = lower(site.getKey());
        String jar = lower(site.getJar());
        String ext = lower(site.getExt());

        if (contains(name, "短剧", "短劇", "微剧", "微劇", "红果", "河马剧场", "河馬劇場")
                || contains(api, "duanju", "minidrama")) {
            return DRAMA;
        }
        if (contains(name, "音乐", "音樂", "酷我", "酷狗", "qq音乐", "qq音樂", "網易雲", "网易云", "喜马拉雅", "喜馬拉雅", "蜻蜓", "music")
                || contains(api, "music")) {
            return MUSIC;
        }
        // PY 站点：type=3（本地/远程 py、js 蜘蛛），或任一字段带 py/python 特征
        if (site.getType() == 3
                || api.endsWith(".py") || api.contains(".py") || api.contains("pyspider")
                || pyToken(key) || pyToken(name) || pyToken(api) || pyToken(jar) || pyToken(ext)) {
            return PY;
        }
        return JAR;
    }

    /**
     * 把站点列表整理成「分组标题 + 站点」的混合列表，空分组跳过。
     * 标题项是 Integer（分类常量），站点项是 Site。
     */
    public static List<Object> group(List<Site> sites) {
        List<Object> result = new ArrayList<>();
        if (sites == null || sites.isEmpty()) return result;
        for (int category : ORDER) {
            boolean headerAdded = false;
            for (Site site : sites) {
                if (of(site) != category) continue;
                if (!headerAdded) {
                    result.add(category);
                    headerAdded = true;
                }
                result.add(site);
            }
        }
        return result;
    }

    /**
     * 返回某个分类下的所有站点（供筛选/统计用）。
     */
    public static List<Site> find(List<Site> sites, int category) {
        List<Site> result = new ArrayList<>();
        if (sites == null) return result;
        for (Site site : sites) {
            if (of(site) == category) result.add(site);
        }
        return result;
    }

    private static String lower(String value) {
        return TextUtils.isEmpty(value) ? "" : value.toLowerCase();
    }

    private static boolean contains(String value, String... keys) {
        for (String key : keys) {
            if (value.contains(key)) return true;
        }
        return false;
    }
}
