package com.huazie.frame.jersey.server.resource.impl;

import com.huazie.frame.jersey.common.data.FleaJerseyRequest;
import com.huazie.frame.jersey.common.data.FleaJerseyResponse;
import com.huazie.frame.jersey.server.resource.JerseyResource;
import com.huazie.frame.jersey.server.resource.Resource;

/**
 * <p> Flea Jersey资源（包含GET, POST, PUT, DELETE） </p>
 *
 * @author huazie
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class FleaJerseyAllResource extends Resource implements JerseyResource {

    @Override
    public FleaJerseyResponse doGetResource(String requestXml) {
        return doResource(requestXml);
    }

    @Override
    public FleaJerseyResponse doPostResource(FleaJerseyRequest request) {
        return doResource(request);
    }

    @Override
    public FleaJerseyResponse doPutResource(FleaJerseyRequest request) {
        return doResource(request);
    }

    @Override
    public FleaJerseyResponse doDeleteResource(String requestXml) {
        return doResource(requestXml);
    }
}
