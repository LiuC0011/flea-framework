package com.huazie.fleaframework.common.util;

import com.huazie.fleaframework.common.slf4j.FleaLogger;
import com.huazie.fleaframework.common.slf4j.impl.FleaLoggerProxy;
import com.huazie.fleaframework.common.DateFormatEnum;
import org.junit.Test;

import java.util.Date;

/**
 * @author huazie
 * @version 1.0.0
 * @since 1.0.0
 */
public class DateUtilsTest {

    private static final FleaLogger LOGGER = FleaLoggerProxy.getProxyInstance(DateUtilsTest.class);

    @Test
    public void testDate2String() {
        LOGGER.debug(DateUtils.date2String(null, DateFormatEnum.YYMMDDHHMMSS.getFormat()));
        LOGGER.debug(DateUtils.date2String(null, DateFormatEnum.YYYY_MM_DD));
        LOGGER.debug(DateUtils.date2String(null));
    }

    @Test
    public void testString2Date() {
        String dateStr = DateUtils.date2String(null);
        LOGGER.debug("DATE1: {}", DateUtils.string2Date(dateStr, DateFormatEnum.YYYY_MM_DD.getFormat()));
        LOGGER.debug("DATE2: {}", DateUtils.string2Date(dateStr, DateFormatEnum.YYYY_MM));
        LOGGER.debug("DATE3: {}", DateUtils.string2Date(dateStr));
    }

    @Test
    public void testTime() {
        Date date1 = DateUtils.string2Date("20200504180100", DateFormatEnum.YYMMDDHHMMSS.getFormat());
        Date date2 = DateUtils.string2Date("20200504180110", DateFormatEnum.YYMMDDHHMMSS.getFormat());
        assert date2 != null;
        assert date1 != null;
        LOGGER.debug("Time: {}", date2.getTime() - date1.getTime());
    }
}
