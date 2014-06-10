package com.gigaspaces.httpsession.qa.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class JettyController extends ServerController {

	private static final String BIN_START_JAR = "start.jar";
	public static final String JETTY_WEB_APPS = FilenameUtils.concat(
			Config.getJettyHome(), WEB_APPS);

	public JettyController(String host, int port) {
		super(host, port);
	}

	public JettyController(int port) {
		super(port);
	}

	@Override
	public Runner createStarter() {
		Runner starter = new Runner(Config.getJettyHome(),10000);

		String path = FilenameUtils
				.concat(Config.getJettyHome(), BIN_START_JAR);

		List<String> commands = starter.getCommands();
		commands.add("java");
		commands.add("-jar");
		commands.add(path);

		commands.add("-DSTOP.PORT=" + (port - 1));
		commands.add("-DSTOP.KEY=secret");
		commands.add("-Djetty.port=" + port);
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
		Runner stopper = new Runner(Config.getJettyHome());

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
		FileUtils.copyDirectory(new File(Config.getAbrolutePath(WEB_APP_SOURCE)), new File(
				FilenameUtils.concat(JETTY_WEB_APPS, appName)));
	}

	@Override
	public void saveShiroFile(String appName, List<String> lines)
			throws IOException {

		String path = FilenameUtils.concat(JETTY_WEB_APPS, appName + "/"
				+ "WEB-INF/shiro.ini");

		FileUtils.writeLines(new File(path), lines);
	}

	@Override
	public void undeploy(String appName) throws IOException {
	}	
}
