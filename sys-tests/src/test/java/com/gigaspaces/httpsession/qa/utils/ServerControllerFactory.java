package com.gigaspaces.httpsession.qa.utils;

/**
 * @author Yohana Khoury
 * @since 10.1
 */
public class ServerControllerFactory {
    public enum ServerControllerEnum {
        JETTY9, JETTY9HTTPS, WEBSPHERE, WEBSPHEREHTTPS, TOMCAT7, TOMCAT7HTTPS, JBOSS7, JBOSS8, TOMCAT7SPRINGSECURITY
    }
    public static ServerController getServerController(ServerControllerEnum serverController, int port) {
        switch (serverController) {
            case JETTY9:
                return new JettyController(port);
            case JETTY9HTTPS:
                return new JettyHTTPSController(port);
            case TOMCAT7:
                return new TomcatController(port);
            case TOMCAT7HTTPS:
                return new TomcatHTTPSController(port);
            case TOMCAT7SPRINGSECURITY:
                return new TomcatController(port, false, true);
            case JBOSS7:
                return new JbossController(port);
            case JBOSS8:
                return new JBoss8Controller(port);
            case WEBSPHERE:
                return new WebsphereController(port);
            case WEBSPHEREHTTPS:
                return new WebsphereHTTPSController(port);
            default:
                System.out.println("Unsupported!!");
                return null;
        }
    }
}
