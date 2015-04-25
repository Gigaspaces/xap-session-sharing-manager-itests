package com.gigaspaces.httpsession.qa.newtests.bases;

import com.gigaspaces.httpsession.qa.utils.*;

import java.io.IOException;

/**
 * @author Yohana Khoury
 * @since 10.1
 */
public class AbstractTestBase extends TestBase {
    public void testJetty() throws IOException {
        startWebServer(new JettyController(7777));
        test();
    }

    public void testTomcat() throws IOException {
        startWebServer(new TomcatController(7777));

        test();
    }

    public void testWebSphere() throws IOException {
        startWebServer(new WebsphereController(7777));

        test();
    }


    /*
    Here starts with HTTPS
     */

    public void testJettyHTTPS() throws IOException {
        startWebServer(new JettyHTTPSController(7777));
        test();
    }

    public void testTomcatHTTPS() throws IOException {
        startWebServer(new TomcatHTTPSController(7777));

        test();
    }

    public void testWebSphereHTTPS() throws IOException {
        startWebServer(new WebsphereHTTPSController(7777));

        test();
    }

   /*
    Here starts tests with secure webserver
     */

    public void testTomcatSecurity() throws IOException {

        startWebServer(new TomcatController(7777,true));

        test();
    }

    public void testTomcatSecurityHTTPS() throws IOException {

        startWebServer(new TomcatHTTPSController(7777,true));
        test();
    }
}