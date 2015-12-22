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
import java.util.concurrent.atomic.AtomicInteger;

public class JbossController extends ServerController {
	protected static final String DEFAULT_SERVER_CONFIG = "sys-tests/src/test/resources/config/jboss7-standalone.xml";
	protected static final String BIN_JBOSS_CLI = "bin/jboss-cli";
	protected static final String BIN_STANDALONE = "bin/standalone";
	protected static final String STARTED_COMPLETED = "Deployed \"demo-app.war\"";
	public static final String JBOSS_DEPLOYMENTS = FilenameUtils.concat(
			Config.getJbossHome(), "standalone/deployments");
	protected static AtomicInteger instancesCount = new AtomicInteger(0);
	protected File serverConfig;
	protected static boolean isDeployed;
	protected static boolean isUndeployed;
	protected int defaultJbossCliAdminPort;
	protected int actualJbossCliAdminPort;

	protected static final String DEFAULT_WEB_XML_CONFIG = "sys-tests/src/test/resources/config/web-jboss.xml";
	protected static final String SPRING_SECURITY_WEB_XML_CONFIG = "sys-tests/src/test/resources/config/web-jboss-spring-security.xml";

	public JbossController(String host, int port) {
		super(host, port);
	}

	public JbossController(int port) {
		super(port);
	}

    public JbossController(int port, String appName) {
        super(port, appName);
    }

	public JbossController(int port, boolean secured, boolean springSecured, String appName) {
		super(port, secured, springSecured, appName);
	}

    @Override
	protected void init() {
		defaultJbossCliAdminPort = 9999;
		super.init();
	}

	@Override
	public Runner createStarter() {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
			int currentInstance = instancesCount.getAndIncrement();
			//TODO put these files in the tests directory
			serverConfig = File.createTempFile("jboss-server" + currentInstance, ".xml", new File(Config.getJbossHome() + File.separator + "standalone" + File.separator + "configuration"));
            fis = new FileInputStream(Config.getAbrolutePath(DEFAULT_SERVER_CONFIG));
            String content = IOUtils.toString(fis, "UTF-8");
            content = content.replaceAll("8080", "" + port);
			content = content.replaceAll("4447", "" + (4447 + currentInstance));
			content = content.replaceAll("8009", "" + (8009 + currentInstance));
			actualJbossCliAdminPort = defaultJbossCliAdminPort + currentInstance;
			content = content.replaceAll(String.valueOf(defaultJbossCliAdminPort), "" + (defaultJbossCliAdminPort + currentInstance));
			content = content.replaceAll("9990", "" + (9990 + currentInstance));
            fos = new FileOutputStream(serverConfig);
            IOUtils.write(content, fos, "UTF-8");
        }catch (IOException e) {
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

		Runner starter = new Runner(Config.getJbossHome(), null);
        starter.setWaitForTermination(false);
		String path = getExecutionPath(Config.getJbossHome(), BIN_STANDALONE);

		LOGGER.debug("JBOSS start script:" + path);

		List<String> commands = starter.getCommands();
		commands.add(path);
		commands.add("-c");
		commands.add(serverConfig.getName());

		starter.or(new StringPredicate(STARTED_COMPLETED) {

			@Override
			public boolean customTest(String input) {
				return input.contains(match);
			}
		});

		return starter;
	}

	@Override
	public Runner createStopper() {
		Runner stopper = new Runner(Config.getJbossHome(), null);

		String path = getExecutionPath(Config.getJbossHome(), BIN_JBOSS_CLI);

		LOGGER.debug("JBOSS shutdown script:" + path);

		stopper.getCommands().add(path);
		stopper.getCommands().add("--connect");
		stopper.getCommands().add("controller=localhost:" + actualJbossCliAdminPort);
		stopper.getCommands().add("--command=:shutdown");

		stopper.or(new StringPredicate("{\"outcome\" => \"success\"}") {
			@Override
			public boolean customTest(String input) {
				return match.equals(input);
			}
		});

		return stopper;
	}

	@Override
	public void deploy(String appName) throws IOException {

		
		FileUtils.copyDirectory(
				new File(Config.getAbrolutePath(WEB_APP_SOURCE)),
				new File(FilenameUtils.concat(JBOSS_DEPLOYMENTS, appName + ".war")));

		FileUtils.touch(new File(FilenameUtils.concat(JBOSS_DEPLOYMENTS,
				appName + ".war.dodeploy")));

	}

	@Override
	public void saveShiroFile(String appName, List<String> lines)
			throws IOException {
		String path = FilenameUtils.concat(JBOSS_DEPLOYMENTS, appName + ".war/"
				+ "WEB-INF/shiro.ini");

		FileUtils.writeLines(new File(path), lines);

	}

	@Override
	protected void saveWebXmlFile(String appName, String webServerApp)
			throws IOException {
		String path = FilenameUtils.concat(webServerApp, appName + "/" + "WEB-INF/web.xml");

		if(springSecured) {
			FileUtils.copyFile(new File(Config.getAbrolutePath(SPRING_SECURITY_WEB_XML_CONFIG)), new File(path));
		}else{
			FileUtils.copyFile(new File(Config.getAbrolutePath(DEFAULT_WEB_XML_CONFIG)), new File(path));
		}
	}

    @Override
	public void undeploy(String appName) throws IOException {
		File serverDir = new File(FilenameUtils.concat(JBOSS_DEPLOYMENTS, appName + ".war"));
		Assert.assertTrue("Failed to find server's directory [" + appName + "]", serverDir.exists());
		FileUtils.forceDelete(serverDir);

		File[] dirFiles = new File(JBOSS_DEPLOYMENTS).listFiles();
		for (int i=0; i<dirFiles.length; i++)
			if (dirFiles[i].getName().startsWith(appName + ".war.", 0))
				FileUtils.forceDelete(dirFiles[i]);
	}


	@Override
	public void startAll(String file, Map<String, String> properties) {
		if (!isDeployed) {
			isDeployed = true;
			try {
				deploy(appName);
				saveWebXmlFile(appName + ".war", JBOSS_DEPLOYMENTS);
				saveSpringSecurityFile(appName + ".war", JBOSS_DEPLOYMENTS);
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
		if (!isUndeployed || !undeployOnce) {
			isUndeployed = true;
			undeploy(appName);
		}
	}

	@Override
	public void reset() {
		isDeployed = false;
		isUndeployed = false;
	}

	@Override
	public void dumpLogsToDir(File dir) {
		File destination = new File(dir, "jboss_" + port+".zip");
		LOGGER.info("Dumping logs to " + destination.getAbsolutePath());
		File[] files = new File[0];
		try {
			ZipUtils.zipDirectory(destination , new File(Config.getJbossHome()+"/logs"), files, files, files,"");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
