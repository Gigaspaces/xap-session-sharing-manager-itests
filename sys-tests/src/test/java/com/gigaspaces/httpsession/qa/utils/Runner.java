package com.gigaspaces.httpsession.qa.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Runner extends Thread {

	protected static final Logger LOGGER = LoggerFactory
			.getLogger(Runner.class);

	private volatile Object monitor = new Object();
	private List<Function> predicates = new ArrayList<Function>();

	protected List<String> commands = new ArrayList<String>();
	protected ProcessBuilder builder;
	protected Process process;
	protected BufferedReader stdInput;
	private int timeout = 60000;

	public Runner(String wc, Map<String, String> envs) {
		setDaemon(true);

		this.builder = new ProcessBuilder(commands);
        Map<String, String> env = builder.environment();
        if(envs != null)
            env.putAll(envs);
		this.builder.directory(new File(wc));
		builder.redirectErrorStream(true);
	}

	public Runner(String wc, int timeout, Map<String, String> envs) {
		this(wc, envs);
		this.timeout = timeout;
	}

	public List<String> getCommands() {
		return commands;
	}

	public void setCommands(List<String> commands) {
		this.commands = commands;
	}

	public void run() {

		try {
			process = builder.start();

			stdInput = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			try {
				String line;

				while ((line = stdInput.readLine()) != null) {

					LOGGER.debug(line);
					System.out.println(line);

					if (sunchronize(line)) {
						refresh();
					}
				}
			} finally {
				stdInput.close();
			}

			refresh();
		} catch (IOException e) {
            e.printStackTrace();
			throw new AssertionError();
		}
	}

	private void refresh() {
		synchronized (monitor) {
			monitor.notifyAll();
		}
	}

	private boolean sunchronize(String line2) {

		boolean result = false;

		for (Function p : predicates) {
			result = result || p.test(line2);

			if (result)
				return result;
		}

		return result;
	}

	public void startAndWait() {

		start();

		synchronized (monitor) {
			try {
				monitor.wait(timeout);
			} catch (InterruptedException e) {
			}
		}
	}

	public void or(Function predicate) {
		this.predicates.add(predicate);
	}
}
