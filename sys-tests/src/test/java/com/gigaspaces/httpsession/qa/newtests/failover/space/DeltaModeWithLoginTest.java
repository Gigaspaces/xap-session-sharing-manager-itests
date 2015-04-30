package com.gigaspaces.httpsession.qa.newtests.failover.space;

import com.gigaspaces.httpsession.qa.newtests.bases.AbstractLoadBalancerTest;
import com.gigaspaces.httpsession.qa.newtests.bases.DeltaStoreModeBase;
import com.gigaspaces.httpsession.qa.newtests.bases.WithLoginShiroSecurityConfiguration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Yohana Khoury
 * @since 10.2
 */
@Ignore
public class DeltaModeWithLoginTest extends AbstractLoadBalancerTest {
    @Override
    @Before
    public void before() {
        super.before();
        this.storeModeBase = new DeltaStoreModeBase();
        this.shiroSecurityConfiguration = new WithLoginShiroSecurityConfiguration();
        this.webAppAddress = "http://localhost:7777/demo-app";
    }

    /*
    Here starts space failover - primaries only
     */

    @Test
    public void testJettyLoadBalancerSpaceFailover() throws IOException {
        super.testJettyLoadBalancerSpaceFailover();
    }

    @Test
    public void testTomcatLoadBalancerSpaceFailover() throws IOException {
        super.testTomcatLoadBalancerSpaceFailover();
    }

    @Test
    public void testWebSphereLoadBalancerSpaceFailover() throws IOException {
        super.testWebSphereLoadBalancerSpaceFailover();
    }

    @Test
    public void testJBoss7LoadBalancerSpaceFailover() throws IOException {
        super.testJBoss7LoadBalancerSpaceFailover();
    }

    @Test
    public void testJBoss8LoadBalancerSpaceFailover() throws IOException {
        super.testJBoss8LoadBalancerSpaceFailover();
    }

    /*
    Here starts space failover - whole cluster
     */

    @Test
    public void testJettyLoadBalancerFullSpaceFailover() throws IOException {
        super.testJettyLoadBalancerFullSpaceFailover();
    }

    @Test
    public void testTomcatLoadBalancerFullSpaceFailover() throws IOException {
        super.testTomcatLoadBalancerFullSpaceFailover();
    }

    @Test
    public void testWebSphereLoadBalancerFullSpaceFailover() throws IOException {
        super.testWebSphereLoadBalancerFullSpaceFailover();
    }

    @Test
    public void testJBoss7LoadBalancerFullSpaceFailover() throws IOException {
        super.testJBoss7LoadBalancerFullSpaceFailover();
    }

    @Test
    public void testJBoss8LoadBalancerFullSpaceFailover() throws IOException {
        super.testJBoss8LoadBalancerFullSpaceFailover();
    }
}
