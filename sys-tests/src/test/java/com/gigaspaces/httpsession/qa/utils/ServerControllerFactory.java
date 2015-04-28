package com.gigaspaces.httpsession.qa.utils;

/**
 * @author Yohana Khoury
 * @since 10.1
 */
public class ServerControllerFactory {
    public enum ServerControllerEnum {
        JETTY9, JETTY9HTTPS,JETTY9SPRINGSECURITY,
        WEBSPHERE, WEBSPHEREHTTPS,WEBSHERESPRINGSECURITY,
        TOMCAT7, TOMCAT7HTTPS, TOMCAT7SECURITY, TOMCAT7SECURITYHTTPS,
        TOMCAT7SPRINGSECURITY,
        JBOSS7, JBOSS8, JBOSS7SPRINGSECURITY
    }
    public static ServerController getServerController(ServerControllerEnum serverController, int port) {
        return getServerController(serverController, port, ServerController.DEFAULT_APP_NAME);
    }

    public static ServerController getServerController(ServerControllerEnum serverController, int port, String appName) {
        switch (serverController) {
            case JETTY9:
                return new JettyController(port, appName);
            case JETTY9HTTPS:
                return new JettyHTTPSController(port, appName);
            case JETTY9SPRINGSECURITY:
                return new JettyController(port, true, appName);
            case TOMCAT7:
                return new TomcatController(port, appName);
            case TOMCAT7SECURITY:
                return new TomcatController(port, true, appName);
            case TOMCAT7HTTPS:
                return new TomcatHTTPSController(port, appName);
            case TOMCAT7SPRINGSECURITY:
                return new TomcatController(port, false, true, appName);
            case TOMCAT7SECURITYHTTPS:
                return new TomcatHTTPSController(port, true, appName);
            case JBOSS7:
            return new JbossController(port, appName);
            case JBOSS7SPRINGSECURITY:
                return new JbossController(port, appName);
            case JBOSS8:
            return new JBoss8Controller(port, appName);
            case WEBSPHERE:
            return new WebsphereController(port, appName);
            case WEBSHERESPRINGSECURITY:
                return new WebsphereController(port, true, appName);
            case WEBSPHEREHTTPS:
            return new WebsphereHTTPSController(port, appName);
            default:
                System.out.println("Unsupported!!");
                return null;
        }
    }
}
