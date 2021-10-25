package com.huazie.frame.db.common.lib.split.config;

import com.huazie.frame.common.exception.CommonException;
import com.huazie.frame.common.slf4j.FleaLogger;
import com.huazie.frame.common.slf4j.impl.FleaLoggerProxy;
import com.huazie.frame.common.util.ObjectUtils;
import com.huazie.frame.db.common.DBXmlDigesterHelper;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 分库配置类，参考 分库配置文件 flea-lib-split.xml
 *
 * @author huazie
 * @version 1.1.0
 * @since 1.1.0
 */
public class LibSplitConfig {

    private static final FleaLogger LOGGER = FleaLoggerProxy.getProxyInstance(LibSplitConfig.class);

    private static volatile LibSplitConfig config;

    private Libs libs;    // 分库配置集合定义类

    private LibSplitConfig() throws CommonException {
        this.libs = DBXmlDigesterHelper.getInstance().getLibs();
    }

    /**
     * 获取分表配置类实例对象
     *
     * @return 分表配置类实例对象
     * @since 1.0.0
     */
    public static LibSplitConfig getConfig() {
        if (ObjectUtils.isEmpty(config)) {
            synchronized (LibSplitConfig.class) {
                if (ObjectUtils.isEmpty(config)) {
                    try {
                        config = new LibSplitConfig();
                    } catch (Exception e) {
                        if (LOGGER.isErrorEnabled()) {
                            LOGGER.error("Fail to init flea-lib-split.xml");
                        }
                    }
                }
            }
        }
        return config;
    }

    /**
     * 根据name获取指定的分库配置定义
     *
     * @param name 模板库名
     * @return 分库配置定义
     * @since 1.1.0
     */
    public Lib getLib(String name) {
        return libs.toLibMap().get(name);
    }

    public Libs getLibs() {
        return libs;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
