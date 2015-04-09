package com.gigaspaces.httpsession.qa.newtests.loadbalancertests.nonstickysession;

import com.gigaspaces.httpsession.qa.newtests.DeltaStoreModeBase;
import com.gigaspaces.httpsession.qa.newtests.TestBase;
import com.gigaspaces.httpsession.qa.newtests.WithLoginShiroSecurityConfiguration;
import com.gigaspaces.httpsession.qa.newtests.WithoutLoginShiroSecurityConfiguration;
import com.gigaspaces.httpsession.qa.utils.ApacheLoadBalancerController;
import com.gigaspaces.httpsession.qa.utils.ServerControllerFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Yohana Khoury
 * @since 10.1
 */
@Ignore
public class DeltaModeWithLoginTest extends TestBase {

    @Override
    @Before
    public void before() {
        super.before();
        this.storeModeBase = new DeltaStoreModeBase();
        this.shiroSecurityConfiguration = new WithLoginShiroSecurityConfiguration();
        this.webAppAddress = "http://localhost:7777/demo-app";
    }
    @Test
    public void testJettyLoadBalancer() throws IOException {

        startWebServer(new ApacheLoadBalancerController(
                ServerControllerFactory.ServerControllerEnum.JETTY9,
                new int[]{8080,8082},
                "demo-app",
                Arrays.asList("http://localhost:8080/demo-app http://localhost:8082/demo-app".split(" "))
        ));
        test();
    }

    @Test
    public void testTomcatLoadBalancer() throws IOException {

        startWebServer(new ApacheLoadBalancerController(
                ServerControllerFactory.ServerControllerEnum.TOMCAT7,
                new int[]{8080,8082},
                "demo-app",
                Arrays.asList("http://localhost:8080/demo-app http://localhost:8082/demo-app".split(" "))
        ));
        test();
    }

    @Test
    public void testWebSphereLoadBalancer() throws IOException {

        startWebServer(new ApacheLoadBalancerController(
                ServerControllerFactory.ServerControllerEnum.WEBSPHERE,
                new int[]{8080,8082},
                "demo-app",
                Arrays.asList("http://localhost:8080/demo-app http://localhost:8082/demo-app".split(" "))
        ));
        test();
    }
}
