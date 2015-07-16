package com.gigaspaces.httpsession.qa.newtests;

import com.gigaspaces.httpsession.qa.newtests.bases.AbstractTestBase;
import com.gigaspaces.httpsession.qa.newtests.bases.DeltaStoreModeBase;
import com.gigaspaces.httpsession.qa.newtests.bases.WithoutLoginShiroSecurityConfiguration;
import com.gigaspaces.httpsession.qa.utils.HTTPUtils;
import com.gigaspaces.httpsession.qa.utils.JettyControllerWithoutLicense;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Yohana Khoury
 * @since 10.2
 */
public class LicenseTest extends AbstractTestBase {
    @Override
    @Before
    public void before() {
        super.before();
        this.storeModeBase = new DeltaStoreModeBase();
        this.shiroSecurityConfiguration = new WithoutLoginShiroSecurityConfiguration();
        this.webAppAddress = "http://localhost:7778/demo-app";
    }

    @Test
    public void testJettyNegative() throws IOException {
        startWebServer(new JettyControllerWithoutLicense(7778));
        HTTPUtils.HTTPSession session = new HTTPUtils.HTTPSession();

        HTTPUtils.HTTPGetRequest getRequest = new HTTPUtils.HTTPGetRequest(this.webAppAddress);
        HTTPUtils.HTTPResponse response = session.send(getRequest);
        Assert.assertEquals("Expecting a 503 error", 503, response.getStatusCode());
    }
}
