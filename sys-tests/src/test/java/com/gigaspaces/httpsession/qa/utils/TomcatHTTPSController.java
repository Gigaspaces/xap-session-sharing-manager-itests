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

    public TomcatHTTPSController(int port, String appName) {
        super(port,appName);
    }

    public TomcatHTTPSController(int port, boolean secured, String appName) {
        super(port, secured, appName);
    }

    @Override
    public void start() {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(Config.getAbrolutePath(DEFAULT_SERVER_CONFIG));
            String content = IOUtils.toString(fis, "UTF-8");
            content = content.replaceAll("9005", "" + (9005 + currentInstance));
            content = content.replaceAll("8443", "" + port);
            content = content.replaceAll("9009", "" + (9009 + currentInstance));
            content = content.replaceAll("KEYSTORE_FILE_GOES_HERE", Config.getAbrolutePath(KEYSTORE_FILE_LOCATION));
            fos = new FileOutputStream(serverConfig);
            IOUtils.write(content, fos, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    LOGGER.warn("FileInputStream close throws an exception", e);
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    LOGGER.warn("FileOutputStream close throws an exception", e);
                }
            }
        }
        super.startStarterRunner();
    }
}
