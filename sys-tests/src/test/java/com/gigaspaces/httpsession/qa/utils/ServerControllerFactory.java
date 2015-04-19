package com.gigaspaces.httpsession.qa.utils;

/**
 * @author Yohana Khoury
 * @since 10.1
 */
public class ServerControllerFactory {
    public enum ServerControllerEnum {
        JETTY9, WEBSPHERE, TOMCAT7, JBOSS7, JBOSS8
    }
    public static ServerController getServerController(ServerControllerEnum serverController, int port) {
        switch (serverController) {
            case JETTY9:
                return new JettyController(port);
            case TOMCAT7:
                return new TomcatController(port);
            case JBOSS7:
                return new JbossController(port);
            case WEBSPHERE:
                return new WebsphereController(port);
            default:
                System.out.println("Unsupported!!");
                return null;
        }
    }
}
