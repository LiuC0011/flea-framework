package com.huazie.frame.auth.user;

import com.huazie.frame.auth.base.user.entity.FleaAccount;
import com.huazie.frame.auth.base.user.entity.FleaUserRel;
import com.huazie.frame.auth.base.user.service.interfaces.IFleaUserGroupRelSV;
import com.huazie.frame.auth.base.user.service.interfaces.IFleaUserRelSV;
import com.huazie.frame.auth.common.AuthRelTypeEnum;
import com.huazie.frame.auth.common.UserStateEnum;
import com.huazie.frame.auth.common.pojo.account.attr.FleaAccountAttrPOJO;
import com.huazie.frame.auth.common.pojo.user.attr.FleaUserAttrPOJO;
import com.huazie.frame.auth.common.pojo.user.login.FleaUserLoginPOJO;
import com.huazie.frame.auth.common.pojo.user.register.FleaUserRegisterPOJO;
import com.huazie.frame.auth.common.service.interfaces.IFleaAuthSV;
import com.huazie.frame.auth.common.service.interfaces.IFleaUserLoginSV;
import com.huazie.frame.auth.common.service.interfaces.IFleaUserRegisterSV;
import com.huazie.frame.common.EntityStateEnum;
import com.huazie.frame.common.exception.CommonException;
import com.huazie.frame.common.util.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * <p></p>
 *
 * @author huazie
 * @version 1.0.0
 * @since 1.0.0
 */
public class UserAuthTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserAuthTest.class);

    private ApplicationContext applicationContext;

    @Before
    public void init() {
        applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        LOGGER.debug("ApplicationContext={}", applicationContext);
    }

    @Test
    public void userLoginTest() {

        FleaUserLoginPOJO fleaUserLoginInfo = new FleaUserLoginPOJO();
        fleaUserLoginInfo.setAccountCode("13218010892");
        fleaUserLoginInfo.setAccountPwd("123qwe");

        IFleaUserLoginSV fleaUserLoginSV = (IFleaUserLoginSV) applicationContext.getBean("fleaUserLoginSV");

        try {
            FleaAccount fleaAccount = fleaUserLoginSV.login(fleaUserLoginInfo);
            LOGGER.debug("FleaAccount = {}", fleaAccount);
        } catch (CommonException e) {
            LOGGER.error("Exception = ", e);
        }

    }

    @Test
    public void userRegisterTest() {

        FleaUserRegisterPOJO fleaUserRegisterPOJO = new FleaUserRegisterPOJO();

        fleaUserRegisterPOJO.setAccountCode("13218010892");
        fleaUserRegisterPOJO.setAccountPwd("123qwe");
        fleaUserRegisterPOJO.setState(UserStateEnum.IN_USE.getState());

        // 添加用户属性
        List<FleaUserAttrPOJO> fleaUserAttrInfoList = new ArrayList<FleaUserAttrPOJO>();

        FleaUserAttrPOJO fleaUserAttrInfo1 = new FleaUserAttrPOJO();
        fleaUserAttrInfo1.setAttrCode("USER_TEST1");
        fleaUserAttrInfo1.setAttrValue("11111");
        fleaUserAttrInfoList.add(fleaUserAttrInfo1);

        FleaUserAttrPOJO fleaUserAttrInfo2 = new FleaUserAttrPOJO();
        fleaUserAttrInfo2.setAttrCode("USER_TEST2");
        fleaUserAttrInfo2.setAttrValue("22222");
        fleaUserAttrInfoList.add(fleaUserAttrInfo2);

        // 添加账户属性
        List<FleaAccountAttrPOJO> fleaAccountAttrInfoList = new ArrayList<FleaAccountAttrPOJO>();

        FleaAccountAttrPOJO fleaAccountAttrInfo1 = new FleaAccountAttrPOJO();
        fleaAccountAttrInfo1.setAttrCode("ACCOUNT_TEST1");
        fleaAccountAttrInfo1.setAttrValue("11111");
        fleaAccountAttrInfoList.add(fleaAccountAttrInfo1);

        FleaAccountAttrPOJO fleaAccountAttrInfo2 = new FleaAccountAttrPOJO();
        fleaAccountAttrInfo2.setAttrCode("ACCOUNT_TEST2");
        fleaAccountAttrInfo2.setAttrValue("22222");
        fleaAccountAttrInfoList.add(fleaAccountAttrInfo2);

        fleaUserRegisterPOJO.setUserAttrList(fleaUserAttrInfoList);
        fleaUserRegisterPOJO.setAccountAttrList(fleaAccountAttrInfoList);

        fleaUserRegisterPOJO.setRemarks("用户自己注册时新增数据");

        IFleaUserRegisterSV fleaUserRegisterSV = (IFleaUserRegisterSV) applicationContext.getBean("fleaUserRegisterSV");

        try {
            FleaAccount fleaAccount = fleaUserRegisterSV.register(fleaUserRegisterPOJO);
            LOGGER.debug("FleaAccount = {}", fleaAccount);
        } catch (CommonException e) {
            LOGGER.error("Exception = ", e);
        }
    }

    @Test
    public void systemUserRegisterTest() {
        FleaUserRegisterPOJO fleaUserRegisterPOJO = new FleaUserRegisterPOJO();

        fleaUserRegisterPOJO.setSystemId(1000L);
        fleaUserRegisterPOJO.setAccountCode("SYS_FLEA_MGMT");
        fleaUserRegisterPOJO.setAccountPwd("123456qwertyASDFGH");
        fleaUserRegisterPOJO.setState(UserStateEnum.IN_USE.getState());

        // 添加用户属性
        List<FleaUserAttrPOJO> fleaUserAttrInfoList = new ArrayList<FleaUserAttrPOJO>();

        FleaUserAttrPOJO fleaUserAttrInfo1 = new FleaUserAttrPOJO();
        fleaUserAttrInfo1.setAttrCode("USER_TYPE");
        fleaUserAttrInfo1.setAttrValue("SYSTEM");
        fleaUserAttrInfoList.add(fleaUserAttrInfo1);

        // 添加账户属性
        List<FleaAccountAttrPOJO> fleaAccountAttrInfoList = new ArrayList<FleaAccountAttrPOJO>();

        FleaAccountAttrPOJO fleaAccountAttrInfo1 = new FleaAccountAttrPOJO();
        fleaAccountAttrInfo1.setAttrCode("ACCOUNT_TYPE");
        fleaAccountAttrInfo1.setAttrValue("SYSTEM");
        fleaAccountAttrInfoList.add(fleaAccountAttrInfo1);

        fleaUserRegisterPOJO.setUserAttrList(fleaUserAttrInfoList);
        fleaUserRegisterPOJO.setAccountAttrList(fleaAccountAttrInfoList);

        fleaUserRegisterPOJO.setRemarks("【跳蚤管家】");

        IFleaUserRegisterSV fleaUserRegisterSV = (IFleaUserRegisterSV) applicationContext.getBean("fleaUserRegisterSV");

        try {
            FleaAccount fleaAccount = fleaUserRegisterSV.register(fleaUserRegisterPOJO);
            LOGGER.debug("FleaAccount = {}", fleaAccount);
        } catch (CommonException e) {
            LOGGER.error("Exception = ", e);
        }
    }

    @Test
    public void testSaveQuitLog() {
        IFleaAuthSV fleaUserLoginSV = (IFleaAuthSV) applicationContext.getBean("fleaAuthSV");
        fleaUserLoginSV.saveQuitLog(1L);
    }

    @Test
    public void testInsertUserRel() {

        FleaUserRel fleaUserRel = new FleaUserRel();
        fleaUserRel.setUserId(1L);
        fleaUserRel.setRelId(1L);
        fleaUserRel.setRelType(AuthRelTypeEnum.USER_REL_ROLE.getRelType());
        fleaUserRel.setRelState(EntityStateEnum.IN_USE.getState());
        fleaUserRel.setCreateDate(DateUtils.getCurrentTime());
        fleaUserRel.setRemarks("【13218010892】用户绑定【超级管理员】角色");

        try {
            IFleaUserRelSV fleaUserRelSV = (IFleaUserRelSV) applicationContext.getBean("fleaUserRelSV");
            fleaUserRelSV.save(fleaUserRel);
        } catch (CommonException e) {
            LOGGER.error("Exception = ", e);
        }
    }

    @Test
    public void testQueryUserGroupRel() {

        try {
            IFleaUserGroupRelSV fleaUserGroupRelSV = (IFleaUserGroupRelSV) applicationContext.getBean("fleaUserGroupRelSV");
            fleaUserGroupRelSV.getUserGroupRelList(1L, AuthRelTypeEnum.USER_GROUP_REL_ROLE.getRelType());
        } catch (CommonException e) {
            LOGGER.error("Exception = ", e);
        }
    }

    @Test
    public void testQueryUserRel() {

        try {
            IFleaUserRelSV fleaUserRelSV = (IFleaUserRelSV) applicationContext.getBean("fleaUserRelSV");
            fleaUserRelSV.getUserRelList(1L, AuthRelTypeEnum.USER_REL_ROLE.getRelType());
        } catch (CommonException e) {
            LOGGER.error("Exception = ", e);
        }
    }

}
