package com.gigaspaces.httpsession.qa.newtests.loadbalancertests.nonstickysession;

import com.gigaspaces.httpsession.qa.newtests.bases.AbstractLoadBalancerTest;
import com.gigaspaces.httpsession.qa.newtests.bases.DeltaStoreModeBase;
import com.gigaspaces.httpsession.qa.newtests.bases.WithoutLoginShiroSecurityConfiguration;
import com.gigaspaces.httpsession.qa.utils.ExpectedTestResult;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Kobi Kisos
 * @since 10.2
 */
@Ignore
public class DeltaModeWithoutLoginSecuredSpaceNegativeTest extends AbstractLoadBalancerTest {

    @Override
    @Before
    public void before() {
        super.isSecuredSpace = true;
        super.expectedTestResult = new ExpectedTestResult(500, "");
        super.before();
        this.storeModeBase = new DeltaStoreModeBase();
        this.shiroSecurityConfiguration = new WithoutLoginShiroSecurityConfiguration();
        this.webAppAddress = "http://localhost:7777/demo-app";
    }

    @Test
    public void testTomcatLoadBalancer() throws IOException {
        super.testTomcatLoadBalancer();
    }
}
