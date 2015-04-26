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
public class ApacheLoadBalancerController extends ServerController {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(ApacheLoadBalancerController.class);
    private static String APACHE_HOME = Config.getApacheHome();
    private final HashMap<Integer, ServerController> serverControllers;

    private String appname;
    private List<String> addresses;
    private String stickySession;

    public ApacheLoadBalancerController(ServerControllerFactory.ServerControllerEnum serverControllerFactory, int[] serversPorts, String appname, List<String> addresses) {
        this(serverControllerFactory, serversPorts, appname, addresses, null);
    }

    public ApacheLoadBalancerController(ServerControllerFactory.ServerControllerEnum serverControllerFactory, int[] serversPorts, String appname, List<String> addresses, String stickySession) {
        this.appname = appname;
        this.addresses = addresses;
        this.stickySession = stickySession;
        this.serverControllers = new HashMap<Integer, ServerController>();
        for (int i=0; i<serversPorts.length; i++) {
            System.out.println("Will start "+serverControllerFactory+" with port "+serversPorts[i]);
            this.serverControllers.put(serversPorts[i], ServerControllerFactory.getServerController(serverControllerFactory, serversPorts[i]));
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
            updateLoadBalancerConfig(appname, addresses, stickySession);
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


    private void updateLoadBalancerConfig(String appname, List<String> addresses, String stickySession) throws IOException {
        ArrayList<String> lines = new ArrayList<String>();
        lines.add("<Proxy balancer://mycluster>");
        for (String address : addresses) {
            lines.add("BalancerMember " + address);
        }
        lines.add("</Proxy>");
        lines.add("SSLProxyEngine on");
        lines.add("SSLProxyVerify none");
        lines.add("ProxyPass /" + appname + " balancer://mycluster" + (stickySession != null ? " stickysession=" + stickySession : ""));
        lines.add("ProxyPassReverse /" + appname + " balancer://mycluster" + (stickySession != null ? " stickysession=" + stickySession : ""));


        String path = APACHE_HOME+"/conf/gigaspaces/httpd-proxy-balancer.conf";

        FileUtils.writeLines(new File(path), lines);
    }

}
