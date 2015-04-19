package com.gigaspaces.httpsession.qa.newtests.loadbalancertests.nonstickysession;

import com.gigaspaces.httpsession.qa.newtests.FullStoreModeBase;
import com.gigaspaces.httpsession.qa.newtests.TestBase;
import com.gigaspaces.httpsession.qa.newtests.WithoutLoginShiroSecurityConfiguration;
import com.gigaspaces.httpsession.qa.utils.ApacheLoadBalancerController;
import com.gigaspaces.httpsession.qa.utils.ServerControllerFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Kobi Kisos
 * @since 10.1
 */
public class SoapUIFullModeWithoutLoginTest extends TestBase {

    @Override
    @Before
    public void before() {
        super.before();
        this.storeModeBase = new FullStoreModeBase();
        this.shiroSecurityConfiguration = new WithoutLoginShiroSecurityConfiguration();
        this.webAppAddress = "http://localhost:7777/demo-app";
    }
    @Test
    public void testJettyLoadBalancer() throws Exception {

        startWebServer(new ApacheLoadBalancerController(
                ServerControllerFactory.ServerControllerEnum.JETTY9,
                new int[]{8080,8082},
                "demo-app",
                Arrays.asList("http://localhost:8080/demo-app http://localhost:8082/demo-app".split(" "))
        ));
        soapUITest();
    }

    @Test
    public void testTomcatLoadBalancer() throws Exception {

        startWebServer(new ApacheLoadBalancerController(
                ServerControllerFactory.ServerControllerEnum.TOMCAT7,
                new int[]{8080,8082},
                "demo-app",
                Arrays.asList("http://localhost:8080/demo-app http://localhost:8082/demo-app".split(" "))
        ));
        soapUITest();
    }

    @Test
    public void testWebSphereLoadBalancer() throws Exception {

        startWebServer(new ApacheLoadBalancerController(
                ServerControllerFactory.ServerControllerEnum.WEBSPHERE,
                new int[]{8080,8082},
                "demo-app",
                Arrays.asList("http://localhost:8080/demo-app http://localhost:8082/demo-app".split(" "))
        ));
        soapUITest();
    }
}
