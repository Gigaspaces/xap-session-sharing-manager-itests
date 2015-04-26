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

	@Override
	public Runner createStarter() {
        String path = FilenameUtils
                .concat(Config.getWebsphereHome(), BIN_START);



        Runner starter = new Runner(Config.getWebsphereHome(),10000, null);

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
		Runner stopper = new Runner(Config.getWebsphereHome(),10000, null);

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
        File target = new File(FilenameUtils.concat(WEBSPHERE_WEB_APPS, appName+".war"));
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

        creator.or(new TimeoutPredicate(TIMEOUT));

        try {
            service.submit(creator).get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Failed to run websphere creator command. "+e.getMessage());
        }


        try {
            //TODO put these files in the tests directory
            File serverConfig = new File(Config.getWebsphereHome()+"/usr/servers/site"+instanceId+"/server.xml");
            if (!serverConfig.exists()) {
                throw new RuntimeException("Unable to find server.xml file for site"+instanceId);
            }
            String content = IOUtils.toString(new FileInputStream(Config.getAbrolutePath(DEFAULT_SERVER_CONFIG)), "UTF-8");
            content = content.replaceAll("9443", ""+(9443+instanceId    ));
            content = content.replaceAll("9080", ""+port);
            IOUtils.write(content, new FileOutputStream(serverConfig), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        super.start();
    }

    @Override
    public void startAll(String file, Map<String, String> properties) {
        start();
        try {
            deploy(appName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        config(file, properties);
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
}
