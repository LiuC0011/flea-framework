package com.huazie.fleaframework.common.util;

import com.huazie.fleaframework.common.slf4j.FleaLogger;
import com.huazie.fleaframework.common.slf4j.impl.FleaLoggerProxy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 数据处理工具类
 *
 * @author huazie
 * @version 1.0.0
 * @since 1.0.0
 */
public class DataHandleUtils {

    private static final FleaLogger LOGGER = FleaLoggerProxy.getProxyInstance(DataHandleUtils.class);

    private DataHandleUtils() {
    }

    /**
     * 十六进制字符串转化为字节数组
     *
     * @param hex 十六进制字符串
     * @return 字节数组
     * @since 1.0.0
     */
    public static byte[] hex2byte(String hex) {
        if (StringUtils.isBlank(hex)) {
            return new byte[0];
        }
        int len = hex.length();
        int byteLen;
        if (len % 2 == 1) {
            byteLen = len / 2 + 1;
        } else {
            byteLen = len / 2;
        }

        byte[] bytes = new byte[byteLen];
        for (int i = 0; i < byteLen; i++) {
            if (i == byteLen - 1) {
                if (len % 2 == 1) {
                    bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 1), 16);
                    continue;
                }
            }
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }

    /**
     * 将字节数组转化为十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     * @since 1.0.0
     */
    public static String byte2hex(byte[] bytes) {
        if (ArrayUtils.isEmpty(bytes)) {
            return null;
        }
        StringBuilder sBuilder = new StringBuilder();
        for (byte tempByte : bytes) {
            String tempHexStr = (java.lang.Integer.toHexString(tempByte & 0XFF));
            if (tempHexStr.length() == 1) {
                sBuilder.append("0");
            }
            sBuilder.append(tempHexStr);
        }
        return sBuilder.toString();
    }

    /**
     * 数据压缩(gzip)
     *
     * @param originalStr 原始字符串
     * @return 压缩后的字符串（Base64编码了）
     * @since 1.0.0
     */
    public static String gzip(String originalStr) {
        if (StringUtils.isBlank(originalStr)) {
            return null;
        }

        Object obj = new Object() {};
        LOGGER.debug1(obj, "[GZIP] Original Length = {}", originalStr.length());

        String compressedStr = null;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
                gzipOutputStream.write(originalStr.getBytes());
            } finally {
                // 获取输出流的数据之前，需要将gzipOutputStream关闭
                compressedStr = new String(Base64Helper.getInstance().encode(byteArrayOutputStream.toByteArray()));
            }
        } catch (Exception e) {
            LOGGER.error1(new Object() {}, "采用gzip方式压缩数据异常：", e);
        }

        if (StringUtils.isNotBlank(compressedStr)) {
            LOGGER.debug1(obj, "Compressed Length = {}", compressedStr.length());
        }

        return compressedStr;
    }

    /**
     * 数据解压(gzip)
     *
     * @param compressedStr 压缩后的字符串
     * @return 原始字符串
     * @since 1.0.0
     */
    public static String unGzip(String compressedStr) {
        if (StringUtils.isBlank(compressedStr)) {
            return null;
        }

        Object obj = new Object() {};
        LOGGER.debug1(obj,"[GZIP] Compressed Length = {}", compressedStr.length());

        String originalStr = null;
        byte[] compressedArr = Base64Helper.getInstance().decode(compressedStr.getBytes());
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressedArr);
             GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream)) {
            originalStr = IOUtils.toString(gzipInputStream, false);
        } catch (Exception e) {
            LOGGER.error1(new Object() {},"采用gzip方式解压数据异常：", e);
        }

        if (StringUtils.isNotBlank(originalStr)) {
            LOGGER.debug1(obj,"Original Length = {}", originalStr.length());
        }

        return originalStr;
    }

    /**
     * 数据压缩(zip)
     *
     * @param originalStr 原始字符串
     * @return 压缩后的字符串（Base64编码）
     * @since 1.0.0
     */
    public static String zip(String originalStr) {
        if (StringUtils.isBlank(originalStr)) {
            return null;
        }

        Object obj = new Object() {};
        LOGGER.debug1(obj,"[ZIP] Original Length = {}", originalStr.length());

        String compressedStr = null;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            // 开始写入新的 ZIP 文件条目并将流定位到条目数据的开始处
            zipOutputStream.putNextEntry(new ZipEntry("zip"));
            zipOutputStream.write(originalStr.getBytes());
            // 关闭当前的 ZIP 条目并定位流以读取下一个条目
            zipOutputStream.closeEntry();

            compressedStr = new String(Base64Helper.getInstance().encode(byteArrayOutputStream.toByteArray()));
        } catch (Exception e) {
            LOGGER.error1(new Object() {},"采用zip方式压缩数据异常：", e);
        }

        if (StringUtils.isNotBlank(compressedStr)) {
            LOGGER.debug1(obj,"Compressed Length = {}", compressedStr.length());
        }

        return compressedStr;
    }

    /**
     * 数据解压(zip)
     *
     * @param compressedStr 压缩后的字符串(Base64编码)
     * @return 原始字符串
     * @since 1.0.0
     */
    public static String unZip(String compressedStr) {
        if (StringUtils.isBlank(compressedStr)) {
            return null;
        }

        Object obj = new Object() {};
        LOGGER.debug1(obj,"[ZIP] Compressed Length = {}", compressedStr.length());

        String originalStr = null;
        byte[] compressedArr = Base64Helper.getInstance().decode(compressedStr.getBytes());
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressedArr);
             ZipInputStream zipInputStream = new ZipInputStream(byteArrayInputStream)) {
            zipInputStream.getNextEntry();
            originalStr = IOUtils.toString(zipInputStream, false);
        } catch (Exception e) {
            LOGGER.error1(new Object() {},"采用zip方式解压数据异常：", e);
        }

        if (StringUtils.isNotBlank(originalStr)) {
            LOGGER.debug1(obj,"Original Length = {}", originalStr.length());
        }

        return originalStr;
    }

}
