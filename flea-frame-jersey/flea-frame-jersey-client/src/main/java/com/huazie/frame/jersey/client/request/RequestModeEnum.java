package com.huazie.frame.jersey.client.request;

/**
 * <p> 请求方式枚举 </p>
 *
 * @author huazie
 * @version 1.0.0
 * @since 1.0.0
 */
public enum RequestModeEnum {

    GET("get", "com.huazie.frame.jersey.client.request.impl.GetFleaRequest", "Get请求"),
    POST("post", "com.huazie.frame.jersey.client.request.impl.PostFleaRequest", "Post请求"),
    PUT("put", "com.huazie.frame.jersey.client.request.impl.PutFleaRequest", "Put请求"),
    DELETE("delete", "com.huazie.frame.jersey.client.request.impl.DeleteFleaRequest", "Delete请求");

    private String mode; // 请求方式

    private String implClass; // 请求实现类

    private String desc; // 请求方式描述

    RequestModeEnum(String mode, String implClass, String desc) {
        this.mode = mode;
        this.implClass = implClass;
        this.desc = desc;
    }

    public String getMode() {
        return mode;
    }

    public String getImplClass() {
        return implClass;
    }

    public String getDesc() {
        return desc;
    }
}
