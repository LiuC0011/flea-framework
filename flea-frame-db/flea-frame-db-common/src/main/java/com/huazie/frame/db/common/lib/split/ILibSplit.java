package com.huazie.frame.db.common.lib.split;

import com.huazie.frame.common.exception.CommonException;

/**
 * 分库转换接口定义
 *
 * @author huazie
 * @version 1.1.0
 * @since 1.1.0
 */
public interface ILibSplit {

    /**
     * <p> 分库转换 </p>
     *
     * @param splitLibObj 分库对象
     * @param count       分库总数
     * @return 分库转换值
     * @throws CommonException 通用异常
     */
    String convert(Object splitLibObj, int count) throws CommonException;
}
