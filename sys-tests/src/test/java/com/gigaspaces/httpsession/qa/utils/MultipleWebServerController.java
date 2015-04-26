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
public class MultipleWebServerController extends ServerController {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(MultipleWebServerController.class);
    private final HashMap<Integer, ServerController> serverControllers;

    public MultipleWebServerController(ServerController[] servercontrollers) {
        this.serverControllers = new HashMap<Integer, ServerController>();
        for (ServerController serverController : servercontrollers) {
            System.out.println("Will start "+serverController.getClass().getName()+" with port "+serverController.getPort());
            this.serverControllers.put(serverController.getPort(), serverController);
        }
    }

    @Override
    public Runner createStarter() {
        return null;
    }

    @Override
    public Runner createStopper() {
        return null;
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
    }

    @Override
    public void stopAll(boolean undeploy, boolean undeployOnce) throws IOException {
        for (ServerController serverController : serverControllers.values()) {
            serverController.stopAll(undeploy, true);
        }
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


}
