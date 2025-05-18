package com.huazie.fleaframework.common.util;

import com.huazie.fleaframework.common.PinyinEnum;
import com.huazie.fleaframework.common.slf4j.FleaLogger;
import com.huazie.fleaframework.common.slf4j.impl.FleaLoggerProxy;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * 汉字转拼音工具类，用于获取汉字对应的简拼或全拼
 * <p> 调用示例：
 * <pre>
 *    PinyinUtils.getJianPin("中国", PinyinEnum.LOWER_CASE.getType());
 *    PinyinUtils.getJianPin("中国", PinyinEnum.UPPER_CASE.getType());
 *    PinyinUtils.getQuanPin("中国", PinyinEnum.LOWER_CASE.getType());
 *    PinyinUtils.getQuanPin("中国", PinyinEnum.UPPER_CASE.getType());
 * </pre>
 *
 * @author huazie
 * @version 1.0.0
 * @since 1.0.0
 */
public class PinyinUtils {

    private static final FleaLogger LOGGER = FleaLoggerProxy.getProxyInstance(PinyinUtils.class);

    private PinyinUtils() {
    }

    /**
     * 获取中文的简拼
     *
     * @param chinese 指定的中文字符
     * @return 中文简拼
     * @since 1.0.0
     */
    public static String getJianPin(String chinese, int caseType) {
        String name = getPinyin(chinese, caseType, PinyinEnum.JIAN_PIN.getType());
        LOGGER.debug1(new Object() {}, "JianPin : {}", name);
        return name;
    }

    /**
     * 获取中文的全拼
     *
     * @param chinese 指定的中文字符
     * @return 中文全拼
     * @since 1.0.0
     */
    public static String getQuanPin(String chinese, int caseType) {
        String name = getPinyin(chinese, caseType, PinyinEnum.QUAN_PIN.getType());
        LOGGER.debug1(new Object() {}, "QuanPin : {}", name);
        return name;
    }

    /**
     * 获取拼音
     *
     * @param chinese          指定的中文字符
     * @param caseType         0：大写 1： 小写
     * @param howManyCharacter 0 ：简拼 1： 全拼
     * @return 获取中文的拼英
     * @since 1.0.0
     */
    public static String getPinyin(String chinese, int caseType, int howManyCharacter) {
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        defaultFormat.setCaseType(PinyinEnum.UPPER_CASE.getType() == caseType ? HanyuPinyinCaseType.UPPERCASE : HanyuPinyinCaseType.LOWERCASE);

        StringBuilder pinyinName = new StringBuilder();
        char[] nameCharArr = chinese.trim().toCharArray();
        for (char nameChar : nameCharArr) {
            if (nameChar > '') {
                try {
                    String[] pinyin = PinyinHelper.toHanyuPinyinStringArray(nameChar, defaultFormat);
                    if ((pinyin != null) && (pinyin.length > 0)) {
                        if (howManyCharacter == PinyinEnum.JIAN_PIN.getType()) {
                            pinyinName.append(pinyin[0].charAt(0));
                        } else if (howManyCharacter == PinyinEnum.QUAN_PIN.getType()) {
                            pinyinName.append(pinyin[0]);
                        }
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    LOGGER.error1(new Object() {},"拼英转换出现异常， Exception = ", e);
                }
            } else {
                pinyinName.append(nameChar);
            }
        }
        return pinyinName.toString();
    }

}
