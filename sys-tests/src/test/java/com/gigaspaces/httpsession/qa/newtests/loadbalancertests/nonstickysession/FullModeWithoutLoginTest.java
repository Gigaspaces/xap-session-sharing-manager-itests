package com.gigaspaces.httpsession.qa.newtests.loadbalancertests.nonstickysession;

import com.gigaspaces.httpsession.qa.newtests.bases.AbstractLoadBalancerTest;
import com.gigaspaces.httpsession.qa.newtests.bases.FullStoreModeBase;
import com.gigaspaces.httpsession.qa.newtests.bases.WithoutLoginShiroSecurityConfiguration;
import com.gigaspaces.httpsession.qa.newtests.bases.WithoutLoginShiroSecurityConfigurationSessionIDNameChange;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Yohana Khoury
 * @since 10.1
 */
public class FullModeWithoutLoginTest extends AbstractLoadBalancerTest {

    @Override
    @Before
    public void before() {
        super.before();
        this.storeModeBase = new FullStoreModeBase();
        this.shiroSecurityConfiguration = new WithoutLoginShiroSecurityConfiguration();
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
