package com.huazie.fleaframework.core.request;

import com.huazie.fleaframework.common.slf4j.FleaLogger;
import com.huazie.fleaframework.common.slf4j.impl.FleaLoggerProxy;
import com.huazie.fleaframework.common.util.ObjectUtils;
import com.huazie.fleaframework.common.util.StringUtils;
import com.huazie.fleaframework.common.util.xml.XmlDigesterHelper;
import com.huazie.fleaframework.core.common.FleaCoreConstants;
import com.huazie.fleaframework.core.request.config.FilterTask;
import com.huazie.fleaframework.core.request.config.FilterTaskChain;
import com.huazie.fleaframework.core.request.config.FilterTasks;
import com.huazie.fleaframework.core.request.config.FleaRequest;
import com.huazie.fleaframework.core.request.config.FleaRequestFilter;
import com.huazie.fleaframework.core.request.config.FleaSession;
import com.huazie.fleaframework.core.request.config.FleaUrl;
import com.huazie.fleaframework.core.request.config.Property;
import com.huazie.fleaframework.core.request.config.RedirectUrl;
import com.huazie.fleaframework.core.request.config.UrlPrefix;
import org.apache.commons.digester.Digester;

/**
 * Flea 请求配置文件XML解析类，用于解析 <b>flea-request.xml</b>
 * 和 <b>flea-request-filter.xml</b> 配置文件。
 *
 * @author huazie
 * @version 1.0.0
 * @since 1.0.0
 */
public class FleaRequestXmlDigesterHelper {

    private static final FleaLogger LOGGER = FleaLoggerProxy.getProxyInstance(FleaRequestXmlDigesterHelper.class);

    private static volatile FleaRequestXmlDigesterHelper xmlDigester;

    private static final Object fleaRequestInitLock = new Object();
    private static final Object fleaRequestFilterInitLock = new Object();

    private static FleaRequest fleaRequest;
    private static FleaRequestFilter fleaRequestFilter;

    /**
     * <p> 只允许通过getInstance()获取 XML解析类 </p>
     */
    private FleaRequestXmlDigesterHelper() {
    }

    /**
     * <p> 获取XML解析工具类 </p>
     *
     * @return XML解析工具类对象
     * @since 1.0.0
     */
    public static FleaRequestXmlDigesterHelper getInstance() {
        if (ObjectUtils.isEmpty(xmlDigester)) {
            synchronized (FleaRequestXmlDigesterHelper.class) {
                if (ObjectUtils.isEmpty(xmlDigester)) {
                    xmlDigester = new FleaRequestXmlDigesterHelper();
                }
            }
        }
        return xmlDigester;
    }

    /**
     * <p> 获取Flea请求 </p>
     *
     * @return Flea请求
     * @since 1.0.0
     */
    public FleaRequest getFleaRequest() {
        if (ObjectUtils.isEmpty(fleaRequest)) {
            synchronized (fleaRequestInitLock) {
                if (ObjectUtils.isEmpty(fleaRequest)) {
                    fleaRequest = newFleaRequest();
                }
            }
        }
        return fleaRequest;
    }

    private FleaRequest newFleaRequest() {

        String fileName = FleaCoreConstants.FleaRequestConfigConstants.FLEA_REQUEST_FILE_NAME;
        if (StringUtils.isNotBlank(System.getProperty(FleaCoreConstants.FleaRequestConfigConstants.FLEA_REQUEST_FILE_SYSTEM_KEY))) {
            fileName = StringUtils.trim(System.getProperty(FleaCoreConstants.FleaRequestConfigConstants.FLEA_REQUEST_FILE_SYSTEM_KEY));
            LOGGER.debug("Use the specified flea-request.xml : " + fileName);
        }

        LOGGER.debug("Use the current flea-request.xml : " + fileName);
        LOGGER.debug("Start to parse the flea-request.xml");

        Digester digester = newFleaRequestFileDigester();
        FleaRequest obj = XmlDigesterHelper.parse(fileName, digester, FleaRequest.class);

        LOGGER.debug("End to parse the flea-request.xml");

        return obj;
    }

    /**
     * <p> 解析flea-request.xml的Digester对象 </p>
     *
     * @return Digester对象
     * @since 1.0.0
     */
    private Digester newFleaRequestFileDigester() {
        Digester digester = new Digester();
        digester.setValidating(false);

        digester.addObjectCreate("flea-request", FleaRequest.class.getName());
        digester.addSetProperties("flea-request");

        // flea session
        digester.addObjectCreate("flea-request/flea-session", FleaSession.class.getName());
        digester.addSetProperties("flea-request/flea-session");

        digester.addSetNext("flea-request/flea-session", "setFleaSession", FleaSession.class.getName());
        digester.addCallMethod("flea-request/flea-session/user-session-key", "setUserSessionKey", 0);
        digester.addCallMethod("flea-request/flea-session/idle-time", "setIdleTime", 0);

        // flea url
        digester.addObjectCreate("flea-request/flea-url", FleaUrl.class.getName());
        digester.addSetProperties("flea-request/flea-url");

        digester.addObjectCreate("flea-request/flea-url/redirect-url", RedirectUrl.class.getName());
        digester.addSetProperties("flea-request/flea-url/redirect-url");

        digester.addObjectCreate("flea-request/flea-url/redirect-url/property", Property.class.getName());
        digester.addSetProperties("flea-request/flea-url/redirect-url/property");
        digester.addBeanPropertySetter("flea-request/flea-url/redirect-url/property", "value");


        digester.addObjectCreate("flea-request/flea-url/url-prefix", UrlPrefix.class.getName());
        digester.addSetProperties("flea-request/flea-url/url-prefix");

        digester.addObjectCreate("flea-request/flea-url/url-prefix/property", Property.class.getName());
        digester.addSetProperties("flea-request/flea-url/url-prefix/property");
        digester.addBeanPropertySetter("flea-request/flea-url/url-prefix/property", "value");

        digester.addSetNext("flea-request/flea-url", "setFleaUrl", FleaUrl.class.getName());

        digester.addSetNext("flea-request/flea-url/redirect-url", "setRedirectUrl", RedirectUrl.class.getName());
        digester.addSetNext("flea-request/flea-url/redirect-url/property", "addProperty", Property.class.getName());

        digester.addCallMethod("flea-request/flea-url/uncheck-urls/uncheck-url", "addUnCheckUrl", 0);

        digester.addCallMethod("flea-request/flea-url/check-urls/check-url", "addCheckUrl", 0);

        digester.addSetNext("flea-request/flea-url/url-prefix", "setUrlPrefix", UrlPrefix.class.getName());
        digester.addSetNext("flea-request/flea-url/url-prefix/property", "addProperty", Property.class.getName());

        digester.addCallMethod("flea-request/flea-url/url-illegal-char", "setUrlIllegalChar", 0);
        return digester;
    }

    /**
     * <p> 获取Flea请求过滤器 </p>
     *
     * @return Flea请求过滤器
     * @since 1.0.0
     */
    public FleaRequestFilter getFleaRequestFilter() {
        if (ObjectUtils.isEmpty(fleaRequestFilter)) {
            synchronized (fleaRequestFilterInitLock) {
                if (ObjectUtils.isEmpty(fleaRequestFilter)) {
                    fleaRequestFilter = newFleaRequestFilter();
                }
            }
        }
        return fleaRequestFilter;
    }

    private FleaRequestFilter newFleaRequestFilter() {

        String fileName = FleaCoreConstants.FleaRequestConfigConstants.FLEA_REQUEST_FILTER_FILE_NAME;
        if (StringUtils.isNotBlank(System.getProperty(FleaCoreConstants.FleaRequestConfigConstants.FLEA_REQUEST_FILTER_FILE_SYSTEM_KEY))) {
            fileName = StringUtils.trim(System.getProperty(FleaCoreConstants.FleaRequestConfigConstants.FLEA_REQUEST_FILTER_FILE_SYSTEM_KEY));
            LOGGER.debug("Use the specified flea-request-filter.xml : " + fileName);
        }

        LOGGER.debug("Use the current flea-request-filter.xml : " + fileName);
        LOGGER.debug("Start to parse the flea-request-filter.xml");

        Digester digester = newFleaRequestFilterFileDigester();
        FleaRequestFilter obj = XmlDigesterHelper.parse(fileName, digester, FleaRequestFilter.class);

        LOGGER.debug("End to parse the flea-request-filter.xml");

        return obj;
    }

    /**
     * <p> 解析flea-request-filter.xml的Digester对象 </p>
     *
     * @return Digester对象
     * @since 1.0.0
     */
    private Digester newFleaRequestFilterFileDigester() {
        Digester digester = new Digester();
        digester.setValidating(false);

        digester.addObjectCreate("flea-request-filter", FleaRequestFilter.class.getName());
        digester.addSetProperties("flea-request-filter");

        // filter-task-chain
        digester.addObjectCreate("flea-request-filter/filter-task-chain", FilterTaskChain.class.getName());
        digester.addSetProperties("flea-request-filter/filter-task-chain");

        // filter-tasks
        digester.addObjectCreate("flea-request-filter/filter-task-chain/filter-tasks", FilterTasks.class.getName());
        digester.addSetProperties("flea-request-filter/filter-task-chain/filter-tasks");

        // filter-task
        digester.addObjectCreate("flea-request-filter/filter-task-chain/filter-tasks/filter-task", FilterTask.class.getName());
        digester.addSetProperties("flea-request-filter/filter-task-chain/filter-tasks/filter-task");

        digester.addSetNext("flea-request-filter/filter-task-chain", "setFilterTaskChain", FilterTaskChain.class.getName());
        digester.addSetNext("flea-request-filter/filter-task-chain/filter-tasks", "setFilterTasks", FilterTasks.class.getName());
        digester.addSetNext("flea-request-filter/filter-task-chain/filter-tasks/filter-task", "addFilterTask", FilterTask.class.getName());

        return digester;
    }

}
