package com.huazie.frame.auth.common.service.interfaces;

import com.huazie.frame.auth.base.user.entity.FleaAccount;
import com.huazie.frame.auth.common.pojo.user.login.FleaUserLoginPOJO;
import com.huazie.frame.common.exception.CommonException;

/**
 * <p> Flea 用户登录服务接口类 </p>
 *
 * @author huazie
 * @version 1.0.0
 * @since 1.0.0
 */
public interface IFleaUserLoginSV {

    /**
     * <p> 用户登录验证 </p>
     *
     * @param fleaUserLoginPOJO Flea用户登录信息
     * @return 账户信息
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    FleaAccount login(FleaUserLoginPOJO fleaUserLoginPOJO) throws CommonException;

}
