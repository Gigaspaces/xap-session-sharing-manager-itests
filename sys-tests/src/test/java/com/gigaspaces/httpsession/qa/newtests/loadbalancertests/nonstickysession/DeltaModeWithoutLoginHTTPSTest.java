package com.gigaspaces.httpsession.qa.newtests.loadbalancertests.nonstickysession;

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
public class DeltaModeWithoutLoginHTTPSTest extends AbstractLoadBalancerTest {

    @Override
    @Before
    public void before() {
        super.before();
        this.storeModeBase = new DeltaStoreModeBase();
        this.shiroSecurityConfiguration = new WithoutLoginShiroSecurityConfiguration();
        this.webAppAddress = "http://localhost:7777/demo-app";
    }

    @Test
    public void testJettyLoadBalancerHTTPS() throws IOException {
        super.testJettyLoadBalancerHTTPS();
    }

    @Test
    public void testTomcatLoadBalancerHTTPS() throws IOException {
        super.testTomcatLoadBalancerHTTPS();
    }

    @Test
    public void testWebSphereLoadBalancerHTTPS() throws IOException {
        super.testWebSphereLoadBalancerHTTPS();
    }

    @Test
    public void testTomcatLoadBalancerSecurityHTTPS() throws IOException {
        super.testTomcatLoadBalancerSecurityHTTPS();
    }
}
