package com.gigaspaces.httpsession.qa.newtests.compression;

import com.gigaspaces.httpsession.qa.newtests.bases.AbstractLoadBalancerTest;
import com.gigaspaces.httpsession.qa.newtests.bases.FullStoreModeBase;
import com.gigaspaces.httpsession.qa.newtests.bases.WithLoginShiroSecurityConfigurationWithCompression;
import com.gigaspaces.httpsession.serialize.CompressUtils;
import com.gigaspaces.httpsession.serialize.CompressorImpl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Yohana Khoury
 * @since 10.1
 */
public class FullModeWithLoginTest extends AbstractLoadBalancerTest {


    @Override
    @Before
    public void before() {
        super.before();
        CompressUtils.register(new CompressorImpl());
        this.storeModeBase = new FullStoreModeBase();
        this.shiroSecurityConfiguration = new WithLoginShiroSecurityConfigurationWithCompression();
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
    public void testTomcatLoadBalancerSecurityHTTPS() throws IOException {
        super.testTomcatLoadBalancerSecurityHTTPS();
    }
}
