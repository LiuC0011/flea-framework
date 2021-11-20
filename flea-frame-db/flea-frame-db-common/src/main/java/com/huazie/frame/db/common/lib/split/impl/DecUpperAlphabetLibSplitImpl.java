package com.huazie.frame.db.common.lib.split.impl;

import com.huazie.frame.common.exception.CommonException;
import com.huazie.frame.common.util.StringUtils;

/**
 * 十进制大写字母分库转换实现类，即转换后的分库序列键为大写字母，
 * 从A开始，共即 {@code count} 个 。
 *
 * @author huazie
 * @version 1.1.0
 * @since 1.1.0
 */
public final class DecUpperAlphabetLibSplitImpl extends AbstractLibSplitImpl {

    @Override
    public String convert(Object splitLibObj, int count) throws CommonException {
        return StringUtils.valueOf((char)(convertCommon(splitLibObj, INT_HEX, count) + UPPER_A));
    }
}
