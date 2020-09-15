package com.huazie.frame.auth.common.service.interfaces;

import com.huazie.frame.auth.base.function.entity.FleaMenu;
import com.huazie.frame.auth.common.pojo.user.FleaUserModuleData;
import com.huazie.frame.common.IFleaUser;
import com.huazie.frame.common.exception.CommonException;
import com.huazie.frame.common.object.FleaObjectFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * <p> Flea 授权服务接口类 </p>
 *
 * @author huazie
 * @version 1.0.0
 * @since 1.0.0
 */
public interface IFleaAuthSV {

    /**
     * <p> 初始化用户信息 </p>
     *
     * @param userId       用户编号
     * @param acctId       账户编号
     * @param systemAcctId 系统账户编号
     * @param otherAttrs   其他属性
     * @since 1.0.0
     */
    void initUserInfo(Long userId, Long acctId, Long systemAcctId, Map<String, Object> otherAttrs, FleaObjectFactory<IFleaUser> fleaObjectFactory);

    /**
     * <p> 保存用户登录日志 </p>
     *
     * @param accountId 账户编号
     * @param request   HTTP请求对象
     * @since 1.0.0
     */
    void saveLoginLog(Long accountId, HttpServletRequest request);

    /**
     * <p> 保存用户退出日志 </p>
     *
     * @param accountId 账户编号
     * @since 1.0.0
     */
    void saveQuitLog(Long accountId);

    /**
     * <p> 获取所有可以访问的菜单 </p>
     *
     * @param accountId    操作账户编号
     * @param systemAcctId 系统帐户编号
     * @return 所有可以访问的菜单
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    List<FleaMenu> getAllAccessibleMenus(Long accountId, Long systemAcctId) throws CommonException;

    /**
     * <p> 根据账户编号获取用户模块数据 </p>
     *
     * @param accountId 账户编号
     * @return 用户模块数据
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    FleaUserModuleData getFleaUserModuleData(Long accountId) throws CommonException;
}
