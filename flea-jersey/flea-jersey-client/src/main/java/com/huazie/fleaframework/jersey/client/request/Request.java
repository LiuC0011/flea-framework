package com.huazie.fleaframework.jersey.client.request;

import com.huazie.fleaframework.common.exception.CommonException;
import com.huazie.fleaframework.jersey.client.response.Response;

/**
 * <p> Flea Request 接口 </p>
 *
 * @author huazie
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Request {

    /**
     * <p> 执行请求 </p>
     *
     * @return 响应结果
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    <T> Response<T> doRequest(Class<T> clazz) throws CommonException;

    /**
     * <p> 获取请求方式 </p>
     *
     * @return 请求方式
     * @since 1.0.0
     */
    RequestModeEnum getRequestMode();
}
