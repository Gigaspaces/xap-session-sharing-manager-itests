package com.gigaspaces.httpsession.qa.newtests.loadbalancertests.nonstickysession;

import com.gigaspaces.httpsession.qa.newtests.bases.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Yohana Khoury
 * @since 10.2
 */
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
    public void testJettyLoadBalancer() throws IOException {
        super.testJettyLoadBalancer();
    }

    @Test
    public void testTomcatLoadBalancer() throws IOException {
        super.testTomcatLoadBalancer();
    }

    @Test
    public void testWebSphereLoadBalancer() throws IOException {
        super.testWebSphereLoadBalancer();
    }

    @Test
    @Ignore
    public void testTomcatLoadBalancerSecurity() throws IOException {
        super.testTomcatLoadBalancerSecurity();
    }

    @Test
    public void testJBoss7LoadBalancer() throws IOException {
        super.testJBoss7LoadBalancer();
    }

    @Test
    public void testJBoss8LoadBalancer() throws IOException {
        this.shiroSecurityConfiguration = new WithoutLoginShiroSecurityConfigurationSessionIDNameChange();
        super.testJBoss8LoadBalancer();
    }
}
