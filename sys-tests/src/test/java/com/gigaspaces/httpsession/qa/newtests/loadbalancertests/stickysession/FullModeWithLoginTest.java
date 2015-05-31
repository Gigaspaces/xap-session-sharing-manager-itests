package com.gigaspaces.httpsession.qa.newtests.loadbalancertests.stickysession;

import com.gigaspaces.httpsession.qa.newtests.bases.AbstractLoadBalancerTest;
import com.gigaspaces.httpsession.qa.newtests.bases.FullStoreModeBase;
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
public class FullModeWithLoginTest extends AbstractLoadBalancerTest {

    @Override
    @Before
    public void before() {
        super.before();
        this.storeModeBase = new FullStoreModeBase();
        this.shiroSecurityConfiguration = new WithLoginShiroSecurityConfiguration();
        this.webAppAddress = "http://localhost:7778/demo-app";
    }

    @Test
    public void testJettyLoadBalancerStickySession() throws IOException {
        super.testJettyLoadBalancerStickySession();
    }

    @Test
    public void testTomcatLoadBalancerStickySession() throws IOException {
        super.testTomcatLoadBalancerStickySession();
    }

    @Test
    public void testWebSphereLoadBalancerStickySession() throws IOException {
        super.testWebSphereLoadBalancerStickySession();
    }

    @Test
    public void testTomcatLoadBalancerSecurity() throws IOException {
        super.testTomcatLoadBalancerSecurity();
    }
}
