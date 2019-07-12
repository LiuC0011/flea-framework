package com.huazie.frame.jersey.client.resource;

import com.huazie.ffs.pojo.upload.input.InputUploadAuthInfo;
import com.huazie.ffs.pojo.upload.output.OutputUploadAuthInfo;
import com.huazie.frame.common.FleaFrameManager;
import com.huazie.frame.common.i18n.FleaI18nHelper;
import com.huazie.frame.common.util.json.GsonUtils;
import com.huazie.frame.jersey.client.FleaJerseyClient;
import com.huazie.frame.jersey.client.request.RequestModeEnum;
import com.huazie.frame.jersey.client.response.Response;
import com.huazie.frame.jersey.common.data.FleaJerseyRequest;
import com.huazie.frame.jersey.common.data.FleaJerseyRequestData;
import com.huazie.frame.jersey.common.data.FleaJerseyResponse;
import com.huazie.frame.jersey.common.data.RequestBusinessData;
import com.huazie.frame.jersey.common.data.RequestPublicData;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.util.Locale;

/**
 * <p>  </p>
 *
 * @author huazie
 * @version 1.0.0
 * @since 1.0.0
 */
public class JerseyTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(JerseyTest.class);

    private WebTarget target;

    private ApplicationContext applicationContext;

    @Before
    public void init() {
        Client client = ClientBuilder.newClient();
        target = client.target("http://localhost:8080/fleafs");

        applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        LOGGER.debug("ApplicationContext={}", applicationContext);
    }

    @Test
    public void testUpload() {

        FleaJerseyRequest request = new FleaJerseyRequest();
        FleaJerseyRequestData requestData = new FleaJerseyRequestData();

        RequestPublicData publicData = new RequestPublicData();
        publicData.setSystemUserId("1000");
        publicData.setSystemUserPassword("asd123");
        publicData.setResourceCode("upload");
        publicData.setServiceCode("FLEA_SERVICE_UPLOAD_AUTH");

        RequestBusinessData businessData = new RequestBusinessData();

        InputUploadAuthInfo uploadAuthInfo = new InputUploadAuthInfo();
        uploadAuthInfo.setFileName("美丽的风景.png");

        String inputJson = GsonUtils.toJsonString(uploadAuthInfo);
        businessData.setInput(inputJson);

        requestData.setPublicData(publicData);
        requestData.setBusinessData(businessData);

        request.setRequestData(requestData);

        Entity<FleaJerseyRequest> entity = Entity.entity(request, MediaType.APPLICATION_XML_TYPE);
        FleaJerseyResponse response = target.path("upload").request().post(entity, FleaJerseyResponse.class);

        LOGGER.debug("result = {}", response);
    }

    @Test
    public void testFleaJerseyClient() throws Exception {

        String clientCode = "FLEA_CLIENT_UPLOAD_AUTH";

        InputUploadAuthInfo uploadAuthInfo = new InputUploadAuthInfo();
        uploadAuthInfo.setFileName("美丽的风景.png");

        FleaJerseyClient client = applicationContext.getBean(FleaJerseyClient.class);

        Response<OutputUploadAuthInfo> response = client.invoke(clientCode, uploadAuthInfo, OutputUploadAuthInfo.class);

        LOGGER.debug("result = {}", response);
    }

    @Test
    public void testMediaType() {

        String mediaTypeStr = "xml";

        MediaType mediaType = MediaType.valueOf(mediaTypeStr);

        LOGGER.debug("MediaType = {}", mediaType);

    }

    @Test
    public void testEnum() {

        RequestModeEnum modeEnum = RequestModeEnum.valueOf("GET1");

        LOGGER.debug("RequestModeEnum = {}", modeEnum.getMode());
    }

    @Test
    public void fleaI18NHelperTest() {
        FleaFrameManager.getManager().setLocale(Locale.CHINESE);
        try {
            FleaI18nHelper.i18n("ERROR0000000001", "error");
            FleaI18nHelper.i18n("ERROR-JERSEY-CLIENT0000000000", "error_jersey");
        } catch (Exception e) {
            LOGGER.error("Exception={}", e);
        }
    }
}
