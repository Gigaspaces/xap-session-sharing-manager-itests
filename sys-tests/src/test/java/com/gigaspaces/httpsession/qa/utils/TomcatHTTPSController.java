package com.gigaspaces.httpsession.qa.utils;

import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class TomcatHTTPSController extends TomcatController {

    private static final String DEFAULT_SERVER_CONFIG = "sys-tests/src/test/resources/config/tomcat/server-https.xml";
    private static final String KEYSTORE_FILE_LOCATION = "sys-tests/src/test/resources/config/tomcat/httpsession.keystore";

    public TomcatHTTPSController(int port) {
        super(port);
    }

    public TomcatHTTPSController(int port, boolean isSecured) {
        super(port,isSecured);
    }

    @Override
    public void start() {
        try {
            String content = IOUtils.toString(new FileInputStream(Config.getAbrolutePath(DEFAULT_SERVER_CONFIG)), "UTF-8");
            content = content.replaceAll("9005", "" + (9005 + currentInstance));
            content = content.replaceAll("8443", "" + port);
            content = content.replaceAll("9009", "" + (9009 + currentInstance));
            content = content.replaceAll("KEYSTORE_FILE_GOES_HERE", Config.getAbrolutePath(KEYSTORE_FILE_LOCATION));
            IOUtils.write(content, new FileOutputStream(serverConfig), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        super.startStarterRunner();
    }
}
