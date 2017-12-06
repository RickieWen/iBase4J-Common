package top.ibase4j.core.util;

import top.ibase4j.core.support.cache.CacheManager;

public class CacheUtil {
    private static CacheManager cacheManager;

    public static void setCacheManager(CacheManager cacheManager) {
        CacheUtil.cacheManager = cacheManager;
    }

    public static CacheManager getCache() {
        return cacheManager;
    }

    /** 获取锁 */
    public static boolean tryLock(String key) {
        int expires = 1000 * PropertiesUtil.getInt("redis.lock.expires", 180);
        return cacheManager.setnx(key, expires);
    }

    /** 获取锁 */
    public static boolean getLock(String key) {
        return cacheManager.lock(key);
    }

    /** 解锁 */
    public static void unlock(String key) {
        cacheManager.unlock(key);
    }
}
