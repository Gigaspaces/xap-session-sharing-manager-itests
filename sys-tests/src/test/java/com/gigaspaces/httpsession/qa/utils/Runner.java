package com.gigaspaces.httpsession.qa.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private boolean isInterrupted = false;
    private boolean waitForTermination = true;
    private int pid;

	public static int getPid(Process process) {
        try {
            Class<?> ProcessImpl = process.getClass();
            Field field = ProcessImpl.getDeclaredField("pid");
            field.setAccessible(true);
            return field.getInt(process);
        } catch (Exception e) {
            return -1;
        }
    }
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
            boolean succeeded = false;
            System.out.println("Running: "+builder.command());
            process = builder.start();
            pid = getPid(process);
			stdInput = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			try {
				String line;

				StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), "ERROR");
				errorGobbler.start();
				while (true) {
					if ((line = stdInput.readLine()) != null && !isInterrupted) {
						LOGGER.debug("OUT "+line);
						System.out.println("OUT2 "+line);

						if ((succeeded = sunchronize(line))) {
							refresh();
							errorGobbler.interrupt();
							break;
						}
					}
				}
			} finally {
				stdInput.close();
            }

			refresh();
            if (waitForTermination && process.waitFor() != 0) {
                throw new RuntimeException("Server Controller exited with code "+ process.exitValue());
            }
            if (predicates.size() !=0 && !succeeded) {
                throw new RuntimeException("Failed to run!");
            }
		} catch (IOException e) {
            e.printStackTrace();
			throw new AssertionError();
		} catch (InterruptedException e) {
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

	public void startAndWait() { // This method is not called when using ExecutorService

		start();

		synchronized (monitor) {
			try {
				monitor.wait(timeout);
			} catch (InterruptedException e) {
			}
		}
	}

    @Override
    public void interrupt() {
        LOGGER.info("Runner interrupted!");
        isInterrupted = true;
        process.destroy();
        super.interrupt();
    }

    public void or(Function predicate) {
		this.predicates.add(predicate);
	}

    public void setWaitForTermination(boolean waitForTermination) {
        this.waitForTermination = waitForTermination;
    }

    public int getPid() {
        return pid;
    }

	private class StreamGobbler extends Thread {
		InputStream is;
		String type;

		private StreamGobbler(InputStream is, String type) {
			this.is = is;
			this.type = type;
		}

		@Override
		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
                System.out.println("Printing stderr");
                while (!this.isInterrupted() && (line = br.readLine()) != null) {
                    System.out.println(type + "> " + line);
                    LOGGER.debug("ERR " + line);
                }
                System.out.println("Finished printing strerr");
                isr.close();
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
}
