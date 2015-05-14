package com.gigaspaces.httpsession.qa.utils;

import junit.framework.Assert;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class WebsphereHTTPSController extends WebsphereController {
    private static final String DEFAULT_SERVER_CONFIG = "sys-tests/src/test/resources/config/websphere/websphere-server-https.xml";

    public WebsphereHTTPSController(int port) {
        super(port);
    }

    public WebsphereHTTPSController(int port, String appName) {
        super(port, appName);
    }

    @Override
    public void start() {
        String path = FilenameUtils
                .concat(Config.getWebsphereHome(), BIN_START);


        Runner creator = new Runner(Config.getWebsphereHome(),10000, null);
        List<String> creatorCommands = creator.getCommands();
        creatorCommands.add(path);
        creatorCommands.add("create");
        creatorCommands.add("site"+instanceId);


        creator.or(new StringPredicate("Server site"+instanceId+" created.") {

            @Override
            public boolean customTest(String input) {
                return input.contains(match);
            }
        });

        Future<?> future = service.submit(creator);
        try {
            future.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Failed to run websphere creator command. "+e.getMessage());
        } finally {
            future.cancel(true);
        }


        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            //TODO put these files in the tests directory
            File serverConfig = new File(Config.getWebsphereHome()+"/usr/servers/site"+instanceId+"/server.xml");
            if (!serverConfig.exists()) {
                throw new RuntimeException("Unable to find server.xml file for site"+instanceId);
            }
            fis = new FileInputStream(Config.getAbrolutePath(DEFAULT_SERVER_CONFIG));
            String content = IOUtils.toString(fis, "UTF-8");
            content = content.replaceAll("9080", "-1");
            content = content.replaceAll("9443", ""+port);
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
