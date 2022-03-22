package com.huazie.fleaframework.core.base.cfgdata.service.interfaces;

import com.huazie.fleaframework.common.exception.CommonException;
import com.huazie.fleaframework.core.base.cfgdata.entity.FleaJerseyResService;
import com.huazie.fleaframework.db.jpa.service.interfaces.IAbstractFleaJPASV;

/**
 * Flea Jersey资源服务SV层接口
 *
 * @author huazie
 * @version 1.0.0
 * @since 1.0.0
 */
public interface IFleaJerseyResServiceSV extends IAbstractFleaJPASV<FleaJerseyResService> {

    /**
     * 根据资源编码和服务编码，获取唯一在用的资源服务信息
     *
     * @param resourceCode 资源编码
     * @param serviceCode  服务编码
     * @return 在用的资源服务
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    FleaJerseyResService getResService(String resourceCode, String serviceCode) throws CommonException;

}
