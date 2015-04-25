package com.gigaspaces.httpsession.qa.newtests.loadbalancertests.stickysession;

import com.gigaspaces.httpsession.qa.newtests.bases.AbstractLoadBalancerTest;
import com.gigaspaces.httpsession.qa.newtests.bases.FullStoreModeBase;
import com.gigaspaces.httpsession.qa.newtests.bases.WithoutLoginShiroSecurityConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Yohana Khoury
 * @since 10.1
 */
public class FullModeWithoutLoginHTTPSTest extends AbstractLoadBalancerTest {

    @Override
    @Before
    public void before() {
        super.before();
        this.storeModeBase = new FullStoreModeBase();
        this.shiroSecurityConfiguration = new WithoutLoginShiroSecurityConfiguration();
        this.webAppAddress = "http://localhost:7777/demo-app";
    }

    @Test
    public void testJettyLoadBalancerStickySessionHTTPS() throws IOException {
        super.testJettyLoadBalancerStickySessionHTTPS();
    }

    @Test
    public void testTomcatLoadBalancerStickySessionHTTPS() throws IOException {
        super.testTomcatLoadBalancerStickySessionHTTPS();
    }

    @Test
    public void testWebSphereLoadBalancerStickySessionHTTPS() throws IOException {
        super.testWebSphereLoadBalancerStickySessionHTTPS();
    }
}
