package com.gigaspaces.httpsession.qa.newtests.withoutloadbalancer;

import com.gigaspaces.httpsession.qa.newtests.bases.AbstractTestBase;
import com.gigaspaces.httpsession.qa.newtests.bases.FullStoreModeBase;
import com.gigaspaces.httpsession.qa.newtests.bases.WithoutLoginShiroSecurityConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Yohana Khoury
 * @since 10.1
 */
public class FullModeWithoutLoginHTTPSTest extends AbstractTestBase {

    @Override
    @Before
    public void before() {
        super.before();
        this.storeModeBase = new FullStoreModeBase();
        this.shiroSecurityConfiguration = new WithoutLoginShiroSecurityConfiguration();
        this.webAppAddress = "https://localhost:7778/demo-app";
    }
    @Test
    public void testJettyHTTPS() throws IOException {
        super.testJettyHTTPS();
    }

    @Test
    public void testTomcatHTTPS() throws IOException {
        super.testTomcatHTTPS();
    }

    @Test
    public void testWebSphereHTTPS() throws IOException {
        super.testWebSphereHTTPS();
    }


}
