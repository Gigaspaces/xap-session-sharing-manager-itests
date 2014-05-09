package com.gigaspaces.httpsession.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ServerController {

	protected static final Logger LOGGER = LoggerFactory
			.getLogger(ServerController.class);

	protected static final String WEB_APP_SOURCE = "/Users/shadim/src/http-session/samples/web/target/sample-webapp-1.0-b13";
	protected static final String WEB_APPS = "webapps/";

	protected static final long TIMEOUT = 5000L;

	private Runner starter;
	private Runner stopper;
	private AtomicBoolean running = new AtomicBoolean(false);
	protected String host = "127.0.0.1";
	protected int port = 8080;

	public ServerController() {
		init();
	}

	private void init() {
		starter = createStarter();
		stopper = createStopper();
	}

	public ServerController(int port) {
		this.port = port;
		init();
	}

	public ServerController(String host, int port) {
		this.host = host;
		this.port = port;
		init();
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public abstract Runner createStarter();

	public abstract Runner createStopper();

	public abstract void deploy(String appName) throws IOException;

	public abstract void undeploy(String appName) throws IOException;

	public abstract void saveShiroFile(String appName, List<String> lines)
			throws IOException;

	public void start() {
		try {

			starter.startAndWait();

			running.compareAndSet(false, true);
		} catch (Throwable e) {
		}
	}

	public void stop() {
		try {
			// stopper.start();
			stopper.startAndWait();

			running.compareAndSet(true, false);
		} catch (Throwable e) {
		}
	}

	protected String getExecutionPath(String basePath, String filePath) {
		String path = FilenameUtils.concat(basePath, filePath);

		if (File.separatorChar == '\\') {
			path = "\"" + path + ".bat\"";
		} else {
			path += ".sh";
		}

		return FilenameUtils.normalize(path);
	}

	protected void log(Throwable e, String msg) throws AssertionError {
		LOGGER.error(msg, e);
		throw new AssertionError();
	}
}
