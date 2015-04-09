package com.gigaspaces.httpsession.qa.newtests;

import com.gigaspaces.httpsession.qa.utils.*;
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
public class TempTestCase extends TestBase {

    @Override
    //@Before
    public void before() {
        super.before();
        this.storeModeBase = new DeltaStoreModeBase();
        this.shiroSecurityConfiguration = new WithoutLoginShiroSecurityConfiguration();
    }
    //@Test
    public void test() throws IOException {
        this.webAppAddress = "http://localhost:7777/demo-app";

        //startWebServer(new JettyController(7777));

        /*startWebServer(new ApacheLoadBalancerController(
                ServerControllerFactory.ServerControllerEnum.JETTY9,
                new int[]{8080,8082},
                "demo-app",
                Arrays.asList("http://localhost:8080/demo-app http://localhost:8082/demo-app".split(" "))
        ));*/

        //startWebServer(new TomcatController(7777));

/*        startWebServer(new ApacheLoadBalancerController(
                ServerControllerFactory.ServerControllerEnum.TOMCAT7,
                new int[]{8080,8082},
                "demo-app",
                Arrays.asList("http://localhost:8080/demo-app http://localhost:8082/demo-app".split(" "))
        ));*/

        //DOES NOT WORK!!
        //startWebServer(new JbossController(7777));

        //startWebServer(new WebsphereController(7777));
/*
        startWebServer(new ApacheLoadBalancerController(
                ServerControllerFactory.ServerControllerEnum.WEBSPHERE,
                new int[] {8080, 8082},
                "demo-app",
                Arrays.asList("http://localhost:8080/demo-app http://localhost:8082/demo-app".split(" "))
        ));
*/

        super.test();
    }



}
