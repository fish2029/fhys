package com.fongmi.android.tv.api;

import android.text.TextUtils;
import android.util.Log;

import com.fongmi.android.tv.App;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * 首页跑马灯公告：直接取「繁华影视」后台里配的「跑马灯公告」表，不是把文案写死。
 *
 * 只保留 status=启用 且绑定电视端的记录，按权重降序，内容去重后拼成一条给跑马灯滚动。
 * 后台改了什么，下一次刷新（缓存 5 分钟 / 重启 App）就变什么。
 */
public class NoticeApi {

    private static final String TAG = "NoticeED";
    private static final long CACHE_MS = 5 * 60 * 1000L;

    private static String cached = null;
    private static long cachedAt = 0L;

    /** 取公告。回调已切回主线程。 */
    public static void load(Consumer<String> callback) {
        if (cached != null && System.currentTimeMillis() - cachedAt < CACHE_MS) {
            callback.accept(cached);
            return;
        }
        BoxApi.table("box_marquee_notice", rows -> {
            String text = parse(rows);
            Log.d(TAG, "notice len=" + text.length());
            if (!TextUtils.isEmpty(text)) {
                cached = text;
                cachedAt = System.currentTimeMillis();
            }
            // 无论有没有内容都要回调：调用方需要在后台没配或拉取失败时退回配置 JSON 的 notice。
            // 只在有内容时回调会让 fallback 永远不执行，公告直接消失。
            App.post(() -> callback.accept(text));
        });
    }

    /** 后台权重字段有 weigh / weight 两种写法，取到哪个算哪个。 */
    private static int weight(JSONObject row) {
        if (row == null) return 0;
        if (row.has("weigh")) return row.optInt("weigh");
        return row.optInt("weight", 0);
    }

    /** 只留启用 + 绑定电视端的公告，按权重降序，内容相同的只留一条。 */
    private static String parse(JSONArray rows) {
        if (rows == null) return "";
        try {
            List<JSONObject> list = new ArrayList<>();
            for (int i = 0; i < rows.length(); i++) {
                JSONObject row = rows.optJSONObject(i);
                if (row == null) continue;
                if (!BoxApi.enabled(row) || !BoxApi.matchApp(row)) continue;
                list.add(row);
            }
            // 权重字段兼容 weigh / weight 两种写法，缺省按 0 处理
            Collections.sort(list, (a, b) -> weight(b) - weight(a));
            StringBuilder sb = new StringBuilder();
            List<String> seen = new ArrayList<>();
            for (JSONObject row : list) {
                String content = row.optString("content").trim();
                if (content.isEmpty()) continue;
                // 后台常有多条内容一模一样的公告（不同权重各留一份），重复滚动很吵
                if (seen.contains(content)) continue;
                seen.add(content);
                if (sb.length() > 0) sb.append("　　");
                sb.append(content);
            }
            return sb.toString();
        } catch (Throwable e) {
            Log.d(TAG, "parse error: " + e);
            return "";
        }
    }
}
