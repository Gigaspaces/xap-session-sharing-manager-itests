package com.gigaspaces.httpsession.qa.newtests.multiplewebapps;

import com.gigaspaces.httpsession.qa.newtests.bases.AbstractTestBase;
import com.gigaspaces.httpsession.qa.newtests.bases.DeltaStoreModeBase;
import com.gigaspaces.httpsession.qa.newtests.bases.WithLoginShiroSecurityConfiguration;
import com.gigaspaces.httpsession.qa.utils.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author Yohana Khoury
 * @since 10.1
 */
public class DeltaModeWithLoginTest extends AbstractTestBase {
    @Override
    @Before
    public void before() {
        super.before();
        this.storeModeBase = new DeltaStoreModeBase();
        this.shiroSecurityConfiguration = new WithLoginShiroSecurityConfiguration();
    }

    //@Test
    //This does not work because of the BUG with LB and login
    public void testMultiLB() {
        HashMap<String, String[]> addressesByAppName = new HashMap<String, String[]>();
        addressesByAppName.put("demo-app", new String[]{"http://localhost:8080/demo-app", "http://localhost:8082/demo-app"});
        addressesByAppName.put("demo-app2", new String[]{"http://localhost:9080/demo-app2", "http://localhost:9082/demo-app2"});
        startWebServer(new MultiServersApacheLoadBalancerController(
                new ServerController[]{
                        new TomcatController(8080, "demo-app"),
                        new TomcatController(8082, "demo-app"),
                        new TomcatController(9080, "demo-app2"),
                        new TomcatController(9082, "demo-app2")
                },
                addressesByAppName
        ));
    }

    @Test
    public void testMultiTomcat() throws IOException {
        startWebServer(new MultipleWebServerController(
                new ServerController[]{
                        new TomcatController(8080, "demo-app"),
                        new TomcatController(8082, "demo-app2"),
                }));
        testTwoWebAppsSharingAttributes();
    }

    @Test
    public void testMultiJetty() throws IOException {
        startWebServer(new MultipleWebServerController(
                new ServerController[]{
                        new JettyController(8080, "demo-app"),
                        new JettyController(8082, "demo-app2"),
                }));
        testTwoWebAppsSharingAttributes();
    }


    @Test
    public void testMultiWebsphere() throws IOException {
        startWebServer(new MultipleWebServerController(
                new ServerController[]{
                        new WebsphereController(8080, "demo-app"),
                        new WebsphereController(8082, "demo-app2"),
                }));
        testTwoWebAppsSharingAttributes();
    }


    public void testTwoWebAppsSharingAttributes() throws IOException {
        this.webAppAddress = "http://localhost:8080/demo-app";
        test();

        createSessions();
        this.isLoggedIn = false;
        this.webAppAddress = "http://localhost:8082/demo-app2";
        test();

        createSessions();
        this.isLoggedIn = false;
        this.webAppAddress = "http://localhost:8080/demo-app";
        test();
    }

}
