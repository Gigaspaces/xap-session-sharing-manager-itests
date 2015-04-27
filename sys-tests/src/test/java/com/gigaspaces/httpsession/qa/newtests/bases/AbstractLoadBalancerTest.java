package com.gigaspaces.httpsession.qa.newtests.bases;

import com.gigaspaces.httpsession.qa.utils.ApacheLoadBalancerController;
import com.gigaspaces.httpsession.qa.utils.ServerControllerFactory;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Yohana Khoury
 * @since 10.1
 */

public class AbstractLoadBalancerTest extends TestBase {
    private String stickySession = "JSESSIONID";

    public void testJettyLoadBalancer() throws IOException {

        startWebServer(new ApacheLoadBalancerController(
                ServerControllerFactory.ServerControllerEnum.JETTY9,
                new int[]{8080,8082},
                "demo-app",
                Arrays.asList("http://localhost:8080/demo-app http://localhost:8082/demo-app".split(" "))
        ));
        test();
    }

    public void testTomcatLoadBalancer() throws IOException {

        startWebServer(new ApacheLoadBalancerController(
                ServerControllerFactory.ServerControllerEnum.TOMCAT7,
                new int[]{8080,8082},
                "demo-app",
                Arrays.asList("http://localhost:8080/demo-app http://localhost:8082/demo-app".split(" "))
        ));
        test();
    }

    public void testWebSphereLoadBalancer() throws IOException {

        startWebServer(new ApacheLoadBalancerController(
                ServerControllerFactory.ServerControllerEnum.WEBSPHERE,
                new int[]{8080,8082},
                "demo-app",
                Arrays.asList("http://localhost:8080/demo-app http://localhost:8082/demo-app".split(" "))
        ));
        test();
    }

    /*
    Here starts tests with Sticky Session
     */

    public void testJettyLoadBalancerStickySession() throws IOException {

        startWebServer(new ApacheLoadBalancerController(
                ServerControllerFactory.ServerControllerEnum.JETTY9,
                new int[]{8080, 8082},
                "demo-app",
                Arrays.asList("http://localhost:8080/demo-app http://localhost:8082/demo-app".split(" ")),
                stickySession
        ));
        test();
    }

    public void testTomcatLoadBalancerStickySession() throws IOException {

        startWebServer(new ApacheLoadBalancerController(
                ServerControllerFactory.ServerControllerEnum.TOMCAT7,
                new int[]{8080,8082},
                "demo-app",
                Arrays.asList("http://localhost:8080/demo-app http://localhost:8082/demo-app".split(" ")),
                stickySession
        ));
        test();
    }

    public void testWebSphereLoadBalancerStickySession() throws IOException {

        startWebServer(new ApacheLoadBalancerController(
                ServerControllerFactory.ServerControllerEnum.WEBSPHERE,
                new int[]{8080,8082},
                "demo-app",
                Arrays.asList("http://localhost:8080/demo-app http://localhost:8082/demo-app".split(" ")),
                stickySession
        ));
        test();
    }

    /*
    Here starts tests with HTTPS
     */

    public void testJettyLoadBalancerHTTPS() throws IOException {

        startWebServer(new ApacheLoadBalancerController(
                ServerControllerFactory.ServerControllerEnum.JETTY9HTTPS,
                new int[]{8080,8082},
                "demo-app",
                Arrays.asList("https://localhost:8080/demo-app https://localhost:8082/demo-app".split(" "))
        ));
        test();
    }


    public void testTomcatLoadBalancerHTTPS() throws IOException {

        startWebServer(new ApacheLoadBalancerController(
                ServerControllerFactory.ServerControllerEnum.TOMCAT7HTTPS,
                new int[]{8080,8082},
                "demo-app",
                Arrays.asList("https://localhost:8080/demo-app https://localhost:8082/demo-app".split(" "))
        ));
        test();
    }




    public void testWebSphereLoadBalancerHTTPS() throws IOException {

        startWebServer(new ApacheLoadBalancerController(
                ServerControllerFactory.ServerControllerEnum.WEBSPHEREHTTPS,
                new int[]{8080,8082},
                "demo-app",
                Arrays.asList("https://localhost:8080/demo-app https://localhost:8082/demo-app".split(" "))
        ));
        test();
    }

    /*
    HTTPS with sticky session
     */

    public void testJettyLoadBalancerStickySessionHTTPS() throws IOException {

        startWebServer(new ApacheLoadBalancerController(
                ServerControllerFactory.ServerControllerEnum.JETTY9HTTPS,
                new int[]{8080, 8082},
                "demo-app",
                Arrays.asList("https://localhost:8080/demo-app https://localhost:8082/demo-app".split(" ")),
                stickySession
        ));
        test();
    }

    public void testTomcatLoadBalancerStickySessionHTTPS() throws IOException {

        startWebServer(new ApacheLoadBalancerController(
                ServerControllerFactory.ServerControllerEnum.TOMCAT7HTTPS,
                new int[]{8080,8082},
                "demo-app",
                Arrays.asList("https://localhost:8080/demo-app https://localhost:8082/demo-app".split(" ")),
                stickySession
        ));
        test();
    }

    public void testWebSphereLoadBalancerStickySessionHTTPS() throws IOException {

        startWebServer(new ApacheLoadBalancerController(
                ServerControllerFactory.ServerControllerEnum.WEBSPHEREHTTPS,
                new int[]{8080,8082},
                "demo-app",
                Arrays.asList("https://localhost:8080/demo-app https://localhost:8082/demo-app".split(" ")),
                stickySession
        ));
        test();
    }

    /*
    Spring Security
     */
    public void testTomcatLoadBalancerNonStickySessionSpringSecurity() throws IOException {

        startWebServer(new ApacheLoadBalancerController(
                ServerControllerFactory.ServerControllerEnum.TOMCAT7SPRINGSECURITY,
                new int[]{8080,8082},
                "demo-app",
                Arrays.asList("http://localhost:8080/demo-app http://localhost:8082/demo-app".split(" "))
        ));
        test();
    }

    public void testJettyLoadBalancerNonStickySessionSpringSecurity() throws IOException {

        startWebServer(new ApacheLoadBalancerController(
                ServerControllerFactory.ServerControllerEnum.JETTY9SPRINGSECURITY,
                new int[]{8080,8082},
                "demo-app",
                Arrays.asList("http://localhost:8080/demo-app http://localhost:8082/demo-app".split(" "))
        ));
        test();
    }


    /*
    Here starts tests with secure webserver
     */

    public void testTomcatLoadBalancerSecurity() throws IOException {

        startWebServer(new ApacheLoadBalancerController(
                ServerControllerFactory.ServerControllerEnum.TOMCAT7SECURITY,
                new int[]{8080,8082},
                "demo-app",
                Arrays.asList("http://localhost:8080/demo-app http://localhost:8082/demo-app".split(" "))
        ));
        test();
    }

    public void testTomcatLoadBalancerSecurityHTTPS() throws IOException {

        startWebServer(new ApacheLoadBalancerController(
                ServerControllerFactory.ServerControllerEnum.TOMCAT7SECURITYHTTPS,
                new int[]{8080,8082},
                "demo-app",
                Arrays.asList("https://localhost:8080/demo-app https://localhost:8082/demo-app".split(" "))
        ));
        test();
    }

}
