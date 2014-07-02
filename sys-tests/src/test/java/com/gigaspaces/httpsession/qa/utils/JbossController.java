package com.gigaspaces.httpsession.qa.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class JbossController extends ServerController {

	private static final String BIN_JBOSS_CLI = "bin/jboss-cli";
	private static final String BIN_STANDALONE = "bin/standalone";
	private static final String STARTED_COMPLETED = "Deployed \"app.war\"";
	public static final String JBOSS_DEPLOYMENTS = FilenameUtils.concat(
			Config.getJbossHome(), "standalone/deployments");

	public JbossController(String host, int port) {
		super(host, port);
	}

	public JbossController(int port) {
		super(port);
	}

	@Override
	public Runner createStarter() {
		Runner starter = new Runner(Config.getJbossHome(), null);

		String path = getExecutionPath(Config.getJbossHome(), BIN_STANDALONE);

		LOGGER.debug("JBOSS start script:" + path);

		starter.getCommands().add(path);

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

		
		FileUtils.copyDirectory(new File(Config.getAbrolutePath(WEB_APP_SOURCE)), new File(
				FilenameUtils.concat(JBOSS_DEPLOYMENTS, appName + ".war")));

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
	public void undeploy(String appName) throws IOException {
//		File file=new File(FilenameUtils.concat(JBOSS_DEPLOYMENTS,
//				appName + ".war.deploy"));
//		
//		FileUtils.forceDelete(file);
		
	}
}
