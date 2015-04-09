package com.gigaspaces.httpsession.qa.newtests.withoutloadbalancer;

import com.gigaspaces.httpsession.qa.newtests.DeltaStoreModeBase;
import com.gigaspaces.httpsession.qa.newtests.TestBase;
import com.gigaspaces.httpsession.qa.newtests.WithLoginShiroSecurityConfiguration;
import com.gigaspaces.httpsession.qa.utils.JettyController;
import com.gigaspaces.httpsession.qa.utils.TomcatController;
import com.gigaspaces.httpsession.qa.utils.WebsphereController;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

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
    public void testJetty() throws IOException {

        startWebServer(new JettyController(7777));
        test();
    }

    @Test
    public void testTomcat() throws IOException {
        startWebServer(new TomcatController(7777));

        test();
    }

    @Test
    public void testWebSphere() throws IOException {
        startWebServer(new WebsphereController(7777));

        test();
    }



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


}
