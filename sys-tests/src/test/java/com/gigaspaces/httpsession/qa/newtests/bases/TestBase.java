package com.gigaspaces.httpsession.qa.newtests.bases;
import com.eviware.soapui.tools.SoapUITestCaseRunner;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigaspaces.httpsession.qa.DataUnit;
import com.gigaspaces.httpsession.qa.User;
import com.gigaspaces.httpsession.qa.oldtests.SystemTestCase;
import com.gigaspaces.httpsession.qa.utils.*;
import com.gigaspaces.httpsession.serialize.CompressUtils;
import com.gigaspaces.httpsession.serialize.KryoSerializerImpl;
import com.gigaspaces.httpsession.serialize.NonCompressCompressor;
import com.gigaspaces.httpsession.serialize.SerializeUtils;
import junit.framework.Assert;
import org.apache.commons.codec.binary.Base64;
import org.hornetq.utils.json.JSONException;
import org.hornetq.utils.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openspaces.admin.Admin;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
    private HashMap<String, Map<String, DataUnit>> expected;
    private ArrayList<HTTPUtils.HTTPSession> sessions;
    private int usersCount = 2;
    private static ObjectMapper mapper = new ObjectMapper();

    private boolean isLoggedIn = false;

    protected void assertSpaceMode(int expectedSessions, HashMap<String, Map<String, DataUnit>> expected, String type) {
        storeModeBase.assertSpaceMode(remoteSpaceController.getSpace(), expectedSessions, expected, type);
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
        serverController.stopAll(true, false);
    }

    @BeforeClass
    public static void init() {

        CompressUtils.register(new NonCompressCompressor());

        SerializeUtils.register(new KryoSerializerImpl());
    }

    @Before
    public void before() {
        remoteSpaceController.start();
        try {
            remoteSpaceController.deploy("");
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("TBD");
        }
        expected = new HashMap<String, Map<String, DataUnit>>();
        sessions = new ArrayList<HTTPUtils.HTTPSession>();
        for (int i = 0; i < usersCount; i++) {
            HTTPUtils.HTTPSession user = new HTTPUtils.HTTPSession();
            sessions.add(user);
        }
        isLoggedIn = false;
    }

    @After
    public void after() throws IOException, InterruptedException {
        remoteSpaceController.undeploy();
        remoteSpaceController.stop();
        stopWebServer();
        Thread.sleep(5000);
    }


    protected String getWebAppAddress() {
        return webAppAddress;
    }

    public void test() throws IOException {
        int iterations = 4;

        if (!isLoggedIn) {
            if (shiroSecurityConfiguration instanceof WithLoginShiroSecurityConfiguration) {
                for (int i = 0; i < sessions.size(); i++) {
                    HTTPUtils.HTTPPostRequest postRequest = new HTTPUtils.HTTPPostRequest(getWebAppAddress() + "/login.jsp");
                    postRequest.withParameter("username", "user" + (i + 1))
                            .withParameter("password", "user" + (i + 1));
                    HTTPUtils.HTTPResponse response = sessions.get(i).send(postRequest);
                    Assert.assertEquals("Unexpected status code", 302, response.getStatusCode());
                }
                isLoggedIn = true;
            } else if (shiroSecurityConfiguration instanceof WithLoginSpringSecurityConfiguration) {
                for (int i = 0; i < sessions.size(); i++) {
                    HTTPUtils.HTTPPostRequest postRequest = new HTTPUtils.HTTPPostRequest(getWebAppAddress() + "/j_spring_security_check");
                    postRequest.withParameter("j_username", "user" + (i + 1))
                            .withParameter("j_password", "user" + (i + 1));
                    HTTPUtils.HTTPResponse response = sessions.get(i).send(postRequest);
                    Assert.assertEquals("Unexpected status code", 302, response.getStatusCode());
                }
                isLoggedIn = true;
            }
        }

        String requestsAddress = getWebAppAddress();
        //Write
        for (int j = 0; j < iterations; j++) {

            for (int i = 0; i < sessions.size(); i++) {
                HTTPUtils.HTTPSession session = sessions.get(i);
                Map<String, DataUnit> data = DataGenerator.generateData();
                HTTPUtils.HTTPGetRequest getRequest = new HTTPUtils.HTTPGetRequest(requestsAddress);
                if (/*j == 0 &&*/ shiroSecurityConfiguration instanceof WithLoginShiroSecurityConfiguration) {
                    //getRequest.auth("user" + (i + 1), "user" + (i + 1));
                }

                HTTPUtils.HTTPResponse response = session.send(getRequest);
                System.out.println(response.getCookies() + "," + response.getStatusCode());

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
        /*try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        assertSpaceMode(usersCount, expected, SystemTestCase.DEFAULT_SESSION_BASE_NAME);


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
       /* try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
          assertSpaceMode(usersCount, expected, SystemTestCase.DEFAULT_SESSION_BASE_NAME);

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
                if (dataUnit.getDatavalue() == null) {
                    continue;
                }
                dataUnit.setDataaction("R");
                dataUnit.setExpectedVersion(dataUnit.getExpectedVersion() + 1);
            }

            updateSession(session, expectedBySessionID, requestsAddress);
        }
       /* try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        assertSpaceMode(usersCount, expected, SystemTestCase.DEFAULT_SESSION_BASE_NAME);
    }

    public void testFullSpaceFailover() throws IOException {
        test();

        assertSpaceMode(sessions.size(), expected, SystemTestCase.DEFAULT_SESSION_BASE_NAME);

        Admin admin = remoteSpaceController.getAdmin();

        ProcessingUnit spacePU = admin.getProcessingUnits().getProcessingUnit(SESSION_SPACE);
        for (ProcessingUnitInstance instance : spacePU.getInstances()) {
            instance.restart();
        }

        Assert.assertTrue("Failed to find [" + spacePU.getPlannedNumberOfInstances() + "] instances after restarting space pu", spacePU.waitFor(spacePU.getPlannedNumberOfInstances(), 10, TimeUnit.SECONDS));


        test();

        assertSpaceMode(sessions.size(), expected, SystemTestCase.DEFAULT_SESSION_BASE_NAME);

    }

    public void testSpaceFailover() throws IOException {
        test();

        assertSpaceMode(sessions.size(), expected, SystemTestCase.DEFAULT_SESSION_BASE_NAME);

        Admin admin = remoteSpaceController.getAdmin();

        ProcessingUnit spacePU = admin.getProcessingUnits().getProcessingUnit(SESSION_SPACE);
        AdminUtils.restartPrimaries(admin, SESSION_SPACE);

        AdminUtils.waitForPrimaries(admin, SESSION_SPACE, 2);
        AdminUtils.waitForBackups(admin, SESSION_SPACE, 2);

        Assert.assertTrue("Failed to find [" + spacePU.getPlannedNumberOfInstances() + "] instances after restarting space pu", spacePU.waitFor(spacePU.getPlannedNumberOfInstances(), 10, TimeUnit.SECONDS));


        test();

        assertSpaceMode(sessions.size(), expected, SystemTestCase.DEFAULT_SESSION_BASE_NAME);

    }

    public void testOneWebServerOutOfManyFailover() throws IOException, JSONException {
        HTTPUtils.HTTPSession session = new HTTPUtils.HTTPSession();

        Map<String, DataUnit> data = DataGenerator.generateData(1);
        DataUnit attribute = data.values().iterator().next();

        HTTPUtils.HTTPResponse firstResponse = sendRequest(session, attribute, getWebAppAddress());

        JSONObject firstResponseJSON = new JSONObject(firstResponse.getBody());

        int firstRequestPort = firstResponseJSON.getJSONObject("more").getInt("port");

        this.serverController.stopOneWebServerWithPort(firstRequestPort);

        HTTPUtils.HTTPGetRequest getRequest = new HTTPUtils.HTTPGetRequest(getWebAppAddress()+"/json/view");
        HTTPUtils.HTTPResponse secondResponse = session.send(getRequest);

        JSONObject secondResponseJSON = new JSONObject(secondResponse.getBody());
        int secondRequestPort = secondResponseJSON.getJSONObject("more").getInt("port");
        System.out.println("Ports: "+firstRequestPort+","+secondRequestPort);
        Assert.assertTrue("First port [" +firstRequestPort+"] and second port ["+secondRequestPort+"] should be different", firstRequestPort != secondRequestPort);

        Assert.assertEquals("Attributes should be the same", mapper.readValue(firstResponseJSON.getJSONObject("attributes").toString(), HashMap.class), mapper.readValue(secondResponseJSON.getJSONObject("attributes").toString(), HashMap.class));

        Assert.assertEquals("session id should be the same", firstResponseJSON.getJSONObject("more").getString("sessionid"), secondResponseJSON.getJSONObject("more").getString("sessionid"));
    }

    public void testWebServersFailover() throws IOException, JSONException {
        HTTPUtils.HTTPSession session = new HTTPUtils.HTTPSession();

        Map<String, DataUnit> data = DataGenerator.generateData(1);
        DataUnit attribute = data.values().iterator().next();

        HTTPUtils.HTTPResponse firstResponse = sendRequest(session, attribute, getWebAppAddress());
        JSONObject firstResponseJSON = new JSONObject(firstResponse.getBody());

        User actualAttribute = mapper.readValue(firstResponseJSON.getJSONObject("attributes").get(attribute.getDataname()).toString(), User.class);

        Assert.assertEquals("Unexpected attribute", attribute.getDatavalue(), actualAttribute);

        stopWebServer();
        startWebServer(this.serverController);

        Set<Integer> ports = new HashSet<Integer>();
        for (int i=0 ; i<4 ; i++) {

            HTTPUtils.HTTPGetRequest getRequest = new HTTPUtils.HTTPGetRequest(getWebAppAddress() + "/json/view");
            HTTPUtils.HTTPResponse secondResponse = session.send(getRequest);

            JSONObject secondResponseJSON = new JSONObject(secondResponse.getBody());
            int secondRequestPort = secondResponseJSON.getJSONObject("more").getInt("port");
            ports.add(secondRequestPort);
            Assert.assertEquals("Attributes should be the same", mapper.readValue(firstResponseJSON.getJSONObject("attributes").toString(), HashMap.class), mapper.readValue(secondResponseJSON.getJSONObject("attributes").toString(), HashMap.class));
            Assert.assertEquals("session id should be the same", firstResponseJSON.getJSONObject("more").getString("sessionid"), secondResponseJSON.getJSONObject("more").getString("sessionid"));
        }

        Assert.assertEquals("Unexpected number of total web servers (ports)", 2, ports.size());
    }

    public HTTPUtils.HTTPResponse sendRequest(HTTPUtils.HTTPSession session, DataUnit dataUnit, String requestsAddress) throws IOException {
        String key = dataUnit.getDataname();
        String value;
        if (dataUnit.isSerialized()) { //should serialize
            byte[] buff = SerializeUtils.serialize(dataUnit.getDatavalue());

            byte[] buff1 = Base64.encodeBase64(buff);

            value = new String(buff1);
        } else {
            value = dataUnit.getDatavalue().toString();
        }

        HTTPUtils.HTTPPostRequest postRequest = new HTTPUtils.HTTPPostRequest(requestsAddress + "/json/update");
        postRequest.withParameter("dataname", key);
        postRequest.withParameter("datavalue", value);
        postRequest.withParameter("dataaction", dataUnit.getDataaction());
        HTTPUtils.HTTPResponse response = session.send(postRequest);
        System.out.println(response.getStatusCode());

        return response;
    }

    public void updateSession(HTTPUtils.HTTPSession session, Map<String, DataUnit> expected, String requestsAddress) throws IOException {
        for (String key : expected.keySet()) {
            DataUnit dataUnit = expected.get(key);
            if (dataUnit.getDatavalue() == null) {
                //Means that this attribute was deleted!
                continue;
            }

            HTTPUtils.HTTPResponse response = sendRequest(session, expected.get(key), requestsAddress);

            if (response.getStatusCode() != 200) {
                System.out.println(response.getBody());
                Assert.assertEquals("Unexpected status code. Response body: [" + response.getBody() + "]", 200, response.getStatusCode());
            }

            if (dataUnit.getDataaction().equals("R")) {
                try {
                    Assert.assertFalse("Session should not have the attribute with key [" + key + "]", new JSONObject(response.getBody()).getJSONObject("attributes").has(key));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                if (dataUnit.getDatavalue() instanceof User) {
                    try {
                        Assert.assertEquals("Failed to find attribute [" + key + "] in web server side", dataUnit.getDatavalue(), mapper.readValue(new JSONObject(response.getBody()).getJSONObject("attributes").get(key).toString(), User.class));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Assert.fail("Failed to parse JSON in updateSession. " + e.getMessage());
                    }
                } else {
                    try {
                        Assert.assertEquals("Failed to find attribute [" + key + "] in web server side", dataUnit.getDatavalue(), new JSONObject(response.getBody()).getJSONObject("attributes").get(key).toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Assert.fail("Failed to parse JSON in updateSession. " + e.getMessage());
                    }
                }
            }

            if (expected.get(key).getDataaction().equals("R")) {
                expected.get(key).setDatavalue(null);
            }

        }
    }

    public void soapUITest() throws Exception{
        SoapUITestCaseRunner runner = new SoapUITestCaseRunner();
        runner.setProjectFile( "../soupui/REST-test-project.xml" );
        runner.run();
    }

}
