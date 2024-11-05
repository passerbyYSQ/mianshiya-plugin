package com.github.yuyuanweb.mianshiyaplugin.utils;

import cn.hutool.cache.impl.TimedCache;

import java.util.HashMap;

/**
 * 缓存工具类
 *
 * @author pine
 */
public class CacheUtil extends cn.hutool.cache.CacheUtil {

    public static String QUESTION_ORDER_KEY = "question:Order:";

    // 2个小时的缓存
    public static TimedCache<String, HashMap<Long, Long>> TIMED_CACHE = cn.hutool.cache.CacheUtil.newTimedCache(2 * 60 * 60 * 1000);
}
