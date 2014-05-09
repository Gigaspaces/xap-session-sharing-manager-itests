package com.gigaspaces.httpsession.qa.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class TomcatController extends ServerController {

	private static final String SERVER_CONFIG = "/Users/shadim/src/http-session/gs-session-manager/src/test/resources/config/server.xml";
	private static final String STARTED_COMPLETED = "org.apache.catalina.startup.Catalina start";
	private static final String DESTROYING_COMPLETED = "Destroying ProtocolHandler [\"ajp-bio-9009\"]";
	private static final String BIN_CATALINA = "bin/catalina";
	public static final String TOMCAT_WEB_APPS = FilenameUtils.concat(
			Config.getTomcatHome(), WEB_APPS);

	public TomcatController(String host, int port) {
		super(host, port);
	}

	public TomcatController(int port) {
		super(port);
	}

	@Override
	public Runner createStarter() {

		Runner starter = new Runner();

		String path = getExecutionPath(Config.getTomcatHome(), BIN_CATALINA);

		LOGGER.debug("Tomcat start script:" + path);

		List<String> commands = starter.getCommands();

		commands.add(path);
		commands.add("run");
		commands.add("-config");
		commands.add(SERVER_CONFIG);

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

		Runner stopper = new Runner();

		String path = getExecutionPath(Config.getTomcatHome(), BIN_CATALINA);

		LOGGER.debug("Tomcat start script:" + path);

		List<String> commands = stopper.getCommands();

		commands.add(path);
		commands.add("stop");
		commands.add("-config");
		commands.add(SERVER_CONFIG);

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
		FileUtils.copyDirectory(new File(WEB_APP_SOURCE), new File(
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
	public void undeploy(String appName) throws IOException {
	}
}