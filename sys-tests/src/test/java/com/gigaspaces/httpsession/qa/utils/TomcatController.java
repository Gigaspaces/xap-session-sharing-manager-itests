package com.gigaspaces.httpsession.qa.utils;

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

public class TomcatController extends ServerController {

    private static final String DEFAULT_SERVER_CONFIG = "sys-tests/src/test/resources/config/tomcat/server.xml";
    private static final String TEST_SECURED_POLICY = "src/test/resources/config/tomcat/catalina-security.policy";
    private static final String SECURED_POLICY_ORG = "catalina.policy";
    private static final String STARTED_COMPLETED = "org.apache.catalina.startup.Catalina start";
    private static final String DESTROYING_COMPLETED = "Destroying ProtocolHandler";
    private static final String BIN_CATALINA = "bin/catalina";
    protected File serverConfig;
    public final String TOMCAT_WEB_APPS = FilenameUtils.concat(
            getTomcatHome(), WEB_APPS);

    private static AtomicInteger instancesCount = new AtomicInteger(0);
    protected int currentInstance;

    public TomcatController(String host, int port, boolean isSecured, boolean isSpringSecurity) {
        super(host, port, isSecured, isSpringSecurity);
    }

    public TomcatController(String host, int port) {
        this(host, port, false, false);
    }

    public TomcatController(int port) {
        super(port);
    }

    public TomcatController(int port, String appName) {
        super(port, appName);
    }

    public TomcatController(int port, boolean isSecured) {
        super(port, isSecured);
    }

    public TomcatController(int port, boolean isSecured, String appName) {
        super(port, isSecured, appName);
    }
    public TomcatController(int port, boolean isSecured, boolean isSpringSecurity) {
        super(port, isSecured, isSpringSecurity);
    }

    public TomcatController(int port, boolean isSecured, boolean isSpringSecurity, String appName) {
        super(port, isSecured, isSpringSecurity, appName);
    }

    public String getTomcatHome() {
        return Config.getTomcatHome() + "-" + (currentInstance % 2 + 1);
    }


    @Override
    public Runner createStarter() {

        try {
            currentInstance = instancesCount.getAndIncrement();
            //TODO put these files in the tests directory
            serverConfig = File.createTempFile("tomcat-server", ".xml", new File("/tmp/"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Runner starter = new Runner(getTomcatHome(), Config.getEnvsWithJavaHome());
        starter.setWaitForTermination(false);


        String path = getExecutionPath(getTomcatHome(), BIN_CATALINA);

        LOGGER.debug("Tomcat start script:" + path);

        List<String> commands = starter.getCommands();

        commands.add(path);
        commands.add("run");
        if (isSecured())
            commands.add("-security");
        commands.add("-config");
        commands.add(serverConfig.getAbsolutePath());

        starter.or(new StringPredicate(STARTED_COMPLETED) {
            @Override
            public boolean customTest(String input) {
                return input.endsWith(match);
            }
        });

        return starter;
    }

    @Override
    public Runner createStopper() {

        Runner stopper = new Runner(getTomcatHome(), Config.getEnvsWithJavaHome());


        String path = getExecutionPath(getTomcatHome(), BIN_CATALINA);

        LOGGER.debug("Tomcat stop script:" + path);

        List<String> commands = stopper.getCommands();

        commands.add(path);
        //commands.add("-force");
        commands.add("stop");
        commands.add("-config");
        commands.add(Config.getAbrolutePath(serverConfig.getAbsolutePath()));

        /*commands.add("/bin/bash");
        commands.add(Config.getAbrolutePath("sys-tests/src/test/resources/config/tomcat/killScript.sh"));
        commands.add(serverConfig.getAbsolutePath());
*/

     /*   stopper.or(new StringPredicate(DESTROYING_COMPLETED) {
            @Override
            public boolean customTest(String input) {
                return input.endsWith(match);
            }
        });*/

        return stopper;
    }

    @Override
    public void deploy(String appName) throws IOException {
        FileUtils.copyDirectory(new File(Config.getAbrolutePath(WEB_APP_SOURCE)), new File(
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
    public void start() {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(Config.getAbrolutePath(DEFAULT_SERVER_CONFIG));
            String content = IOUtils.toString(fis, "UTF-8");
            content = content.replaceAll("9005", "" + (9005 + currentInstance));
            content = content.replaceAll("9090", "" + port);
            content = content.replaceAll("9009", "" + (9009 + currentInstance));
            fos = new FileOutputStream(serverConfig);
            IOUtils.write(content, fos, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    LOGGER.warn("FileInputStream close throws an exception", e);
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    LOGGER.warn("FileOutputStream close throws an exception", e);
                }
            }
        }
        super.start();
    }

    @Override
    public void startAll(String file, Map<String, String> properties) {
        if (isSecured()) {
            try {
                File securedPolicy = new File(getTomcatHome() + File.separator + "conf" + File.separator + SECURED_POLICY_ORG);
                securedPolicy.renameTo(new File(getTomcatHome() + File.separator + "conf" + File.separator + SECURED_POLICY_ORG + ".org"));
                FileUtils.copyFile(new File(TEST_SECURED_POLICY), new File(getTomcatHome() + File.separator + "conf" + File.separator + SECURED_POLICY_ORG));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            deploy(appName);
            saveWebXmlFile(appName, TOMCAT_WEB_APPS);
            saveSpringSecurityFile(appName, TOMCAT_WEB_APPS);
        } catch (IOException e) {
            e.printStackTrace();
        }
        config(file, properties);
        start();
    }

    @Override
    public void stopAll(boolean undeploy, boolean undeployOnce) throws IOException {
        stop();
        if (undeploy) {
            undeploy(appName);

            if (isSecured()) {
                try {
                    File securedPolicy = new File(getTomcatHome() + File.separator + "conf" + File.separator + SECURED_POLICY_ORG);
                    FileUtils.forceDelete(securedPolicy);

                    File orgSecuredPolicy = new File(getTomcatHome() + File.separator + "conf" + File.separator + SECURED_POLICY_ORG + ".org");
                    orgSecuredPolicy.renameTo(new File(getTomcatHome() + File.separator + "conf" + File.separator + SECURED_POLICY_ORG));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public void dumpLogsToDir(File dir) {
        File destination = new File(dir, "tomcat" + currentInstance+".zip");
        LOGGER.info("Dumping logs to " + destination.getAbsolutePath());
        File[] files = new File[0];
        try {
            ZipUtils.zipDirectory(destination , new File(getTomcatHome()+"/logs"), files, files, files,"");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void undeploy(String appName) throws IOException {
        File serverDir = new File(FilenameUtils.concat(TOMCAT_WEB_APPS, appName));
        Assert.assertTrue("Failed to find server's directory [" + appName + "]", serverDir.exists());
        FileUtils.forceDelete(serverDir);
    }
}
