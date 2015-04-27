package com.gigaspaces.httpsession.qa.newtests.loadbalancertests.nonstickysession;

import com.gigaspaces.httpsession.qa.newtests.bases.AbstractLoadBalancerTest;
import com.gigaspaces.httpsession.qa.newtests.bases.FullStoreModeBase;
import com.gigaspaces.httpsession.qa.newtests.bases.WithLoginSpringSecurityConfiguration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Yohana Khoury
 * @since 10.1
 */
@Ignore
public class FullModeWithoutLoginSpringSecurityLoginTest extends AbstractLoadBalancerTest {

    @Override
    @Before
    public void before() {
        super.before();
        this.storeModeBase = new FullStoreModeBase();
        this.shiroSecurityConfiguration = new WithLoginSpringSecurityConfiguration();
        this.webAppAddress = "http://localhost:7777/demo-app";
    }

    @Test
    public void testJettyLoadBalancer() throws IOException {
        super.testJettyLoadBalancerNonStickySessionSpringSecurity();
    }

    @Test
    public void testTomcatLoadBalancer() throws IOException {
        super.testTomcatLoadBalancerNonStickySessionSpringSecurity();
    }

//    @Test
//    public void testWebSphereLoadBalancer() throws IOException {
//        super.testWebSphereLoadBalancer();
//    }
}
