package com.gigaspaces.httpsession.qa.utils;

import com.vladium.util.Files;
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
import java.util.concurrent.atomic.AtomicInteger;

public class TomcatController extends ServerController {

	private static final String DEFAULT_SERVER_CONFIG = "sys-tests/src/test/resources/config/server.xml";
	private static final String TEST_SECURED_POLICY = "src/test/resources/config/catalina-security.policy";
	private static final String SECURED_POLICY_ORG = "catalina.policy";
	private static final String STARTED_COMPLETED = "org.apache.catalina.startup.Catalina start";
	private static final String DESTROYING_COMPLETED = "Destroying ProtocolHandler [\"ajp-bio-9009\"]";
	private static final String BIN_CATALINA = "bin/catalina";
    private File serverConfig;
	public static final String TOMCAT_WEB_APPS = FilenameUtils.concat(
			Config.getTomcatHome(), WEB_APPS);

    private static AtomicInteger instancesCount = new AtomicInteger(0);
    private static boolean isDeployed;
    private static boolean isUndeployed;

    public TomcatController(String host, int port, boolean isSecured) {
		super(host, port, isSecured);
	}

	public TomcatController(String host, int port) {
		this(host, port, false);
	}

	public TomcatController(int port) {
		super(port);
	}

	@Override
	public Runner createStarter() {
        try {
            int currentInstance = instancesCount.getAndIncrement();
            //TODO put these files in the tests directory
            serverConfig = Files.createTempFile(new File("/tmp/"),"tomcat-server",".xml");
            String content = IOUtils.toString(new FileInputStream(Config.getAbrolutePath(DEFAULT_SERVER_CONFIG)), "UTF-8");
            content = content.replaceAll("9005", ""+(9005+currentInstance));
            content = content.replaceAll("9090", ""+port);
            content = content.replaceAll("9009", ""+(9009+currentInstance));
            IOUtils.write(content, new FileOutputStream(serverConfig), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Runner starter = new Runner(Config.getTomcatHome(), null);
		if (isSecured()){
			try {
				File securedPolicy = new File(Config.getTomcatHome() + File.separator + "conf" + File.separator + SECURED_POLICY_ORG);
				securedPolicy.renameTo(new File(Config.getTomcatHome() + File.separator + "conf" + File.separator + SECURED_POLICY_ORG + ".org"));
				FileUtils.copyFile(new File(TEST_SECURED_POLICY), new File(Config.getTomcatHome() + File.separator + "conf" + File.separator + SECURED_POLICY_ORG));
			}catch (IOException e){
				throw new RuntimeException(e);
			}
		}

		String path = getExecutionPath(Config.getTomcatHome(), BIN_CATALINA);

		LOGGER.debug("Tomcat start script:" + path);

		List<String> commands = starter.getCommands();

		commands.add(path);
		commands.add("run");
		if(isSecured())
			commands.add("-security");
		commands.add("-config");
		commands.add(serverConfig.getAbsolutePath());

		starter.or(new StringPredicate(STARTED_COMPLETED) {
			@Override
			public boolean customTest(String input) {
				return input.endsWith(match);
			}
		});

		starter.or(new TimeoutPredicate(TIMEOUT));

		return starter;
	}

	@Override
	public Runner createStopper() {

		Runner stopper = new Runner(Config.getTomcatHome(), null);

		if (isSecured()){
			try {
				File securedPolicy = new File(Config.getTomcatHome() + File.separator + "conf" + File.separator + SECURED_POLICY_ORG);
				FileUtils.forceDelete(securedPolicy);

				File orgSecuredPolicy = new File(Config.getTomcatHome() + File.separator + "conf" + File.separator + SECURED_POLICY_ORG + ".org");
				orgSecuredPolicy.renameTo(new File(Config.getTomcatHome() + File.separator + "conf" + File.separator + SECURED_POLICY_ORG));
			}catch (IOException e){
				throw new RuntimeException(e);
			}
		}

		String path = getExecutionPath(Config.getTomcatHome(), BIN_CATALINA);

		LOGGER.debug("Tomcat start script:" + path);

		List<String> commands = stopper.getCommands();

		commands.add(path);
		commands.add("stop");
		commands.add("-config");
		commands.add(Config.getAbrolutePath(serverConfig.getAbsolutePath()));

		stopper.or(new StringPredicate(DESTROYING_COMPLETED) {
			@Override
			public boolean customTest(String input) {
				return input.endsWith(match);
			}
		});

		stopper.or(new TimeoutPredicate(TIMEOUT));

		return stopper;
	}

	@Override
	public void deploy(String appName) throws IOException {
		FileUtils.copyDirectory(new File(Config.getAbrolutePath(WEB_APP_SOURCE)), new File(
				FilenameUtils.concat(TOMCAT_WEB_APPS, appName)));
	}

	@Override
	public void saveShiroFile(String appName, List<String> lines)
			throws IOException {
		String path = FilenameUtils.concat(TOMCAT_WEB_APPS, appName + "/"
				+ "WEB-INF/shiro.ini");

		FileUtils.writeLines(new File(path), lines);
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
    public void stopAll(boolean undeployOnce) throws IOException {
        stop();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!isUndeployed || !undeployOnce) {
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
        File serverDir = new File(FilenameUtils.concat(TOMCAT_WEB_APPS, appName));
        Assert.assertTrue("Failed to find server's directory [" + appName+ "]", serverDir.exists());
        FileUtils.forceDelete(serverDir);
	}
}
