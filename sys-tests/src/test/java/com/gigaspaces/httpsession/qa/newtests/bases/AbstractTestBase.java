package com.gigaspaces.httpsession.qa.newtests.bases;

import com.gigaspaces.httpsession.qa.utils.*;

import java.io.IOException;

/**
 * @author Yohana Khoury
 * @since 10.1
 */
public class AbstractTestBase extends TestBase {
    public void testJetty() throws IOException {
        startWebServer(new JettyController(7778));
        test();
    }

    public void testTomcat() throws IOException {
        startWebServer(new TomcatController(7778));

        test();
    }

    public void testWebSphere() throws IOException {
        startWebServer(new WebsphereController(7778));

        test();
    }


    /*
    Here starts with HTTPS
     */

    public void testJettyHTTPS() throws IOException {
        startWebServer(new JettyHTTPSController(7778));
        test();
    }

    public void testTomcatHTTPS() throws IOException {
        startWebServer(new TomcatHTTPSController(7778));

        test();
    }

    public void testWebSphereHTTPS() throws IOException {
        startWebServer(new WebsphereHTTPSController(7778));

        test();
    }

   /*
    Here starts tests with secure webserver
     */

    public void testTomcatSecurity() throws IOException {

        startWebServer(new TomcatController(7778,true));

        test();
    }

    public void testTomcatSecurityHTTPS() throws IOException {

        startWebServer(new TomcatHTTPSController(7778,true));
        test();
    }

    /*
    Here starts tests of multiple sessions for the same user
     */

    public void testTomcatMultiSessionsForSameUser() throws Exception {
        startWebServer(new TomcatController(7778));
        testMultiSessionsForSameUser();
    }

    public void testJettyMultiSessionsForSameUser() throws Exception {
        startWebServer(new JettyController(7778));
        testMultiSessionsForSameUser();
    }

    public void testWebsphereMultiSessionsForSameUser() throws Exception {
        startWebServer(new WebsphereController(7778));
        testMultiSessionsForSameUser();
    }

    public void testJboss7MultiSessionsForSameUser() throws Exception {
        startWebServer(new JbossController(7778));
        testMultiSessionsForSameUser();
    }
}