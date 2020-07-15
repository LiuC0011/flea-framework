package com.huazie.frame.common.util;

import com.huazie.frame.common.CommonConstants;
import com.huazie.frame.common.util.json.FastJsonUtils;
import com.huazie.frame.common.util.json.GsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

/**
 * <p> Http工具类 </p>
 *
 * @author huazie
 * @version 1.0.0
 * @since 1.0.0
 */
public class HttpUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);

    private static final String TAOBAO_IP_URL = "http://ip.taobao.com/service/getIpInfo.php?ip=";

    private static final String SINA_IP_URL = "http://int.dpool.sina.com.cn/iplookup/iplookup.php?format=js&ip=";

    /**
     * <p> 获取Http请求的客户端IP地址 </p>
     * <p> X-Forwarded-For: 简称XFF头，它代表客户端，也就是HTTP的请求端真实的IP，只有在通过了HTTP代理或者负载均衡服务器时才会添加该项 </p>
     *
     * @param request Http请求对象
     * @return ip地址
     * @since 1.0.0
     */
    public static String getIp(HttpServletRequest request) {

        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        //如果是多级代理，那么取第一个ip为客户ip
        if (StringUtils.isBlank(ip) && ip.contains(",")) {
            ip = ip.substring(ip.lastIndexOf(",") + 1).trim();
        }

        if (StringUtils.isBlank(ip) || "null".equals(ip)) {
            ip = "0.0.0.0";
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("HttpUtils#getIp(HttpServletRequest) IP ={}", ip);
        }

        return ip;
    }

    /**
     * <p> 通过TaoBao的接口获取ip所在地理地址 </p>
     *
     * @param ip ip地址
     * @return 较为精确的地理地址
     * @since 1.0.0
     */
    @SuppressWarnings(value = "unchecked")
    public static String getAddressByTaoBao(String ip) {
        StringBuilder sb = new StringBuilder();
        try {
            String urlStr = TAOBAO_IP_URL + ip;
            String retJson = getAddress(urlStr);

            Map<String, Object> map = GsonUtils.toMap(retJson);

            if (ObjectUtils.isEmpty(map)) {
                return "";
            }

            Map<String, Object> dataMap = (Map<String, Object>) map.get("data");

            if (ObjectUtils.isEmpty(dataMap)) {
                return "";
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("HttpUtils#getAddressByTaoBao(String) Map = {}", map);
                LOGGER.debug("HttpUtils#getAddressByTaoBao(String) Data = {}", dataMap);
            }

            sb.append(dataMap.get(CommonConstants.IPAddressConstants.COUNTRY));
            sb.append(dataMap.get(CommonConstants.IPAddressConstants.REGION));
            sb.append(dataMap.get(CommonConstants.IPAddressConstants.CITY));
            Object isp = dataMap.get(CommonConstants.IPAddressConstants.ISP);
            if (ObjectUtils.isNotEmpty(isp)) {
                sb.append("(").append(dataMap.get(CommonConstants.IPAddressConstants.ISP)).append(")");
            }

        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("HttpUtils##getAddressByTaoBao(String) Exception = ", e);
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("HttpUtils##getAddressByTaoBao(String) Address = {}", sb);
        }

        return sb.toString();
    }

    /**
     * <p> 通过Sina的接口获取ip所在地理地址 </p>
     *
     * @param ip ip地址
     * @return 较为精确的地理地址
     * @since 1.0.0
     */
    @SuppressWarnings(value = "unchecked")
    public static String getAddressBySina(String ip) {
        StringBuilder sb = new StringBuilder();
        try {
            String urlStr = SINA_IP_URL + ip;
            String retJson = getAddress(urlStr);

            Map<String, Object> map = FastJsonUtils.toMap(retJson);

            if (ObjectUtils.isEmpty(map)) {
                return "";
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("HttpUtils##getAddressBySina() Map={}", map);
            }

            sb.append(map.get(CommonConstants.IPAddressConstants.COUNTRY))
                    .append(map.get(CommonConstants.IPAddressConstants.PROVINCE) + "\u7701")
                    .append(map.get(CommonConstants.IPAddressConstants.CITY));

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("HttpUtils##getAddressBySina() Address={}", sb);
            }
            
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("HttpUtils##getAddressBySina() Exception=", e);
            }
        }

        return sb.toString();
    }

    /**
     * <p> 获取ip所在地理地址相关信息 </p>
     *
     * @param urlStr url地址
     * @return ip所在地理地址相关信息
     * @since 1.0.0
     */
    private static String getAddress(String urlStr) {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = null;
        try {
            URL url = new URL(urlStr);
            URLConnection conn = url.openConnection();
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("HttpUtils##getAddress() Address={}", sb);
            }
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("HttpUtils##getAddress() IOException={}", e);
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("HttpUtils##getAddress() IOException={}", e);
                    }
                }
            }
        }
        return sb.toString();
    }

}
