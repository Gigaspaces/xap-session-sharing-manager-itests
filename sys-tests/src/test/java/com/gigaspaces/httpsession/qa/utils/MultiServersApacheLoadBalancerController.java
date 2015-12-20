package com.gigaspaces.httpsession.qa.utils;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yohana Khoury
 * @since 10.1
 */
public class MultiServersApacheLoadBalancerController extends ServerController {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(MultiServersApacheLoadBalancerController.class);
    private static String APACHE_HOME = Config.getApacheHome();
    private final HashMap<Integer, ServerController> serverControllers;
    private final HashMap<String, String[]> addressesByAppName;
    private String stickySession;

    public MultiServersApacheLoadBalancerController(ServerController[] servercontrollers, HashMap<String, String[]> addressesByAppName) {
        this(servercontrollers, addressesByAppName, null);
    }

    public MultiServersApacheLoadBalancerController(ServerController[] servercontrollers, HashMap<String, String[]> addressesByAppName, String stickySession) {
        this.stickySession = stickySession;
        this.serverControllers = new HashMap<Integer, ServerController>();
        this.addressesByAppName = addressesByAppName;
        for (ServerController serverController : servercontrollers) {
            System.out.println("Will start "+serverController.getClass().getName()+" with port "+serverController.getPort());
            this.serverControllers.put(serverController.getPort(), serverController);
        }
    }

    @Override
    public Runner createStarter() {
        Map<String, String> envs = new HashMap<String, String>();
        Runner starter = new Runner(Config.getApacheHome(), 10000, envs);
        starter.getCommands().add(APACHE_HOME + "/bin/apachectl");
        starter.getCommands().add("start");
        return starter;
    }

    @Override
    public Runner createStopper() {
        Map<String, String> envs = new HashMap<String, String>();
        Runner stopper = new Runner(Config.getApacheHome(), 10000, envs);
        stopper.getCommands().add(APACHE_HOME + "/bin/apachectl");
        stopper.getCommands().add("stop");
        return stopper;
    }

    @Override
    public void deploy(String appName) throws IOException {

    }

    @Override
    public void undeploy(String appName) throws IOException {

    }

    @Override
    public void saveShiroFile(String appName, List<String> lines) throws IOException {

    }

    @Override
    public void startAll(String file, Map<String, String> properties) {
        for (ServerController serverController : serverControllers.values()) {
            serverController.reset();
        }
        for (ServerController serverController : serverControllers.values()) {
            serverController.startAll(file, properties);
        }
        try {
            updateLoadBalancerConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
        start();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopAll(boolean undeploy, boolean undeployOnce) throws IOException {
        for (ServerController serverController : serverControllers.values()) {
            serverController.stopAll(undeploy, true);
        }
        stop();
    }

    @Override
    public void reset() {
        for (ServerController serverController : serverControllers.values()) {
            serverController.reset();
        }
    }

    public void stopOneWebServerWithPort(int port) {
        serverControllers.get(port).stop();
    }


    private void updateLoadBalancerConfig() throws IOException {
        ArrayList<String> lines = new ArrayList<String>();
        for (String appName : addressesByAppName.keySet()) {
            lines.add("<Proxy balancer://mycluster"+appName+">");
            for (String address : addressesByAppName.get(appName)) {
                lines.add("BalancerMember " + address);
            }
            lines.add("</Proxy>");
            lines.add("ProxyPass /" + appName + " balancer://mycluster"+appName + (stickySession != null ? " stickysession=" + stickySession : ""));
            lines.add("ProxyPassReverse /" + appName + " balancer://mycluster"+appName + (stickySession != null ? " stickysession=" + stickySession : ""));
        }
        lines.add("SSLProxyEngine on");
        lines.add("SSLProxyVerify none");



        String path = APACHE_HOME+"/conf/gigaspaces/httpd-proxy-balancer.conf";

        FileUtils.writeLines(new File(path), lines);
    }


    @Override
    public void dumpLogsToDir(File dir) {
        File destination = new File(dir, "apacheloadbalancer.zip");
        LOGGER.info("Dumping logs to " + destination.getAbsolutePath());
        File[] files = new File[0];
        try {
            ZipUtils.zipDirectory(destination , new File(APACHE_HOME + "/logs"), files, files, files,"");
        } catch (IOException e) {
            e.printStackTrace();
        }


        for (ServerController serverController : serverControllers.values()) {
            serverController.dumpLogsToDir(dir);
        }
    }

}
