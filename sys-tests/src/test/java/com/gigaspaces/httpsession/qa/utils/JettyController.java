package com.gigaspaces.httpsession.qa.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

public class JettyController extends ServerController {

	protected static final String BIN_START_JAR = "start.jar";
    protected static final String JETTY_WEB_APPS = FilenameUtils.concat(
			Config.getJettyHome(), WEB_APPS);
    protected static boolean isDeployed;
    protected static boolean isUndeployed;

    protected static final String START_INI = "sys-tests/src/test/resources/config/jetty/start.ini";


    public JettyController(String host, int port) {
		super(host, port);
	}

	public JettyController(int port) {
		super(port);
	}

	@Override
	public Runner createStarter() {
		Runner starter = new Runner(Config.getJettyHome(),10000, null);

		String path = FilenameUtils
				.concat(Config.getJettyHome(), BIN_START_JAR);

		List<String> commands = starter.getCommands();
		commands.add("java");
		commands.add("-jar");
		commands.add(path);

        commands.add("--module=http");
        commands.add("-Djetty.port="+port);
		commands.add("-DSTOP.PORT=" + (port - 1));
		commands.add("-DSTOP.KEY=secret");
		commands.add("-Djetty.home=" + Config.getJettyHome());

		starter.or(new StringPredicate("oejs.Server:main: Started") {

			@Override
			public boolean customTest(String input) {
				return input.contains(match);
			}
		});


    starter.or(new TimeoutPredicate(TIMEOUT));

		return starter;
	}

	@Override
	public Runner createStopper() {
		Runner stopper = new Runner(Config.getJettyHome(), null);

		String path = FilenameUtils
				.concat(Config.getJettyHome(), BIN_START_JAR);

		List<String> commands = stopper.getCommands();
		commands.add("java");
		commands.add("-jar");
		commands.add(path);
		commands.add("-DSTOP.PORT=" + (port - 1));
		commands.add("-DSTOP.KEY=secret");
		commands.add("-Djetty.home=" + Config.getJettyHome());
		commands.add("--stop");

		stopper.or(new TimeoutPredicate(TIMEOUT));

		return stopper;
	}

	@Override
	public void deploy(String appName) throws IOException {
        File target = new File(FilenameUtils.concat(JETTY_WEB_APPS, appName));
        FileUtils.copyDirectory(new File(Config.getAbrolutePath(WEB_APP_SOURCE)), target);

	}

	@Override
	public void saveShiroFile(String appName, List<String> lines)
			throws IOException {

		String path = FilenameUtils.concat(JETTY_WEB_APPS, appName + "/"
				+ "WEB-INF/shiro.ini");

		FileUtils.writeLines(new File(path), lines);
	}

    @Override
    public void start() {
        try {
            FileUtils.copyFile(new File(Config.getAbrolutePath(START_INI)), new File(Config.getJettyHome()+"/start.ini"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        super.start();
    }

    @Override
    public void startAll(String file, Map<String, String> properties) {
        if (!isDeployed) {
            isDeployed = true;
            try {
                deploy(APP_NAME);
            } catch (IOException e) {
                e.printStackTrace();
            }
            config(file, properties);
        }
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
        if (undeploy && (!isUndeployed || !undeployOnce)) {
            isUndeployed = true;
            undeploy(APP_NAME);
        }
    }

    @Override
    public void reset() {
        isDeployed = false;
        isUndeployed = false;
    }

    @Override
	public void undeploy(String appName) throws IOException {
        File serverDir = new File(FilenameUtils.concat(JETTY_WEB_APPS, appName));
        Assert.assertTrue("Failed to find server's directory [" + appName + "]", serverDir.exists());
        FileUtils.forceDelete(serverDir);
	}	
}
