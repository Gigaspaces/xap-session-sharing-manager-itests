package com.gigaspaces.httpsession.qa.newtests.loadbalancertests.stickysession;

import com.gigaspaces.httpsession.qa.newtests.bases.AbstractLoadBalancerTest;
import com.gigaspaces.httpsession.qa.newtests.bases.DeltaStoreModeBase;
import com.gigaspaces.httpsession.qa.newtests.bases.WithoutLoginShiroSecurityConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Yohana Khoury
 * @since 10.1
 */
public class DeltaModeWithoutLoginTest extends AbstractLoadBalancerTest {

    @Override
    @Before
    public void before() {
        super.before();
        this.storeModeBase = new DeltaStoreModeBase();
        this.shiroSecurityConfiguration = new WithoutLoginShiroSecurityConfiguration();
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
