package com.huazie.fleaframework.jersey.client.request.impl;

import com.huazie.fleaframework.common.FleaSessionManager;
import com.huazie.fleaframework.common.exceptions.CommonException;
import com.huazie.fleaframework.common.slf4j.FleaLogger;
import com.huazie.fleaframework.common.slf4j.impl.FleaLoggerProxy;
import com.huazie.fleaframework.common.util.ExceptionUtils;
import com.huazie.fleaframework.common.util.ObjectUtils;
import com.huazie.fleaframework.common.util.ReflectUtils;
import com.huazie.fleaframework.common.util.StringUtils;
import com.huazie.fleaframework.common.util.json.GsonUtils;
import com.huazie.fleaframework.common.util.xml.JABXUtils;
import com.huazie.fleaframework.jersey.client.request.Request;
import com.huazie.fleaframework.jersey.client.request.RequestConfig;
import com.huazie.fleaframework.jersey.client.request.RequestConfigEnum;
import com.huazie.fleaframework.jersey.client.request.RequestModeEnum;
import com.huazie.fleaframework.jersey.client.response.Response;
import com.huazie.fleaframework.jersey.common.FleaJerseyConfig;
import com.huazie.fleaframework.jersey.common.data.FleaJerseyRequest;
import com.huazie.fleaframework.jersey.common.data.FleaJerseyRequestData;
import com.huazie.fleaframework.jersey.common.data.FleaJerseyResponse;
import com.huazie.fleaframework.jersey.common.data.FleaJerseyResponseData;
import com.huazie.fleaframework.jersey.common.data.RequestBusinessData;
import com.huazie.fleaframework.jersey.common.data.RequestPublicData;
import com.huazie.fleaframework.jersey.common.data.ResponseBusinessData;
import com.huazie.fleaframework.jersey.common.data.ResponsePublicData;
import com.huazie.fleaframework.jersey.common.exceptions.FleaJerseyClientException;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Flea 抽象请求，封装了公共的 Flea Jersey 请求处理的能力。
 *
 * @author huazie
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class FleaRequest implements Request {

    private static final FleaLogger LOGGER = FleaLoggerProxy.getProxyInstance(FleaRequest.class);

    private RequestConfig config; // 请求配置

    RequestModeEnum modeEnum; // 请求方式

    /**
     * 默认的构造方法
     *
     * @since 1.0.0
     */
    FleaRequest() {
        init(); // 子类实现该方法，用于自定义的初始化内容
    }

    /**
     * 带请求配置参数的构造的方法
     *
     * @param config 请求配置
     * @since 1.0.0
     */
    FleaRequest(RequestConfig config) {
        this.config = config;
        init();
    }

    @Override
    public <T> Response<T> doRequest(Class<T> clazz) throws CommonException {

        Object obj = new Object() {};
        LOGGER.debug1(obj, "Start");

        if (ObjectUtils.isEmpty(config) || config.isEmpty()) {
            // 未初始化请求配置，请检查！
            ExceptionUtils.throwCommonException(FleaJerseyClientException.class, "ERROR-JERSEY-CLIENT0000000000");
        }

        // 客户端编码
        String clientCode = config.getClientCode();
        // 【{0}】未配置，请检查！！！
        StringUtils.checkBlank(clientCode, FleaJerseyClientException.class, "ERROR-JERSEY-CLIENT0000000008", RequestConfigEnum.CLIENT_CODE.getKey());

        // 业务入参
        Object input = config.getInputObj();
        // 【{0}】未配置，请检查！！！
        ObjectUtils.checkEmpty(input, FleaJerseyClientException.class, "ERROR-JERSEY-CLIENT0000000008", RequestConfigEnum.INPUT_OBJECT.getKey());

        // 资源地址
        String resourceUrl = config.getResourceUrl();
        // 【{0}】未配置，请检查！！！
        StringUtils.checkBlank(resourceUrl, FleaJerseyClientException.class, "ERROR-JERSEY-CLIENT0000000008", RequestConfigEnum.RESOURCE_URL.getKey());

        // 资源编码
        String resourceCode = config.getResourceCode();
        // 【{0}】未配置，请检查！！！
        StringUtils.checkBlank(resourceCode, FleaJerseyClientException.class, "ERROR-JERSEY-CLIENT0000000008", RequestConfigEnum.RESOURCE_CODE.getKey());

        // 服务编码
        String serviceCode = config.getServiceCode();
        // 【{0}】未配置，请检查！！！
        StringUtils.checkBlank(serviceCode, FleaJerseyClientException.class, "ERROR-JERSEY-CLIENT0000000008", RequestConfigEnum.SERVICE_CODE.getKey());

        // 业务入参
        String clientInput = config.getClientInput();
        Class inputClazz = ReflectUtils.forName(clientInput);
        // 请检查客户端配置【client_code = {0}】: 【{1} = {2}】非法
        ObjectUtils.checkEmpty(inputClazz, FleaJerseyClientException.class, "ERROR-JERSEY-CLIENT0000000010",
                clientCode, RequestConfigEnum.CLIENT_INPUT.getKey(), clientInput);

        if (!inputClazz.isInstance(input)) {
            // 请检查客户端配置【client_code = {0}】：配置的入参【client_input = {1}】类型和实际传入的入参【{2}】类型不一致
            ExceptionUtils.throwCommonException(FleaJerseyClientException.class, "ERROR-JERSEY-CLIENT0000000004",
                    clientCode, clientInput, input.getClass().getName());
        }

        // 业务出参
        String clientOutput = config.getClientOutput();
        Class mClazz = ReflectUtils.forName(clientOutput);
        // 请检查客户端配置【client_code = {0}】: 【{1} = {2}】非法
        ObjectUtils.checkEmpty(mClazz, FleaJerseyClientException.class, "ERROR-JERSEY-CLIENT0000000010",
                clientCode, RequestConfigEnum.CLIENT_OUTPUT.getKey(), clientOutput);

        if (!mClazz.equals(clazz)) {
            // 请检查客户端配置【client_code = {0}】：配置的出参【client_output = {1}】类型和实际需要返回的出参【{2}】类型不一致
            ExceptionUtils.throwCommonException(FleaJerseyClientException.class, "ERROR-JERSEY-CLIENT0000000007",
                    clientCode, clientOutput, clazz.getName());
        }

        // 客户端注册MultiPartFeature组件，用于支持 multipart/form-data 媒体类型
        WebTarget target = ClientBuilder.newClient().register(MultiPartFeature.class).target(resourceUrl).path(resourceCode);

        FleaJerseyRequest request = createFleaJerseyRequest(resourceCode, serviceCode, input);

        LOGGER.debug1(obj, "FleaJerseyRequest = \n{}", JABXUtils.toXml(request, true));

        FleaJerseyResponse response = request(target, request);
        // 资源服务请求异常：响应报文为空
        ObjectUtils.checkEmpty(response, FleaJerseyClientException.class, "ERROR-JERSEY-CLIENT0000000005");

        FleaJerseyResponseData responseData = response.getResponseData();
        // 资源服务请求异常：响应报文为空
        ObjectUtils.checkEmpty(responseData, FleaJerseyClientException.class, "ERROR-JERSEY-CLIENT0000000005");

        ResponsePublicData responsePublicData = responseData.getPublicData();
        // 资源服务请求异常：响应公共报文为空
        ObjectUtils.checkEmpty(responsePublicData, FleaJerseyClientException.class, "ERROR-JERSEY-CLIENT0000000006");

        LOGGER.debug1(obj, "FleaJerseyResponse = \n{}", JABXUtils.toXml(response, true));

        Response<T> responseResult = new Response<>();
        T output = null;

        // 判断资源服务是否请求成功
        if (responsePublicData.isSuccess()) {
            // 获取资源服务响应业务报文
            ResponseBusinessData businessData = responseData.getBusinessData();
            if (ObjectUtils.isNotEmpty(businessData)) {
                output = GsonUtils.toEntity(businessData.getOutput(), clazz);
            }
            // 设置业务出参
            responseResult.setOutput(output);
        } else {
            // 错误码
            responseResult.setRetCode(responsePublicData.getResultCode());
            // 错误信息
            responseResult.setRetMess(responsePublicData.getResultMess());
        }

        LOGGER.debug1(obj, "End");

        return responseResult;
    }

    /**
     * 从请求配置中获取媒体类型
     *
     * @return 媒体类型
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    MediaType toMediaType() throws CommonException {
        // 媒体类型
        String mediaTypeStr = config.getMediaType();
        // 【{0}】未配置，请检查！！！
        StringUtils.checkBlank(mediaTypeStr, FleaJerseyClientException.class, "ERROR-JERSEY-CLIENT0000000008", RequestConfigEnum.MEDIA_TYPE.getKey());

        MediaType mediaType = null;

        try {
            mediaType = MediaType.valueOf(mediaTypeStr);
        } catch (Exception e) {
            LOGGER.error1(new Object() {}, "Exception = {}", e);
            // 请检查客户端配置【client_code = {0}】: 【{1} = {2}】非法
            ExceptionUtils.throwCommonException(FleaJerseyClientException.class, "ERROR-JERSEY-CLIENT0000000010", config.getClientCode(),
                    RequestConfigEnum.MEDIA_TYPE.getKey(), mediaTypeStr);
        }
        return mediaType;
    }

    /**
     * 将请求报文 转换为 XML字符串
     *
     * @param request 请求报文对象
     * @return 请求XML字符串
     * @since 1.0.0
     */
    String toRequestData(FleaJerseyRequest request) {
        try {
            String input = request.getRequestData().getBusinessData().getInput();
            if (ObjectUtils.isNotEmpty(input)) {
                input = URLEncoder.encode(input, StandardCharsets.UTF_8.displayName());
            }
            request.getRequestData().getBusinessData().setInput(input); // 重新设置入参
            // 将请求报文转换成xml
            return JABXUtils.toXml(request, false);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error1(new Object() {}, "Exception = {}", e);
        }
        return null;
    }

    /**
     * 请求方式初始化，由子类实现，用于自定义初始化逻辑
     *
     * @since 1.0.0
     */
    protected abstract void init();

    /**
     * 实际请求处理，由子类实现具体的请求操作
     *
     * @param target  WebTarget对象
     * @param request Flea Jersey请求对象
     * @return Flea Jersey响应对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    protected abstract FleaJerseyResponse request(WebTarget target, FleaJerseyRequest request) throws CommonException;

    public RequestConfig getConfig() {
        return config;
    }

    public void setConfig(RequestConfig config) {
        this.config = config;
    }

    @Override
    public RequestModeEnum getRequestMode() {
        return modeEnum;
    }

    /**
     * 创建FleaJerseyRequest对象
     *
     * @param resourceCode 资源编码
     * @param serviceCode  服务编码
     * @param input        业务入参
     * @return FleaJerseyRequest对象
     * @since 1.0.0
     */
    private FleaJerseyRequest createFleaJerseyRequest(String resourceCode, String serviceCode, Object input) {
        FleaJerseyRequest request = new FleaJerseyRequest();
        FleaJerseyRequestData requestData = new FleaJerseyRequestData();
        // 创建请求公共报文
        RequestPublicData publicData = createRequestPublicData(resourceCode, serviceCode);
        // 创建请求业务报文
        RequestBusinessData businessData = createRequestBusinessData(input);
        requestData.setPublicData(publicData);
        requestData.setBusinessData(businessData);
        request.setRequestData(requestData);
        return request;
    }

    /**
     * 创建请求公共报文
     *
     * @param resourceCode 资源编码
     * @param serviceCode  服务编码
     * @return 请求公共报文
     * @since 1.0.0
     */
    private static RequestPublicData createRequestPublicData(String resourceCode, String serviceCode) {
        RequestPublicData publicData = new RequestPublicData();
        // 当前客户端的系统账户编号
        publicData.setSystemAccountId(FleaJerseyConfig.getSystemAccountId(String.class));
        // 当前操作的账户编号
        publicData.setAccountId(StringUtils.valueOf(FleaSessionManager.getAccountId()));
        publicData.setResourceCode(resourceCode);
        publicData.setServiceCode(serviceCode);
        return publicData;
    }

    /**
     * 创建请求业务报文
     *
     * @param input 业务入参
     * @return 请求业务报文
     * @since 1.0.0
     */
    private static RequestBusinessData createRequestBusinessData(Object input) {
        RequestBusinessData businessData = new RequestBusinessData();
        String inputJson = GsonUtils.toJsonString(input);
        businessData.setInput(inputJson);
        return businessData;
    }
}
