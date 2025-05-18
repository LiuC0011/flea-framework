package com.huazie.fleaframework.cache;

import com.huazie.fleaframework.cache.common.CacheEnum;
import com.huazie.fleaframework.common.CommonConstants;
import com.huazie.fleaframework.common.slf4j.FleaLogger;
import com.huazie.fleaframework.common.slf4j.impl.FleaLoggerProxy;
import com.huazie.fleaframework.common.util.CollectionUtils;
import com.huazie.fleaframework.common.util.ObjectUtils;
import com.huazie.fleaframework.common.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * 抽象Flea Cache类，实现了Flea缓存接口的读、写、删除和清空缓存的基本操作。
 *
 * <p> 它定义本地读、本地写和本地删除的抽象方法，由子类实现具体的读、
 * 写和删除缓存的操作。
 *
 * <p> 在实际调用写缓存操作时，会同时记录当前缓存数据的数据键关键字
 * 【{@code key}】到专门的数据键关键字的缓存中，以Set集合存储。
 *
 * <p> 比如缓存数据主关键字为【{@code name}】，需要存储的数据键关键字为
 * 【{@code key}】，则在实际调用写缓存操作时，会操作两条缓存数据：<br/>
 * 一条是具体的数据缓存，缓存键为“系统名_name_key”，可查看方法
 * 【{@code getNativeKey}】，有效期从配置中获取；<br/>
 * 一条是数据键关键字的缓存，缓存键为“系统名_name”，可查看方法
 * 【{@code getNativeCacheKey}】，默认永久有效。
 *
 * @author huazie
 * @version 1.1.0
 * @since 1.0.0
 */
public abstract class AbstractFleaCache implements IFleaCache {

    private static final FleaLogger LOGGER = FleaLoggerProxy.getProxyInstance(AbstractFleaCache.class);

    private final String name;  // 缓存数据主关键字

    private final int expiry;  // 缓存数据有效期（单位：s）

    private final int nullCacheExpiry; // 空缓存数据有效期（单位：s）

    protected CacheEnum cache;  // 缓存实现

    public AbstractFleaCache(String name, int expiry, int nullCacheExpiry) {
        this.name = name;
        this.expiry = expiry;
        this.nullCacheExpiry = nullCacheExpiry;
    }

    @Override
    public Object get(String key) {
        Object value = null;
        Object obj = new Object() {};
        try {
            LOGGER.debug1(obj, "KEY = {}", key);
            value = getNativeValue(getNativeKey(key));
            LOGGER.debug1(obj, "VALUE = {}", value);
        } catch (Exception e) {
            LOGGER.error1(obj, "The action of getting [" + cache.getName() + "] cache occurs exception : ", e);
        }
        return value;
    }

    @Override
    public void put(String key, Object value) {
        try {
            Object result = putNativeValue(getNativeKey(key), value, expiry);
            LOGGER.debug1(new Object() {}, "Result = {}", result);
            // 将指定Cache的key添加到Set集合，并存于缓存中
            addCacheKey(key);
        } catch (Exception e) {
            LOGGER.error1(new Object() {}, "The action of adding [" + cache.getName() + "] cache occurs exception : ", e);
        }
    }

    @Override
    public void clear() {
        Set<String> keySet = getCacheKey();
        Object obj = new Object() {};
        LOGGER.debug1(obj, "KEYS = {}", keySet);

        if (CollectionUtils.isEmpty(keySet)) return;

        for (String key : keySet) {
            Object result = deleteNativeValue(getNativeKey(key));
            LOGGER.debug1(obj, "Result = {}", result);
        }
        // 删除 记录当前Cache所有数据键关键字 的缓存
        deleteCacheAllKey();
    }

    @Override
    public void delete(String key) {
        try {
            Object result = deleteNativeValue(getNativeKey(key));
            LOGGER.debug1(new Object() {}, "Result = {}", result);
            // 从 记录当前Cache所有数据键关键字 的缓存中 删除指定数据键关键字key
            deleteCacheKey(key);
        } catch (Exception e) {
            LOGGER.error1(new Object() {}, "The action of deleting [" + cache.getName() + "] cache occurs exception : ", e);
        }
    }

    /**
     * 将指定数据键关键字{@code key}记录到当前Cache所有数据键关键字的缓存中
     *
     * @param key 指定Cache的数据键关键字
     * @since 1.0.0
     */
    private void addCacheKey(String key) {
        Set<String> keySet = getCacheKey();
        if (CollectionUtils.isEmpty(keySet)) {
            keySet = new HashSet<>();
        }
        if (!keySet.contains(key)) { // 只有其中不存在，才重新设置
            keySet.add(key);
            Object result = putNativeValue(getNativeCacheKey(name), keySet, CommonConstants.NumeralConstants.INT_ZERO);
            LOGGER.debug1(new Object() {}, "Result = {}", result);
        }
    }

    /**
     * 从 记录当前Cache所有数据键关键字 的缓存中 删除指定数据键关键字{@code key}
     *
     * @param key 指定Cache的数据键关键字
     * @since 1.0.0
     */
    private void deleteCacheKey(String key) {
        Set<String> keySet = getCacheKey();

        if (CollectionUtils.isEmpty(keySet)) return;

        Object obj = new Object() {};

        // 存在待删除的数据键关键字
        if (keySet.contains(key)) {
            LOGGER.debug1(obj, "Delete cache of recording all key, KEY = {}", key);
            if (CommonConstants.NumeralConstants.INT_ONE == keySet.size()) {
                deleteCacheAllKey(); // 直接将记录当前Cache所有数据键关键字的缓存从缓存中清空
            } else {
                // 将数据键关键字从Set集合中删除
                keySet.remove(key);
                // 重新覆盖当前Cache所有数据键关键字的缓存信息
                Object result = putNativeValue(getNativeCacheKey(name), keySet, CommonConstants.NumeralConstants.INT_ZERO);
                LOGGER.debug1(obj, "Result = {}", result);
            }
        } else {
            LOGGER.debug1(obj, "The CacheKey of [{}] is not exist", key);
        }
    }

    /**
     * 删除 记录当前Cache所有数据键关键字 的缓存
     *
     * @since 1.0.0
     */
    private void deleteCacheAllKey() {
        Object obj = new Object() {};
        try {
            LOGGER.debug1(obj, "Delete cache of recording all key");
            Object result = deleteNativeValue(getNativeCacheKey(name));
            LOGGER.debug1(obj, "Result = {}", result);
        } catch (Exception e) {
            LOGGER.error1(obj, "The action of deleting [" + cache.getName() + "] cache occurs exception : ", e);
        }
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public Set<String> getCacheKey() {
        Set<String> keySet = null;
        try {
            Object keySetObj = getNativeValue(getNativeCacheKey(name));
            if (ObjectUtils.isNotEmpty(keySetObj) && keySetObj instanceof Set) {
                keySet = (Set<String>) keySetObj;
            }
            LOGGER.debug1(new Object() {}, "CacheKey = {}", keySet);
        } catch (Exception e) {
            LOGGER.error1(new Object() {}, "The action of getting [" + cache.getName() + "] cache occurs exception : ", e);
        }
        return keySet;
    }

    /**
     * 获取缓存值
     *
     * @param key 缓存数据键关键字
     * @return 缓存值
     * @since 1.0.0
     */
    public abstract Object getNativeValue(String key);

    /**
     * 添加缓存数据
     *
     * @param key    缓存数据键关键字
     * @param value  缓存值
     * @param expiry 有效期（单位：s）
     * @since 1.0.0
     */
    public abstract Object putNativeValue(String key, Object value, int expiry);

    /**
     * 删除指定缓存数据
     *
     * @param key 缓存数据键关键字
     * @since 1.0.0
     */
    public abstract Object deleteNativeValue(String key);

    /**
     * 获取实际存储的缓存键【缓存所属系统名 + 缓存名（缓存数据主关键字）+ 缓存数据键（缓存数据关键字）】
     *
     * @param key 缓存数据键关键字
     * @return 实际存储的缓存键
     * @since 1.0.0
     */
    private String getNativeKey(String key) {
        return StringUtils.strCat(getNativeCacheKey(name), CommonConstants.SymbolConstants.UNDERLINE, key);
    }

    /**
     * 获取缓存主键【包含缓存所属系统名 + 缓存名（缓存数据主关键字）】
     *
     * @param name 缓存名【缓存数据主关键字】
     * @return 缓存主键【缓存所属系统名 + 缓存名（缓存数据主关键字）】
     * @since 1.0.0
     */
    protected String getNativeCacheKey(String name) {
        return StringUtils.strCat(getSystemName(), CommonConstants.SymbolConstants.UNDERLINE, name);
    }

    /**
     * 获取缓存数据主关键字
     *
     * @return 缓存数据主关键字
     * @since 1.0.0
     */
    public String getName() {
        return name;
    }

    /**
     * 获取缓存数据有效期（单位：s）
     *
     * @return 缓存数据有效期（单位：s）
     * @since 1.0.0
     */
    public int getExpiry() {
        return expiry;
    }

    /**
     * 获取空缓存数据有效期（单位：s）
     *
     * @return 空缓存数据有效期（单位：s）
     * @since 1.0.0
     */
    public int getNullCacheExpiry() {
        return nullCacheExpiry;
    }

    public String getCacheName() {
        return cache.getName();
    }

    public String getCacheDesc() {
        return cache.getDesc();
    }

    public CacheEnum getCache() {
        return cache;
    }
}
