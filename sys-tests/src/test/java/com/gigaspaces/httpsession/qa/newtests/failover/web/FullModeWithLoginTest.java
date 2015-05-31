package com.gigaspaces.httpsession.qa.newtests.failover.web;

import com.gigaspaces.httpsession.qa.newtests.bases.AbstractLoadBalancerTest;
import com.gigaspaces.httpsession.qa.newtests.bases.FullStoreModeBase;
import com.gigaspaces.httpsession.qa.newtests.bases.WithLoginShiroSecurityConfiguration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Yohana Khoury
 * @since 10.2
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
    public void testJettyLoadBalancerOneWebServerOutOfManyFailover() throws Exception {
        super.testJettyLoadBalancerOneWebServerOutOfManyFailover();
    }

    @Test
    public void testTomcatLoadBalancerOneWebServerOutOfManyFailover() throws Exception {
        super.testTomcatLoadBalancerOneWebServerOutOfManyFailover();
    }

    @Test
    public void testWebSphereLoadBalancerOneWebServerOutOfManyFailover() throws Exception {
        super.testWebSphereLoadBalancerOneWebServerOutOfManyFailover();
    }

    @Test
    public void testJBoss7LoadBalancerOneWebServerOutOfManyFailover() throws Exception {
        super.testJBoss7LoadBalancerOneWebServerOutOfManyFailover();
    }

    @Test
    public void testJBoss8LoadBalancerOneWebServerOutOfManyFailover() throws Exception {
        super.testJBoss8LoadBalancerOneWebServerOutOfManyFailover();
    }

    @Test
    public void testJettyLoadBalancerWebServersFailover() throws Exception {
        super.testJettyLoadBalancerWebServersFailover();
    }

    @Test
    public void testTomcatLoadBalancerWebServersFailover() throws Exception {
        super.testTomcatLoadBalancerWebServersFailover();
    }

    @Test
    public void testWebSphereLoadBalancerWebServersFailover() throws Exception {
        super.testWebSphereLoadBalancerWebServersFailover();
    }

    @Test
    public void testJBoss7LoadBalancerWebServersFailover() throws Exception {
        super.testJBoss7LoadBalancerWebServersFailover();
    }

    @Test
    public void testJBoss8LoadBalancerWebServersFailover() throws Exception {
        super.testJBoss8LoadBalancerWebServersFailover();
    }
}
