package com.gigaspaces.httpsession.qa.utils;

import com.vladium.util.Files;
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

/**
 * Created by kobi on 4/19/15.
 */
public class JBoss8Controller extends ServerController {

    protected static final String DEFAULT_SERVER_CONFIG = "sys-tests/src/test/resources/config/jboss8-standalone.xml";
    public static final String JBOSS_DEPLOYMENTS = FilenameUtils.concat(
            Config.getJboss8Home(), "standalone/deployments");
    protected static final String BIN_JBOSS_CLI = "bin/jboss-cli";
    protected static final String BIN_STANDALONE = "bin/standalone";
    protected static final String STARTED_COMPLETED = "Deployed \"demo-app.war\"";

	protected static final String DEFAULT_WEB_XML_CONFIG = "sys-tests/src/test/resources/config/web-jboss.xml";
    protected static final String SPRING_SECURITY_WEB_XML_CONFIG = "sys-tests/src/test/resources/config/web-jboss-spring-security.xml";

    protected File serverConfig;

    protected static AtomicInteger instancesCount = new AtomicInteger(0);
    protected int defaultJbossCliAdminPort;
    protected int actualJbossCliAdminPort;

    protected static boolean isDeployed;
    protected static boolean isUndeployed;

    public JBoss8Controller(int port) {
        super(port);
    }

    public JBoss8Controller(int port, String appName) {
        super(port, appName);
    }

    public JBoss8Controller(int port, boolean secured, boolean springSecured, String appName) {
        super(port, secured, springSecured, appName);
    }

    @Override
	protected void init() {
		defaultJbossCliAdminPort = 9990;
		super.init();
	}

	@Override
    public Runner createStarter() {
        try {
            int currentInstance = instancesCount.getAndIncrement();
            //TODO put these files in the tests directory
            serverConfig = Files.createTempFile(new File(Config.getJboss8Home() + File.separator + "standalone" + File.separator + "configuration"), "jboss8-server" + currentInstance, ".xml");
            String content = IOUtils.toString(new FileInputStream(Config.getAbrolutePath(DEFAULT_SERVER_CONFIG)), "UTF-8");
            content = content.replaceAll("8080", "" + port);
            content = content.replaceAll("4712", "" + (4712 + currentInstance));
            content = content.replaceAll("4713", "" + (4713 + currentInstance));
            actualJbossCliAdminPort = defaultJbossCliAdminPort + currentInstance;
            content = content.replaceAll(String.valueOf(defaultJbossCliAdminPort), "" + (defaultJbossCliAdminPort + currentInstance));
            content = content.replaceAll("8009", "" + (8009 + currentInstance));
            content = content.replaceAll("9993", "" + (9993 + currentInstance));
            IOUtils.write(content, new FileOutputStream(serverConfig), "UTF-8");
        }catch (IOException e) {
            throw new RuntimeException(e);
        }

        Runner starter = new Runner(Config.getJboss8Home(), Config.getEnvsWithJavaHome());

        String path = getExecutionPath(Config.getJboss8Home(), BIN_STANDALONE);

        LOGGER.debug("JBOSS8 start script:" + path);

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

        starter.or(new TimeoutPredicate(TIMEOUT));
        return starter;
    }

    @Override
    public Runner createStopper() {
        Runner stopper = new Runner(Config.getJboss8Home(), Config.getEnvsWithJavaHome());

        String path = getExecutionPath(Config.getJboss8Home(), BIN_JBOSS_CLI);

        LOGGER.debug("JBOSS shutdown script:" + path);


        stopper.getCommands().add("/bin/bash");
        stopper.getCommands().add(Config.getAbrolutePath("sys-tests/src/test/resources/config/tomcat/killScript.sh"));
        stopper.getCommands().add(serverConfig.getName());

    /*    stopper.getCommands().add(path);
        stopper.getCommands().add("--connect");
        stopper.getCommands().add("controller=localhost:" + actualJbossCliAdminPort);
        stopper.getCommands().add("--command=:shutdown");

        stopper.or(new StringPredicate("{\"outcome\" => \"success\"}") {
            @Override
            public boolean customTest(String input) {
                return match.equals(input);
            }
        });
*/
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
}
