package com.gigaspaces.httpsession.qa.newtests.withoutloadbalancer;

import com.gigaspaces.httpsession.qa.newtests.bases.AbstractTestBase;
import com.gigaspaces.httpsession.qa.newtests.bases.DeltaStoreModeBase;
import com.gigaspaces.httpsession.qa.newtests.bases.WithLoginShiroSecurityConfiguration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Yohana Khoury
 * @since 10.1
 */
@Ignore
public class DeltaModeWithLoginHTTPSTest extends AbstractTestBase {

    @Override
    @Before
    public void before() {
        super.before();
        this.storeModeBase = new DeltaStoreModeBase();
        this.shiroSecurityConfiguration = new WithLoginShiroSecurityConfiguration();
        this.webAppAddress = "https://localhost:7777/demo-app";
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
