package com.huazie.fleaframework.cache.redis;

import redis.clients.jedis.Client;
import redis.clients.jedis.params.SetParams;

/**
 * Redis客户端接口，定义了 读、写、删除 Redis缓存的基本操作方法。
 *
 * <p> 分片模式下，针对单独缓存接入场景，可以通过如下方式对外使用Redis客户端，
 * 使用之前需要先初始化Redis连接池【默认default】：
 * <pre>
 *   // 初始化默认连接池
 *   RedisShardedPool.getInstance().initialize();
 *   // 获取分片模式下默认连接池的Redis客户端
 *   RedisClient redisClient = RedisClientFactory.getInstance(); </pre>
 *
 * <p> 集群模式下，针对单独缓存接入场景，可以通过如下方式对外使用Redis客户端，
 * 使用之前需要先初始化Redis连接池【默认default】：
 * <pre>
 *   // 初始化默认连接池
 *   RedisClusterPool.getInstance().initialize();
 *   // 获取集群模式下默认连接池的Redis客户端
 *   RedisClient redisClient = RedisClientFactory.getInstance(CacheModeEnum.CLUSTER); </pre>
 *
 * <p> 哨兵模式下，针对单独缓存接入场景，可以通过如下方式对外使用Redis客户端，
 * 使用之前需要先初始化Redis连接池【默认default】：
 * <pre>
 *   // 初始化默认连接池
 *   RedisSentinelPool.getInstance().initialize(database);
 *   // 获取哨兵模式下默认连接池的Redis客户端
 *   RedisClient redisClient = RedisClientFactory.getInstance(CacheModeEnum.SENTINEL); </pre>
 *
 * <p> 分片模式下，针对整合缓存接入场景，可以通过如下方式对外使用Redis客户端，
 * 使用之前需要先初始化Redis连接池【指定缓存组名】：
 * <pre>
 *   // 初始化指定缓存组名的连接池
 *   RedisPool.getInstance(group).initialize(cacheServerList);
 *   // 获取分片模式下指定连接池【group】的Redis客户端
 *   RedisClient redisClient = RedisClientFactory.getInstance(group); </pre>
 *
 * <p> 集群模式下，针对整合缓存接入场景，可以通过如下方式对外使用Redis客户端，
 * 使用之前需要先初始化Redis连接池【指定缓存组名】：
 * <pre>
 *   // 初始化指定缓存组名的连接池
 *   RedisClusterPool.getInstance(group).initialize(cacheServerList);
 *   // 获取集群模式下指定连接池【group】的Redis客户端
 *   RedisClient redisClient = RedisClientFactory.getInstance(group, CacheModeEnum.CLUSTER); </pre>
 *
 * <p> 哨兵模式下，针对整合缓存接入场景，可以通过如下方式对外使用Redis客户端，
 * 使用之前需要先初始化Redis连接池【指定缓存组名】：
 * <pre>
 *   // 初始化指定缓存组名的连接池
 *   RedisSentinelPool.getInstance(group).initialize(cacheServerList);
 *   // 获取哨兵模式下指定连接池【group】的Redis客户端
 *   RedisClient redisClient = RedisClientFactory.getInstance(group, CacheModeEnum.SENTINEL); </pre>
 *
 * @author huazie
 * @version 1.1.0
 * @since 1.0.0
 */
public interface RedisClient {

    /**
     * 往Redis塞数据
     *
     * @param key   数据键
     * @param value 数据值
     * @return 状态码 （OK ：成功）
     * @since 1.0.0
     */
    String set(final String key, final Object value);

    /**
     * 往Redis赛数据（用于序列化对象）
     *
     * @param key   数据键
     * @param value 数据值
     * @return 状态码 （OK ：成功）
     * @since 1.0.0
     */
    String set(final byte[] key, final byte[] value);

    /**
     * 往Redis塞数据 (带有效期)
     *
     * @param key    数据键
     * @param value  数据值
     * @param expiry 有效期（单位：s）
     * @return 状态码 （OK ：成功）
     * @since 1.0.0
     */
    String set(final String key, final Object value, final int expiry);

    /**
     * 往Redis塞数据 (带有效期，用于序列化对象)
     *
     * @param key    数据键
     * @param value  数据值
     * @param expiry 有效期（单位：s）
     * @return 状态码 （OK ：成功）
     * @since 1.0.0
     */
    String set(final byte[] key, final byte[] value, final int expiry);

    /**
     * 往Redis塞数据 (带有效期)
     *
     * @param key    数据键
     * @param value  数据值
     * @param expiry 有效期（单位：ms）
     * @return 状态码 （OK ：成功）
     * @since 1.0.0
     */
    String set(final String key, final Object value, final long expiry);

    /**
     * 往Redis塞数据 (带有效期，用于序列化对象)
     *
     * @param key    数据键
     * @param value  数据值
     * @param expiry 有效期（单位：ms）
     * @return 状态码 （OK ：成功）
     * @since 1.0.0
     */
    String set(final byte[] key, final byte[] value, final long expiry);

    /**
     * 往Redis塞数据 (带参数)
     *
     * @param key    数据键
     * @param value  数据值
     * @param params 参数
     * @return 状态码 （OK ：成功）
     * @since 1.0.0
     */
    String set(final String key, final Object value, final SetParams params);

    /**
     * 往Redis塞数据 (带参数，用于序列化对象)
     *
     * @param key    数据键
     * @param value  数据值
     * @param params 参数
     * @return 状态码 （OK ：成功）
     * @since 1.0.0
     */
    String set(final byte[] key, final byte[] value, final SetParams params);

    /**
     * 从Redis取数据
     *
     * @param key 数据键
     * @return 数据值
     * @since 1.0.0
     */
    Object get(final String key);

    /**
     * 从Redis取数据（用于获取序列化对象）
     *
     * @param key 数据键
     * @return 数据值
     * @since 1.0.0
     */
    byte[] get(final byte[] key);

    /**
     * 从Redis中删除数据
     *
     * @param key 数据键
     * @return 被删除key的数量
     * @since 1.0.0
     */
    Long del(final String key);

    /**
     * 获取数据所在的Redis服务器ip(主机地址+端口)
     *
     * @param key 数据键
     * @return 当前数据所在的Redis服务器ip
     * @since 1.0.0
     */
    String getLocation(final String key);

    /**
     * 获取数据所在的Redis服务器ip(主机地址+端口)
     *
     * @param key 数据键(字节数组)
     * @return 当前数据所在的Redis服务器ip
     * @since 1.0.0
     */
    String getLocation(final byte[] key);

    /**
     * 获取数据所在的Redis服务器主机
     *
     * @param key 数据键
     * @return 数据所在的Redis服务器主机
     * @since 1.0.0
     */
    String getHost(final String key);

    /**
     * 获取数据所在的Redis服务器主机
     *
     * @param key 数据键(字节数组)
     * @return 数据所在的Redis服务器主机
     * @since 1.0.0
     */
    String getHost(final byte[] key);

    /**
     * 获取数据所在的Redis服务器主机端口
     *
     * @param key 数据键
     * @return 数据所在的Redis服务器主机端口
     * @since 1.0.0
     */
    Integer getPort(final String key);

    /**
     * 获取数据所在的Redis服务器主机端口
     *
     * @param key 数据键(字节数组)
     * @return 数据所在的Redis服务器主机端口
     * @since 1.0.0
     */
    Integer getPort(final byte[] key);

    /**
     * 获取数据所在的客户端类
     *
     * @param key 数据键
     * @return 数据所在的客户端类
     * @since 1.0.0
     */
    Client getClient(final String key);

    /**
     * 获取数据所在的客户端类
     *
     * @param key 数据键
     * @return 数据所在的客户端类
     * @since 1.0.0
     */
    Client getClient(final byte[] key);

    /**
     * 获取连接池名
     *
     * @return 连接池名
     * @since 1.0.0
     */
    String getPoolName();

}
