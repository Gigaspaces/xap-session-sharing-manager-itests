package com.gigaspaces.httpsession.qa.newtests.withoutloadbalancer;

import com.gigaspaces.httpsession.qa.newtests.bases.AbstractTestBase;
import com.gigaspaces.httpsession.qa.newtests.bases.FullStoreModeBase;
import com.gigaspaces.httpsession.qa.newtests.bases.WithLoginShiroSecurityConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Yohana Khoury
 * @since 10.1
 */
public class FullModeWithLoginTest extends AbstractTestBase {

    @Override
    @Before
    public void before() {
        super.before();
        this.storeModeBase = new FullStoreModeBase();
        this.shiroSecurityConfiguration = new WithLoginShiroSecurityConfiguration();
        this.webAppAddress = "http://localhost:7778/demo-app";
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
