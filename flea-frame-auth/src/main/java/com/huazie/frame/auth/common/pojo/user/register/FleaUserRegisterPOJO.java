package com.huazie.frame.auth.common.pojo.user.register;

import com.huazie.frame.auth.common.pojo.account.FleaAccountPOJO;
import com.huazie.frame.auth.common.pojo.account.attr.FleaAccountAttrPOJO;
import com.huazie.frame.auth.common.pojo.user.FleaUserPOJO;
import com.huazie.frame.auth.common.pojo.user.attr.FleaUserAttrPOJO;
import com.huazie.frame.auth.common.pojo.user.login.FleaUserLoginPOJO;
import com.huazie.frame.common.util.CollectionUtils;
import com.huazie.frame.common.util.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Date;
import java.util.List;

/**
 * <p> Flea用户注册信息 </p>
 *
 * @author huazie
 * @version 1.0.0
 * @since 1.0.0
 */
public class FleaUserRegisterPOJO extends FleaUserLoginPOJO {

    private static final long serialVersionUID = -2040583042633897645L;

    private Long groupId; // 用户组编号

    private Integer state; // 状态（0：删除，1：正常 ，2：禁用，3：待审核）

    private Date effectiveDate; // 生效时间

    private Date expiryDate; // 失效时间

    private String remarks; // 备注

    private List<FleaUserAttrPOJO> userAttrList; // Flea用户属性POJO类List集合

    private List<FleaAccountAttrPOJO> accountAttrList; // Flea账户属性POJO类List集合

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
        // Flea用户扩展属性POJO添加备注
        if (CollectionUtils.isNotEmpty(userAttrList)) {
            for (FleaUserAttrPOJO fleaUserAttrInfo : userAttrList) {
                if (ObjectUtils.isNotEmpty(fleaUserAttrInfo)) {
                    fleaUserAttrInfo.setRemarks(remarks);
                }
            }
        }
        // Flea账户扩展属性POJO添加备注
        if (CollectionUtils.isNotEmpty(accountAttrList)) {
            for (FleaAccountAttrPOJO fleaAccountAttrInfo : accountAttrList) {
                if (ObjectUtils.isNotEmpty(fleaAccountAttrInfo)) {
                    fleaAccountAttrInfo.setRemarks(remarks);
                }
            }
        }
    }

    public List<FleaUserAttrPOJO> getUserAttrList() {
        return userAttrList;
    }

    public void setUserAttrList(List<FleaUserAttrPOJO> userAttrList) {
        this.userAttrList = userAttrList;
    }

    /**
     * <p> Flea用户属性POJO类设置用户编号 </p>
     *
     * @param userId 用户编号
     * @since 1.0.0
     */
    public void setUserId(Long userId) {
        if (CollectionUtils.isNotEmpty(userAttrList)) {
            for (FleaUserAttrPOJO fleaUserAttrInfo : userAttrList) {
                if (ObjectUtils.isNotEmpty(fleaUserAttrInfo)) {
                    fleaUserAttrInfo.setUserId(userId);
                }
            }
        }
    }

    public List<FleaAccountAttrPOJO> getAccountAttrList() {
        return accountAttrList;
    }

    public void setAccountAttrList(List<FleaAccountAttrPOJO> accountAttrList) {
        this.accountAttrList = accountAttrList;
    }

    /**
     * <p> Flea用户属性POJO类设置用户编号 </p>
     *
     * @param accountId 用户编号
     * @since 1.0.0
     */
    public void setAccountId(Long accountId) {
        if (CollectionUtils.isNotEmpty(accountAttrList)) {
            for (FleaAccountAttrPOJO fleaAccountAttrInfo : accountAttrList) {
                if (ObjectUtils.isNotEmpty(fleaAccountAttrInfo)) {
                    fleaAccountAttrInfo.setAccountId(accountId);
                }
            }
        }
    }

    /**
     * <p> 获取Flea用户POJO类 </p>
     *
     * @return Flea用户POJO类实例
     * @since 1.0.0
     */
    public FleaUserPOJO newFleaUserPOJO() {
        FleaUserPOJO fleaUserPOJO = new FleaUserPOJO();
        fleaUserPOJO.setGroupId(groupId);
        fleaUserPOJO.setUserName(getAccountCode());
        fleaUserPOJO.setUserState(state);
        fleaUserPOJO.setEffectiveDate(effectiveDate);
        fleaUserPOJO.setExpiryDate(expiryDate);
        fleaUserPOJO.setRemarks(remarks);
        return fleaUserPOJO;
    }

    /**
     * <p> 获取Flea账户POJO类 </p>
     *
     * @param userId 用户编号
     * @return Flea账户POJO类实例
     * @since 1.0.0
     */
    public FleaAccountPOJO newFleaAccountPOJO(Long userId) {
        FleaAccountPOJO fleaAccountPOJO = new FleaAccountPOJO();
        fleaAccountPOJO.setUserId(userId);
        fleaAccountPOJO.setAccountCode(getAccountCode());
        fleaAccountPOJO.setAccountPwd(getAccountPwd());
        fleaAccountPOJO.setAccountState(state);
        fleaAccountPOJO.setEffectiveDate(effectiveDate);
        fleaAccountPOJO.setExpiryDate(expiryDate);
        fleaAccountPOJO.setRemarks(remarks);
        return fleaAccountPOJO;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
