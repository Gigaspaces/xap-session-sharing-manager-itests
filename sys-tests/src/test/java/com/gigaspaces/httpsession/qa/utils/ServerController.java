package com.gigaspaces.httpsession.qa.utils;

import junit.framework.Assert;
import org.apache.commons.io.FilenameUtils;
import org.apache.shiro.config.Ini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ServerController {

	protected static final Logger LOGGER = LoggerFactory
			.getLogger(ServerController.class);

	protected static final String WEB_APP_SOURCE = "web/target/demo-app";
	protected static final String WEB_APPS = "webapps/";

	protected static final long TIMEOUT = 10000L;
    protected static final String SESSION_SPACE = "sessionSpace";
    public static final String DEFAULT_APP_NAME = "demo-app";
    public String appName;

    private Runner starter;
	private Runner stopper;
	protected String host = "127.0.0.1";
	protected int port = 8080;
	protected boolean secured;
	protected boolean springSecured;

    final static ExecutorService service = Executors.newCachedThreadPool();


    public ServerController() {
		init();
	}

	protected void init() {
		starter = createStarter();
		stopper = createStopper();
	}

	public ServerController(int port) {
        this(port, false, false, DEFAULT_APP_NAME);
    }

	public ServerController(String host, int port) {
		this(host, port, false, false);
	}

    public ServerController(int port, String appName) {
        this(port, false, false, appName);
    }

    public ServerController(int port,boolean secured, String appName) {
        this(port, secured, false, appName);
    }

	public ServerController(int port, boolean secured, boolean springSecured) {
		this(port, secured, springSecured, DEFAULT_APP_NAME);
	}

    public ServerController(int port, boolean secured) {
        this(port, secured, false, DEFAULT_APP_NAME);
    }

    public ServerController(String host, int port, boolean secured, boolean springSecured) {
		this.host = host;
		this.port = port;
		this.secured = secured;
		this.springSecured = springSecured;
        this.appName = DEFAULT_APP_NAME;
        init();
	}

    public ServerController(int port, boolean secured, boolean springSecured, String appName) {
        this.port = port;
        this.secured = secured;
        this.springSecured = springSecured;
        this.appName = appName;
        init();
    }

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public boolean isSecured() {
		return secured;
	}

	public abstract Runner createStarter();

	public abstract Runner createStopper();

	public abstract void deploy(String appName) throws IOException;

	public abstract void undeploy(String appName) throws IOException;

	public abstract void saveShiroFile(String appName, List<String> lines)
			throws IOException;

	public void startStarterRunner() {
        try {
            service.submit(starter).get(40, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Failed to run server starter. " + e.getMessage());
        }
/*		try {

			starter.startAndWait();

			running.compareAndSet(false, true);
		} catch (Throwable e) {
		}*/
	}

    public void start() {
        startStarterRunner();
    }

	public void stop() {
        try {
            service.submit(stopper).get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
		/*try {
			// stopper.start();
			stopper.startAndWait();

			running.compareAndSet(true, false);
		} catch (Throwable e) {
            throw new RuntimeException(e);
		}*/
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

    public void startAll(String file, Map<String, String> properties){
        throw new RuntimeException("WHY YOU NO OVERRIDE ME HUMAN?");
    }
    public void stopAll(boolean undeploy, boolean undeployOnce) throws IOException {
        throw new RuntimeException("WHY YOU NO OVERRIDE ME HUMAN?");
    }

    public void config(String file, Map<String, String> properties) {

        properties.put("main/connector.url", "jini://*/*/" + SESSION_SPACE + "?groups="+System.getProperty("group", System.getenv("LOOKUPGROUPS")));

        Ini ini = new Ini();

        ini.loadFromPath(file);

        Iterator<String> keyIterator = properties.keySet().iterator();

        while (keyIterator.hasNext()) {

            String key = keyIterator.next();
            String[] pv = key.split("/");

            if (pv != null && pv.length == 2) {
                String sectionName = pv[0];
                String propertyName = pv[1];
                String propertyValue = properties.get(key);

                ini.setSectionProperty(sectionName, propertyName, propertyValue);
            }
        }


        List<String> lines = new ArrayList<String>();

        Iterator<String> sections = ini.getSectionNames().iterator();

        while (sections.hasNext()) {
            String sectionName = sections.next();

            System.out.println("[" + sectionName + "]");

            lines.add("[" + sectionName + "]");

            Ini.Section sec = ini.getSection(sectionName);

            Iterator<String> keys = sec.keySet().iterator();

            while (keys.hasNext()) {
                String key = keys.next();

                System.out.println(key + "=" + sec.get(key));

                lines.add(key + "=" + sec.get(key));
            }
        }

        try {
            saveShiroFile(appName, lines);
        } catch (IOException e) {
            throw new Error("can not save shiro.ini", e);
        }
    }

    public void reset() {

    }

    public void stopOneWebServerWithPort(int port) {
        throw new RuntimeException("This method can only be called to a load balancer controller");
    }
}
