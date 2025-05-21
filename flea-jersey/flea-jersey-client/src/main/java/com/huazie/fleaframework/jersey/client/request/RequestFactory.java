package com.huazie.fleaframework.jersey.client.request;

import com.huazie.fleaframework.common.exceptions.CommonException;
import com.huazie.fleaframework.common.slf4j.FleaLogger;
import com.huazie.fleaframework.common.slf4j.impl.FleaLoggerProxy;
import com.huazie.fleaframework.common.util.ExceptionUtils;
import com.huazie.fleaframework.common.util.ObjectUtils;
import com.huazie.fleaframework.common.util.ReflectUtils;
import com.huazie.fleaframework.common.util.StringUtils;
import com.huazie.fleaframework.jersey.common.exceptions.FleaJerseyClientException;

/**
 * Flea Jersey 请求工厂类，采用单例模式，提供构建
 * Flea Jersey 请求的能力。
 *
 * @author huazie
 * @version 1.0.0
 * @see RequestModeEnum
 * @since 1.0.0
 */
public class RequestFactory {

    private static final FleaLogger LOGGER = FleaLoggerProxy.getProxyInstance(RequestFactory.class);

    private static volatile RequestFactory factory;

    private RequestFactory() {
    }

    /**
     * 获取请求工厂实例
     *
     * @return 请求工厂实例
     * @since 1.0.0
     */
    public static RequestFactory getInstance() {
        if (ObjectUtils.isEmpty(factory)) {
            synchronized (RequestFactory.class) {
                if (ObjectUtils.isEmpty(factory)) {
                    factory = new RequestFactory();
                }
            }
        }
        return factory;
    }

    /**
     * 根据请求配置，构建一个 Flea 请求
     *
     * @param config 请求配置
     * @return Flea请求
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public Request buildFleaRequest(RequestConfig config) throws CommonException {

        if (ObjectUtils.isEmpty(config) || config.isEmpty()) {
            // 未初始化请求配置，请检查！！！
            ExceptionUtils.throwCommonException(FleaJerseyClientException.class, "ERROR-JERSEY-CLIENT0000000000");
        }

        Object obj = new Object() {};
        LOGGER.debug1(obj, "Start");
        LOGGER.debug1(obj, "RequestConfig = {}", config.getConfig());

        // 获取请求方式
        String requestMode = config.getRequestMode();
        // 【{0}】未配置，请检查！！！
        StringUtils.checkBlank(requestMode, FleaJerseyClientException.class, "ERROR-JERSEY-CLIENT0000000008", RequestConfigEnum.REQUEST_MODE.getKey());

        // 获取请求模式类型枚举
        RequestModeEnum requestModeEnum = null;

        try {
            requestModeEnum = RequestModeEnum.valueOf(requestMode.toUpperCase());
        } catch (IllegalArgumentException e) {
            LOGGER.error1(obj, "Exception = ", e);
        }

        Request request = null;

        if (ObjectUtils.isNotEmpty(requestModeEnum)) {
            Object requestObj = ReflectUtils.newInstance(requestModeEnum.getImplClass(), config);
            if (ObjectUtils.isNotEmpty(requestObj) && requestObj instanceof Request) {
                request = (Request) requestObj;
            }
        }

        if (ObjectUtils.isNotEmpty(request)) {
            LOGGER.debug1(obj, "Request = {}", request.getClass().getName());
            LOGGER.debug1(obj, "RequestMode = {}", request.getRequestMode().getMode());
        }
        LOGGER.debug1(obj, "End");

        return request;
    }
}
