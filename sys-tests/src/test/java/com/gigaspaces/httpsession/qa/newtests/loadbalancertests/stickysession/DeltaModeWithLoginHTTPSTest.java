package com.gigaspaces.httpsession.qa.newtests.loadbalancertests.stickysession;

import com.gigaspaces.httpsession.qa.newtests.bases.AbstractLoadBalancerTest;
import com.gigaspaces.httpsession.qa.newtests.bases.DeltaStoreModeBase;
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
public class DeltaModeWithLoginHTTPSTest extends AbstractLoadBalancerTest {

    @Override
    @Before
    public void before() {
        super.before();
        this.storeModeBase = new DeltaStoreModeBase();
        this.shiroSecurityConfiguration = new WithLoginShiroSecurityConfiguration();
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
}
