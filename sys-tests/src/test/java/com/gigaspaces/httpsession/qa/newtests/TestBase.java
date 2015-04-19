package com.gigaspaces.httpsession.qa.newtests;

import com.eviware.soapui.tools.SoapUITestCaseRunner;
import com.gigaspaces.httpsession.qa.DataUnit;
import com.gigaspaces.httpsession.qa.oldtests.SystemTestCase;
import com.gigaspaces.httpsession.qa.utils.*;
import com.gigaspaces.httpsession.serialize.CompressUtils;
import com.gigaspaces.httpsession.serialize.KryoSerializerImpl;
import com.gigaspaces.httpsession.serialize.NonCompressCompressor;
import com.gigaspaces.httpsession.serialize.SerializeUtils;
import junit.framework.Assert;
import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openspaces.admin.Admin;

import java.io.IOException;
import java.util.*;

/**
 * @author Yohana Khoury
 * @since 10.1
 */
public abstract class TestBase {
    private static final String SESSION_SPACE = "sessionSpace";
    protected StoreModeBase storeModeBase;
    protected String webAppAddress;
    protected RemoteSpaceController remoteSpaceController = new RemoteSpaceController();
    protected ServerController serverController;
    protected ShiroSecurityConfigurationBase shiroSecurityConfiguration;

    protected void assertSpaceMode(int expectedSessions, HashMap<String, Map<String, DataUnit>> expected, String type, int expectedVersion) {
        storeModeBase.assertSpaceMode(remoteSpaceController.getSpace(), expectedSessions, expected, type, expectedVersion);
    }

    protected void startWebServer(ServerController serverController) {
        this.serverController = serverController;
        this.serverController.reset();
        Map<String, String> properties = shiroSecurityConfiguration.getConfiguration();
        properties.put("main/storeMode", storeModeBase.getStoreModeClassName());
        this.serverController.startAll(this.shiroSecurityConfiguration.getFile(false), properties);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void stopWebServer() throws IOException {
        serverController.stopAll();
    }

    @BeforeClass
    public static void init() {

        CompressUtils.register(new NonCompressCompressor());

        SerializeUtils.register(new KryoSerializerImpl());
    }

    @Before
    public void before() {
        remoteSpaceController.start();
    }

    @After
    public void after() throws IOException, InterruptedException {
        remoteSpaceController.stop();
        stopWebServer();
        Thread.sleep(5000);
    }


    protected String getWebAppAddress() {
        return webAppAddress;
    }

    public void soapUITest() throws Exception{
        SoapUITestCaseRunner runner = new SoapUITestCaseRunner();
        runner.setProjectFile( "../soupui/REST-test-project.xml" );
        runner.run();
    }

    public void test() throws IOException {
        int usersCount = 2;
        int iterations = 4;

        String requestsAddress = getWebAppAddress();

        HashMap<String, Map<String, DataUnit>> expected = new HashMap<String, Map<String, DataUnit>>();
        ArrayList<HTTPUtils.HTTPSession> sessions = new ArrayList<HTTPUtils.HTTPSession>();
        for (int i = 0; i < usersCount; i++) {
            HTTPUtils.HTTPSession user = new HTTPUtils.HTTPSession();
            sessions.add(user);
        }
        //Write
        for (int j = 0; j < iterations; j++) {

            for (int i = 0; i < sessions.size(); i++) {
                HTTPUtils.HTTPSession session = sessions.get(i);
                Map<String, DataUnit> data = DataGenerator.generateData();
                HTTPUtils.HTTPGetRequest getRequest = new HTTPUtils.HTTPGetRequest(requestsAddress);
                if (/*j == 0 &&*/ shiroSecurityConfiguration instanceof WithLoginShiroSecurityConfiguration) {
                    getRequest.auth("user" + (i + 1), "user" + (i + 1));
                }

                HTTPUtils.HTTPResponse response = session.send(getRequest);
                System.out.println(response.getCookies() + "," + response.getStatusCode());

                /*HTTPUtils.HTTPGetRequest getRequest2 = new HTTPUtils.HTTPGetRequest(requestsAddress);
                HTTPUtils.HTTPResponse response2 = session.send(getRequest2);
                System.out.println(response2);*/

                updateSession(session, data, requestsAddress);

                String sessionid;
                if (shiroSecurityConfiguration instanceof WithLoginShiroSecurityConfiguration) {
                    sessionid = "user" + (i + 1);
                } else {
                    sessionid = session.getCookie();
                }

                if (!expected.containsKey(sessionid)) {
                    expected.put(sessionid, data);
                } else {
                    expected.get(sessionid).putAll(data);
                }

            }
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertSpaceMode(usersCount, expected, SystemTestCase.DEFAULT_SESSION_BASE_NAME, 0);


        System.out.println("UPDATE STARTS");
        //Update
        for (int i = 0; i < sessions.size(); i++) {
            HTTPUtils.HTTPSession session = sessions.get(i);
            String sessionid;
            if (shiroSecurityConfiguration instanceof WithLoginShiroSecurityConfiguration) {
                sessionid = "user" + (i + 1);
            } else {
                sessionid = session.getCookie();
            }
            Map<String, DataUnit> expectedBySessionID = expected.get(sessionid);
            DataGenerator.manipulateData(expectedBySessionID);

            updateSession(session, expectedBySessionID, requestsAddress);

        }
        System.out.println("UPDATE ENDS");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertSpaceMode(usersCount, expected, SystemTestCase.DEFAULT_SESSION_BASE_NAME, 1);

        //Delete
        for (int i = 0; i < sessions.size(); i++) {
            HTTPUtils.HTTPSession session = sessions.get(i);
            String sessionid;
            if (shiroSecurityConfiguration instanceof WithLoginShiroSecurityConfiguration) {
                sessionid = "user" + (i + 1);
            } else {
                sessionid = session.getCookie();
            }
            Map<String, DataUnit> expectedBySessionID = expected.get(sessionid);

            for (DataUnit dataUnit : expectedBySessionID.values()) {
                dataUnit.setDataaction("R");
            }

            updateSession(session, expectedBySessionID, requestsAddress);
            for (DataUnit dataUnit : expectedBySessionID.values()) {
                dataUnit.setDatavalue(null);
            }
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertSpaceMode(usersCount, expected, SystemTestCase.DEFAULT_SESSION_BASE_NAME, 2);
    }

    public void testFailover() throws IOException {
        HashMap<String, DataUnit> expected = new HashMap<String, DataUnit>();
        int usersCount = 2;

        String requestsAddress = getWebAppAddress();

        ArrayList<HTTPUtils.HTTPSession> sessions = new ArrayList<HTTPUtils.HTTPSession>();
        for (int i = 0; i < usersCount; i++) {
            HTTPUtils.HTTPSession user1 = new HTTPUtils.HTTPSession();
            sessions.add(user1);
        }
        for (int j = 0; j < 3; j++) {
            Map<String, DataUnit> data = DataGenerator.generateData();
            expected.putAll(data);

            for (HTTPUtils.HTTPSession session : sessions) {

                HTTPUtils.HTTPGetRequest getRequest = new HTTPUtils.HTTPGetRequest(requestsAddress);


                HTTPUtils.HTTPResponse response = session.send(getRequest);
                response.getCookies();

                updateSession(session, data, requestsAddress);

            }
        }


        Admin admin = remoteSpaceController.getAdmin();

        AdminUtils.restartPrimaries(admin, SESSION_SPACE);

        AdminUtils.waitForPrimaries(admin, SESSION_SPACE, 2);
        AdminUtils.waitForBackups(admin, SESSION_SPACE, 2);

        // assertSpaceMode(usersCount, expected, SystemTestCase.DEFAULT_SESSION_BASE_NAME);

    }

    public void updateSession(HTTPUtils.HTTPSession session, Map<String, DataUnit> expected, String requestsAddress) throws IOException {
        for (String key : expected.keySet()) {
            String value;

            if (expected.get(key).isSerialized()) { //should serialize
                byte[] buff = SerializeUtils.serialize(expected.get(key).getDatavalue());

                byte[] buff1 = Base64.encodeBase64(buff);

                value = new String(buff1);
            } else {
                value = expected.get(key).getDatavalue().toString();
            }
            HTTPUtils.HTTPPostRequest postRequest = new HTTPUtils.HTTPPostRequest(requestsAddress + "/UpdateSessionServlet");
            postRequest.withParameter("dataname", key);
            postRequest.withParameter("datavalue", value);
            System.out.println("Sending ["+key+"="+value+"]");
            postRequest.withParameter("dataaction", expected.get(key).getDataaction());
            HTTPUtils.HTTPResponse response = session.send(postRequest);
            System.out.println(response.getStatusCode());
            if (response.getStatusCode() != 200) {
                System.out.println(response.getBody());
                Assert.assertEquals("Unexpected status code. Response body: ["+response.getBody()+"]", 200, response.getStatusCode());
            }
        }
    }


}
