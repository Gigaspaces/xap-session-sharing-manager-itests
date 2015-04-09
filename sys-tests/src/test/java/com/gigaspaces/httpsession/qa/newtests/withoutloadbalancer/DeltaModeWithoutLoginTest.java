package com.gigaspaces.httpsession.qa.newtests.withoutloadbalancer;

import com.gigaspaces.httpsession.qa.newtests.DeltaStoreModeBase;
import com.gigaspaces.httpsession.qa.newtests.TestBase;
import com.gigaspaces.httpsession.qa.newtests.WithoutLoginShiroSecurityConfiguration;
import com.gigaspaces.httpsession.qa.utils.JettyController;
import com.gigaspaces.httpsession.qa.utils.TomcatController;
import com.gigaspaces.httpsession.qa.utils.WebsphereController;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Yohana Khoury
 * @since 10.1
 */
public class DeltaModeWithoutLoginTest extends TestBase {

    @Override
    @Before
    public void before() {
        System.out.println("BEFORE");
        super.before();
        System.out.println("BEFORE");
        this.storeModeBase = new DeltaStoreModeBase();
        System.out.println("BEFORE");
        this.shiroSecurityConfiguration = new WithoutLoginShiroSecurityConfiguration();
        System.out.println("BEFORE");

        this.webAppAddress = "http://localhost:7777/demo-app";
    }
    @Test
    public void testJetty() throws IOException {
        System.out.println("STARTED TEST");
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


}
