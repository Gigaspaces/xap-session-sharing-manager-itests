package com.gigaspaces.httpsession.qa.newtests.compression;

import com.gigaspaces.httpsession.qa.newtests.bases.AbstractLoadBalancerTest;
import com.gigaspaces.httpsession.qa.newtests.bases.DeltaStoreModeBase;
import com.gigaspaces.httpsession.qa.newtests.bases.WithoutLoginShiroSecurityConfigurationWithCompression;
import com.gigaspaces.httpsession.serialize.CompressUtils;
import com.gigaspaces.httpsession.serialize.CompressorImpl;
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
        CompressUtils.register(new CompressorImpl());
        this.storeModeBase = new DeltaStoreModeBase();
        this.shiroSecurityConfiguration = new WithoutLoginShiroSecurityConfigurationWithCompression();
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
}
