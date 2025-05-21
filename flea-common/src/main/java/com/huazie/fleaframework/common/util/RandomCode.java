package com.huazie.fleaframework.common.util;

import com.huazie.fleaframework.common.CommonConstants;
import com.huazie.fleaframework.common.PinyinEnum;
import com.huazie.fleaframework.common.slf4j.FleaLogger;
import com.huazie.fleaframework.common.slf4j.impl.FleaLoggerProxy;

import java.util.Random;
import java.util.UUID;

/**
 * 随机码生成公共类
 *
 * @author huazie
 * @version 1.0.0
 * @since 1.0.0
 */
public class RandomCode {

    private static final FleaLogger LOGGER = FleaLoggerProxy.getProxyInstance(RandomCode.class);

    // 创建随机数对象
    private static Random random = new Random();

    private static final int LOWER_A_ASCII_VALUE = 97;

    private static final int UPPER_A_ASCII_VALUE = 65;

    private static final int LETTER_NUM = 26;

    private static final int NUMBER_TEN = 10;

    private RandomCode() {
    }

    /**
     * 产生随机的数字，位数由len控制,以字符串形式返回
     *
     * @param len 随机数字长度
     * @return 指定长度的随机数字
     * @since 1.0.0
     */
    public static String toNumberCode(int len) {
        // 随机产生认证码(len位数字)
        StringBuilder sNumberCode = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sNumberCode.append(random.nextInt(NUMBER_TEN));
        }
        LOGGER.debug1(new Object() {}, "NumberCode = {}", sNumberCode);
        return sNumberCode.toString();
    }

    /**
     * 产生随机的小写字母，位数由len控制。以连续的字符串形式返回
     *
     * @param len 随机字母长度
     * @return 指定定长度的字母字符串
     * @since 1.0.0
     */
    public static String toLowerLetterCode(int len) {
        return toLetterCode(len, PinyinEnum.LOWER_CASE);
    }

    /**
     * 产生随机的大写字母，位数由len控制。以连续的字符串形式返回
     *
     * @param len 随机字母长度
     * @return 指定长度的字母字符串
     * @since 1.0.0
     */
    public static String toUpperLetterCode(int len) {
        return toLetterCode(len, PinyinEnum.UPPER_CASE);
    }

    /**
     * 产生随机的大小写字母，位数由len控制。以连续的字符串形式返回
     *
     * @param len 随机字母长度
     * @return 指定长度的字母字符串
     * @since 1.0.0
     */
    public static String toRandomLetterCode(int len) {
        return toLetterCode(len, null);
    }

    /**
     * 产生随机的大写字母，位数由len控制。以连续的字符串形式返回
     *
     * @param len   随机字母长度
     * @param pEnum 大小写标识
     * @return 指定长度的字母字符串
     * @since 1.0.0
     */
    private static String toLetterCode(int len, PinyinEnum pEnum) {
        StringBuilder sLetterCode = new StringBuilder();
        for (int i = 0; i < len; i++) {
            char letter;
            if (null != pEnum) {
                if (PinyinEnum.UPPER_CASE.getType() == pEnum.getType()) {
                    letter = toUpperSingleLetter();
                } else if (PinyinEnum.LOWER_CASE.getType() == pEnum.getType()) {
                    letter = toLowerSingleLetter();
                } else {
                    letter = toRandomSingleLetter();
                }
            } else {
                letter = toRandomSingleLetter();
            }
            sLetterCode.append(letter);
        }
        LOGGER.debug1(new Object() {}, "LetterCode = {}", sLetterCode);
        return sLetterCode.toString();
    }

    /**
     * 随机取单个大小写字母
     *
     * @return 单个字母
     * @since 1.0.0
     */
    public static char toRandomSingleLetter() {
        int n = random.nextInt(NUMBER_TEN);
        if (n % 2 == 0) {
            return toUpperSingleLetter();
        } else {
            return toLowerSingleLetter();
        }
    }

    /**
     * 单个大写字母
     *
     * @return 单个大写字母
     * @since 1.0.0
     */
    public static char toUpperSingleLetter() {
        return (char) (random.nextInt(LETTER_NUM) + UPPER_A_ASCII_VALUE);
    }

    /**
     * 单个小写字母
     *
     * @return 单个小写字母
     * @since 1.0.0
     */
    public static char toLowerSingleLetter() {
        return (char) (random.nextInt(LETTER_NUM) + LOWER_A_ASCII_VALUE);
    }

    /**
     * 生成UUID值
     *
     * @return 去除"-"的UUID值
     * @since 1.0.0
     */
    public static String toUUID() {
        String sUUID = UUID.randomUUID().toString();
        Object obj = new Object() {};
        LOGGER.debug1(obj, "Before UUID = {}", sUUID);
        // 去掉"-"符号
        String sResult = sUUID.replaceAll(CommonConstants.SymbolConstants.HYPHEN, "");
        LOGGER.debug1(obj, "After UUID = {}", sResult);
        return sResult;
    }
}
