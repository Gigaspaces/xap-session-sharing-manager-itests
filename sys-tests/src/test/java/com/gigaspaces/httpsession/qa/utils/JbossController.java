package com.gigaspaces.httpsession.qa.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.vladium.util.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

public class JbossController extends ServerController {
	private static final String DEFAULT_SERVER_CONFIG = "sys-tests/src/test/resources/config/standalone.xml";
	private static final String BIN_JBOSS_CLI = "bin/jboss-cli";
	private static final String BIN_STANDALONE = "bin/standalone";
	private static final String STARTED_COMPLETED = "Deployed \"app.war\"";
	public static final String JBOSS_DEPLOYMENTS = FilenameUtils.concat(
			Config.getJbossHome(), "standalone/deployments");
	private static AtomicInteger instancesCount = new AtomicInteger(0);
	private File serverConfig;

	public JbossController(String host, int port) {
		super(host, port);
	}

	public JbossController(int port) {
		super(port);
	}

	@Override
	public Runner createStarter() {
		try {
			int currentInstance = instancesCount.getAndIncrement();
			//TODO put these files in the tests directory
			serverConfig = Files.createTempFile(new File(Config.getJbossHome() + File.separator + "standalone" + File.separator + "configuration"), "jboss-server" + currentInstance, ".xml");
			String content = IOUtils.toString(new FileInputStream(Config.getAbrolutePath(DEFAULT_SERVER_CONFIG)), "UTF-8");
			content = content.replaceAll("8080", "" + port);
			content = content.replaceAll("4447", "" + (4447 + currentInstance));
			content = content.replaceAll("8009", "" + (8009 + currentInstance));
			content = content.replaceAll("9999", "" + (9999 + currentInstance));
			content = content.replaceAll("9990", "" + (9990 + currentInstance));
			IOUtils.write(content, new FileOutputStream(serverConfig), "UTF-8");
		}catch (IOException e) {
			throw new RuntimeException(e);
		}

		Runner starter = new Runner(Config.getJbossHome(), null);

		String path = getExecutionPath(Config.getJbossHome(), BIN_STANDALONE);

		LOGGER.debug("JBOSS start script:" + path);

		List<String> commands = starter.getCommands();
		commands.add(path);
		commands.add("-c");
		commands.add(serverConfig.getName());

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
		Runner stopper = new Runner(Config.getJbossHome(), null);

		String path = getExecutionPath(Config.getJbossHome(), BIN_JBOSS_CLI);

		LOGGER.debug("JBOSS shutdown script:" + path);

		stopper.getCommands().add(path);
		stopper.getCommands().add("--connect");
		stopper.getCommands().add("--command=:shutdown");

		stopper.or(new StringPredicate("{\"outcome\" => \"success\"}") {
			@Override
			public boolean customTest(String input) {
				return match.equals(input);
			}
		});

		stopper.or(new TimeoutPredicate(TIMEOUT));

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
    public void startAll(String file, Map<String, String> properties) {
     //   if (!isInitialized) {
            try {
                deploy(APP_NAME);
            } catch (IOException e) {
                e.printStackTrace();
            }
            config(file, properties);
           // isInitialized = true;
        //}
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
        undeploy(APP_NAME);
    }

    @Override
	public void undeploy(String appName) throws IOException {
//		File file=new File(FilenameUtils.concat(JBOSS_DEPLOYMENTS,
//				appName + ".war.deploy"));
//		
//		FileUtils.forceDelete(file);
		
	}
}
