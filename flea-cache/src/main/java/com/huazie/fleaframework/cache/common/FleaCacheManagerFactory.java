package com.huazie.fleaframework.cache.common;

import com.huazie.fleaframework.cache.AbstractFleaCacheManager;
import com.huazie.fleaframework.cache.core.manager.CoreFleaCacheManager;
import com.huazie.fleaframework.cache.memcached.manager.MemCachedFleaCacheManager;
import com.huazie.fleaframework.cache.redis.manager.RedisClusterFleaCacheManager;
import com.huazie.fleaframework.cache.redis.manager.RedisShardedFleaCacheManager;
import com.huazie.fleaframework.common.FleaCommonConfig;
import com.huazie.fleaframework.common.strategy.FleaStrategyFacade;
import com.huazie.fleaframework.common.strategy.IFleaStrategyContext;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Flea缓存管理者工厂类，不同缓存实现对应不同的Flea缓存管理者。
 *
 * <p> 同步集合类【{@code cacheMap}】，存储的键为缓存实现名，
 * 目前仅包含 MemCached、RedisSharded、RedisCluster 和 FleaCore；
 * 存储的值为Flea缓存管理者，目前包含MemCached缓存管理者、
 * Redis分片缓存管理者、Redis集群缓存管理者和Flea核心缓存管理者。
 *
 * @author huazie
 * @version 1.1.0
 * @see MemCachedFleaCacheManager
 * @see RedisShardedFleaCacheManager
 * @see RedisClusterFleaCacheManager
 * @see CoreFleaCacheManager
 * @since 1.0.0
 */
public class FleaCacheManagerFactory {

    private static final ConcurrentMap<String, AbstractFleaCacheManager> managerMap = new ConcurrentHashMap<>();

    private static final Object managerMapLock = new Object();

    private static final Object managerMapLockNew = new Object();

    private static final IFleaStrategyContext<AbstractFleaCacheManager, FleaCommonConfig> fleaStrategy = new FCMStrategyContext();

    private FleaCacheManagerFactory() {
    }

    /**
     * 获取Flea Cache管理类对象实例
     *
     * @param name 缓存实现名称
     * @return Flea Cache管理类对象实例
     * @since 1.0.0
     */
    public static AbstractFleaCacheManager getFleaCacheManager(String name) {
        if (!managerMap.containsKey(name)) {
            synchronized (managerMapLock) {
                if (!managerMap.containsKey(name)) {
                    managerMap.put(name, FleaStrategyFacade.invoke(name, fleaStrategy));
                }
            }
        }
        return managerMap.get(name);
    }

    /**
     * 获取Redis哨兵 Flea Cache管理类对象实例【指定Redis数据库索引】
     *
     * @param database Redis数据库索引
     * @return Flea Cache管理类对象实例
     * @since 2.0.0
     */
    public static AbstractFleaCacheManager getFleaCacheManager(int database) {
        String name = CacheEnum.RedisSentinel.getName();
        if (database == 0)
            return getFleaCacheManager(name);
        name += database;
        if (!managerMap.containsKey(name)) {
            synchronized (managerMapLockNew) {
                if (!managerMap.containsKey(name)) {
                    FleaCommonConfig config = new FleaCommonConfig();
                    config.put(CacheConstants.RedisConfigConstants.REDIS_SENTINEL_CONFIG_DATABASE, database);
                    managerMap.put(name, FleaStrategyFacade.invoke(CacheEnum.RedisSentinel.getName(), new FCMStrategyContext(config)));
                }
            }
        }
        return managerMap.get(name);
    }

}
