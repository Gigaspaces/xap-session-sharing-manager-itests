package com.gigaspaces.httpsession.qa.newtests.compression;

import com.gigaspaces.httpsession.qa.newtests.bases.AbstractLoadBalancerTest;
import com.gigaspaces.httpsession.qa.newtests.bases.FullStoreModeBase;
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
public class FullModeWithoutLoginHTTPSTest extends AbstractLoadBalancerTest {


    @Override
    @Before
    public void before() {
        super.before();
        CompressUtils.register(new CompressorImpl());
        this.storeModeBase = new FullStoreModeBase();
        this.shiroSecurityConfiguration = new WithoutLoginShiroSecurityConfigurationWithCompression();
        this.webAppAddress = "http://localhost:7778/demo-app";
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
