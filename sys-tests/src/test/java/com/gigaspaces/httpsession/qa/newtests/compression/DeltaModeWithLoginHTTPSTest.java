package com.gigaspaces.httpsession.qa.newtests.compression;

import com.gigaspaces.httpsession.qa.newtests.bases.AbstractLoadBalancerTest;
import com.gigaspaces.httpsession.qa.newtests.bases.DeltaStoreModeBase;
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
@Ignore
public class DeltaModeWithLoginHTTPSTest extends AbstractLoadBalancerTest {


    @Override
    @Before
    public void before() {
        super.before();
        CompressUtils.register(new CompressorImpl());
        this.storeModeBase = new DeltaStoreModeBase();
        this.shiroSecurityConfiguration = new WithLoginShiroSecurityConfigurationWithCompression();
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
    public void testTomcatLoadBalancerSecurity() throws IOException {
        super.testTomcatLoadBalancerSecurity();
    }
}
