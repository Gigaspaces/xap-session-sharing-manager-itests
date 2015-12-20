package com.gigaspaces.httpsession.qa.utils;

import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class WebsphereController extends ServerController {

	protected static final String BIN_START = "bin/server";
	public String WEBSPHERE_WEB_APPS;
    private static final String DEFAULT_SERVER_CONFIG = "sys-tests/src/test/resources/config/websphere/websphere-server.xml";

    private static AtomicInteger instancesCount = new AtomicInteger(0);

    protected int instanceId;

    public WebsphereController(int port, String appName) {
        super(port, appName);
    }

    @Override
    protected void init() {
        instanceId = instancesCount.incrementAndGet();
        WEBSPHERE_WEB_APPS= FilenameUtils.concat(Config.getWebsphereHome(), "usr/servers/site"+instanceId+"/dropins");
        super.init();
    }

    public WebsphereController(int port) {
        super(port);
    }

    public WebsphereController(int port, boolean isSpringSecurity, String appName) {
        super(port, false, isSpringSecurity, appName);
    }

	@Override
	public Runner createStarter() {
        String path = FilenameUtils
                .concat(Config.getWebsphereHome(), BIN_START);


        Runner starter = new Runner(Config.getWebsphereHome(), 10000, Config.getEnvsWithJavaHome());

		List<String> commands = starter.getCommands();

		commands.add(path);

		commands.add("start");
        commands.add("site"+instanceId);
        starter.or(new StringPredicate("Server site"+instanceId+" started with process ID ") {

            @Override
            public boolean customTest(String input) {
                return input.contains(match);
            }
        });

/*
        starter.or(new TimeoutPredicate(TIMEOUT));*/
		return starter;
	}

	@Override
	public Runner createStopper() {
        Runner stopper = new Runner(Config.getWebsphereHome(), 10000, Config.getEnvsWithJavaHome());

		String path = FilenameUtils
				.concat(Config.getWebsphereHome(), BIN_START);

		List<String> commands = stopper.getCommands();
        commands.add(path);
        commands.add("stop");
        commands.add("site"+instanceId);

        stopper.or(new StringPredicate("Server site"+instanceId+" stopped.") {

            @Override
            public boolean customTest(String input) {
                return input.contains(match);
            }
        });

/*

        stopper.or(new TimeoutPredicate(TIMEOUT));
*/

		return stopper;
	}

	@Override
	public void deploy(String appName) throws IOException {
        File target = new File(FilenameUtils.concat(WEBSPHERE_WEB_APPS, appName + ".war"));
        FileUtils.copyDirectory(new File(Config.getAbrolutePath(WEB_APP_SOURCE)), target);

	}

	@Override
	public void saveShiroFile(String appName, List<String> lines)
			throws IOException {

		String path = FilenameUtils.concat(WEBSPHERE_WEB_APPS, appName+".war" + "/"
				+ "WEB-INF/shiro.ini");

		FileUtils.writeLines(new File(path), lines);
	}

    @Override
    public void start() {
        super.start();
    }

    protected void createSite() {
        String path = FilenameUtils
                .concat(Config.getWebsphereHome(), BIN_START);


        Runner creator = new Runner(Config.getWebsphereHome(), 10000, Config.getEnvsWithJavaHome());
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

        System.out.println("Will create site"+instanceId);
        Future<?> future = service.submit(creator);
        try {
            future.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Failed to run websphere creator command. "+e.getMessage());
        } finally {
            future.cancel(true);
        }


        FileOutputStream fos = null;
        FileInputStream fis = null;
        try {
            //TODO put these files in the tests directory
            File serverConfig = new File(Config.getWebsphereHome()+"/usr/servers/site"+instanceId+"/server.xml");
            if (!serverConfig.exists()) {
                throw new RuntimeException("Unable to find server.xml file for site"+instanceId);
            }
            fis = new FileInputStream(Config.getAbrolutePath(DEFAULT_SERVER_CONFIG));
            String content = IOUtils.toString(fis, "UTF-8");
            content = content.replaceAll("9443", ""+(9443+instanceId    ));
            content = content.replaceAll("9080", ""+port);
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

    }

    @Override
    public void startAll(String file, Map<String, String> properties) {
        createSite();
        try {
            deploy(appName);
            saveWebXmlFile(appName + ".war", WEBSPHERE_WEB_APPS);
            saveSpringSecurityFile(appName + ".war", WEBSPHERE_WEB_APPS);
        } catch (IOException e) {
            e.printStackTrace();
        }
        config(file, properties);
        start();
    }

    @Override
    public void stopAll(boolean undeploy, boolean undeployOnce) throws IOException {
        stop();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (undeploy) {
            undeploy(appName);
        }
    }

    @Override
    public void reset() {
        instancesCount.set(0);
    }

    @Override
	public void undeploy(String appName) throws IOException {
        File serverDir = new File(Config.getWebsphereHome()+"/usr/servers/site"+instanceId);
        Assert.assertTrue("Failed to find server's directory [site" + instanceId + "]", serverDir.exists());
        FileUtils.forceDelete(serverDir);

	}

    @Override
    public void dumpLogsToDir(File dir) {
        File destination = new File(dir, "websphere_" + port+".zip");
        LOGGER.info("Dumping logs to " + destination.getAbsolutePath());
        File[] files = new File[0];
        try {
            ZipUtils.zipDirectory(destination , new File(Config.getWebsphereHome()+"/usr/servers/site"+instanceId+"/logs"), files, files, files,"");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
