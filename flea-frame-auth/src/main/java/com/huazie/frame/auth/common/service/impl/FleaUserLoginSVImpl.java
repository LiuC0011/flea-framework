package com.huazie.frame.auth.common.service.impl;

import com.huazie.frame.auth.base.user.entity.FleaAccount;
import com.huazie.frame.auth.base.user.service.interfaces.IFleaAccountSV;
import com.huazie.frame.auth.common.exception.FleaAuthCommonException;
import com.huazie.frame.auth.common.pojo.login.FleaUserLoginInfo;
import com.huazie.frame.auth.common.service.interfaces.IFleaUserLoginSV;
import com.huazie.frame.common.exception.CommonException;
import com.huazie.frame.common.util.ObjectUtils;
import com.huazie.frame.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * <p> Flea用户登录服务实现类 </p>
 *
 * @author huazie
 * @version 1.0.0
 * @since 1.0.0
 */
@Service("fleaUserLoginSV")
public class FleaUserLoginSVImpl implements IFleaUserLoginSV {

    private final IFleaAccountSV fleaAccountSV; // Flea账户信息服务

    @Autowired
    public FleaUserLoginSVImpl(@Qualifier("fleaAccountSV") IFleaAccountSV fleaAccountSV) {
        this.fleaAccountSV = fleaAccountSV;
    }

    @Override
    public FleaAccount login(FleaUserLoginInfo fleaUserLoginInfo) throws CommonException {

        String accountCode = fleaUserLoginInfo.getAccountCode();
        if (StringUtils.isBlank(accountCode)) {
            // 账号不能为空！
            throw new FleaAuthCommonException("ERROR-AUTH-COMMON0000000001");
        }

        String accountPwd = fleaUserLoginInfo.getAccountPwd();
        if (StringUtils.isBlank(accountPwd)) {
            // 密码不能为空！
            throw new FleaAuthCommonException("ERROR-AUTH-COMMON0000000002");
        }

        FleaAccount fleaAccount = fleaAccountSV.queryAccount(accountCode, accountPwd);

        if (ObjectUtils.isEmpty(fleaAccount)) {
            // 账号或者密码错误！
            throw new FleaAuthCommonException("ERROR-AUTH-COMMON0000000003");
        }

        return fleaAccount;
    }

}
