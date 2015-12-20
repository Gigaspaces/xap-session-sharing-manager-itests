package com.gigaspaces.httpsession.qa.utils;

import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.shiro.config.Ini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public abstract class ServerController {

	protected static final Logger LOGGER = LoggerFactory
			.getLogger(ServerController.class);

	protected static final String WEB_APP_SOURCE = "web/target/demo-app";
	protected static final String WEB_APPS = "webapps/";

    protected static final String SESSION_SPACE = "sessionSpace";
    public static final String DEFAULT_APP_NAME = "demo-app";
    public String appName;

    private Runner starter;
	private Runner stopper;
	protected String host = "127.0.0.1";
	protected int port = 8080;
	protected boolean secured;
	protected boolean springSecured;
    protected ServerStatus serverStatus = ServerStatus.NOT_RUNNING;

    protected static final String START_INI = "sys-tests/src/test/resources/config/jetty/start.ini";

    protected static final String DEFAULT_WEB_XML_CONFIG = "sys-tests/src/test/resources/config/web.xml";
    protected static final String SPRING_SECURITY_WEB_XML_CONFIG = "sys-tests/src/test/resources/config/web-spring-security.xml";
    protected static final String SPRING_SECURITY_CONFIG = "sys-tests/src/test/resources/config/spring-security.xml";

    final static ExecutorService service = Executors.newCachedThreadPool();

    private enum ServerStatus{NOT_RUNNING, RUNNING}

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
        if (serverStatus.equals(ServerStatus.RUNNING)) {
            System.out.println("Trying to start an already started server controller");
            LOGGER.warn("Trying to start an already started server controller");
            return;
        }
        Future<?> future = service.submit(starter);
        try {
            future.get(40, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            //runPS();
            //runMemory();
            //runTop();
            Assert.fail("Failed to run server starter. " + e.getMessage());
        } finally {
            future.cancel(true);
            serverStatus = ServerStatus.RUNNING;
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
        if (serverStatus.equals(ServerStatus.NOT_RUNNING)) {
            System.out.println("Trying to stop a not running server controller");
            LOGGER.warn("Trying to stop a not running controller");
            return;
        }

        Future<?> future = service.submit(stopper);
        try {
            future.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Failed to run server stopper. " + e.getMessage());
        } finally {
            future.cancel(true);
            //serverStatus=ServerStatus.NOT_RUNNING;
        }

        Runner forceStop = new Runner("/tmp", null);
        forceStop.getCommands().add("/bin/bash");
        forceStop.getCommands().add(Config.getAbrolutePath("sys-tests/src/test/resources/config/tomcat/kkillScript.sh"));
        forceStop.getCommands().add(""+starter.getPid());
        Future<?> forceStopFuture = service.submit(forceStop);
        try {
            forceStopFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Failed to run server stopper. " + e.getMessage());
        } finally {
            forceStopFuture.cancel(true);
            serverStatus=ServerStatus.NOT_RUNNING;
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

    public void startAll(String file, Map<String, String> properties){
        throw new RuntimeException("WHY YOU NO OVERRIDE ME HUMAN?");
    }
    public void stopAll(boolean undeploy, boolean undeployOnce) throws IOException {
        throw new RuntimeException("WHY YOU NO OVERRIDE ME HUMAN?");
    }

    public void config(String file, Map<String, String> properties) {

        properties.put("main/connector.url", "jini://*/*/" + SESSION_SPACE + "?groups="+Config.getLookupGroups());

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

    protected void saveWebXmlFile(String appName, String webServerApp)
            throws IOException {
        String path = FilenameUtils.concat(webServerApp, appName + "/" + "WEB-INF/web.xml");

        if(springSecured) {
            FileUtils.copyFile(new File(Config.getAbrolutePath(SPRING_SECURITY_WEB_XML_CONFIG)), new File(path));
        }else{
            FileUtils.copyFile(new File(Config.getAbrolutePath(DEFAULT_WEB_XML_CONFIG)), new File(path));
        }
    }

    protected void saveSpringSecurityFile(String appName, String webServerApp)
            throws IOException {
        String path = FilenameUtils.concat(webServerApp, appName + "/"
                + "WEB-INF/spring-security.xml");

        if(springSecured)
            FileUtils.copyFile(new File(Config.getAbrolutePath(SPRING_SECURITY_CONFIG)), new File(path));
    }

    private void runPS() {
        System.out.println("Running ps aux");
        Runner starter = new Runner("/",10000, null);
        starter.setWaitForTermination(true);

        List<String> commands = starter.getCommands();
        commands.add("/bin/sh");
        commands.add("-c");
        commands.add("ps aux");

        Future<?> future = service.submit(starter);
        try {
            future.get(4, TimeUnit.SECONDS);
            System.out.println("Finished running ps aux");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Running ps aux failed!");
        }

    }

    private void runMemory() {
        System.out.println("Running free -m");
        Runner starter = new Runner("/",10000, null);
        starter.setWaitForTermination(true);

        List<String> commands = starter.getCommands();
        commands.add("/bin/sh");
        commands.add("-c");
        commands.add("free -m");

        Future<?> future = service.submit(starter);
        try {
            future.get(4, TimeUnit.SECONDS);
            System.out.println("Finished running free -m");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Running free -m failed!");
        }
    }

    private void runTop() {
        System.out.println("Running top -n 1");

        HashMap<String, String> env = new HashMap<String, String>();
        env.put("TERM", "xterm");
        Runner starter = new Runner("/",10000, env);
        starter.setWaitForTermination(true);

        List<String> commands = starter.getCommands();
        commands.add("/bin/sh");
        commands.add("-c");
        commands.add("top -c -n 1 -b");

        Future<?> future = service.submit(starter);
        try {
            future.get(4, TimeUnit.SECONDS);
            System.out.println("Finished running top -n 1");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Running top -n 1 failed!");
        }
    }


    public void dumpLogsToDir(File dir) {
        throw new RuntimeException("WHY YOU NO OVERRIDE ME, HUMAN?" + this.getClass());
    }
}
