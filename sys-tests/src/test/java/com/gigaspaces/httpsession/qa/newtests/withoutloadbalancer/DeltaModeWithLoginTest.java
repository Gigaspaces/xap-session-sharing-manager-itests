package com.gigaspaces.httpsession.qa.newtests.withoutloadbalancer;

import com.gigaspaces.httpsession.qa.newtests.bases.AbstractTestBase;
import com.gigaspaces.httpsession.qa.newtests.bases.DeltaStoreModeBase;
import com.gigaspaces.httpsession.qa.newtests.bases.WithLoginShiroSecurityConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Yohana Khoury
 * @since 10.2
 */
public class DeltaModeWithLoginTest extends AbstractTestBase {

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
        super.testJetty();
    }

    @Test
    public void testTomcat() throws IOException {
        super.testTomcat();
    }

    @Test
    public void testWebSphere() throws IOException {
        super.testWebSphere();
    }


}
