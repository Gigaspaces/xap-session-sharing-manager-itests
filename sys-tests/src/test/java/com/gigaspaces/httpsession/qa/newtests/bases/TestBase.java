package com.gigaspaces.httpsession.qa.newtests.bases;

import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Yohana Khoury
 * @since 10.1
 */
public abstract class TestBase {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(TestBase.class);
    private static final String SESSION_SPACE = "sessionSpace";
    protected StoreModeBase storeModeBase;
    protected String webAppAddress;
    protected RemoteSpaceController remoteSpaceController = new RemoteSpaceController();
    protected ServerController serverController;
    protected ShiroSecurityConfigurationBase shiroSecurityConfiguration;
    private HashMap<String, Map<String, DataUnit>> expected;
    private ArrayList<HTTPUtils.HTTPSession> sessions;
    private int usersCount = 2;
    public static ObjectMapper mapper = new ObjectMapper();

    protected boolean isLoggedIn = false;

    protected boolean isSecuredSpace = false;

    protected ExpectedTestResult expectedTestResult = new ExpectedTestResult(200, null);

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
        if (serverController != null)
            serverController.stopAll(true, false);
    }

    @BeforeClass
    public static void init() {

        CompressUtils.register(new NonCompressCompressor());

        KryoSerializerImpl kryo = new KryoSerializerImpl();
        HashMap<String, Serializer> map = new HashMap<String, Serializer>();
        JavaSerializer javaserializer = new JavaSerializer();
        map.put("javax.security.auth.Subject", javaserializer);
        map.put("org.springframework.security.core.userdetails.User", javaserializer);
        map.put("org.springframework.security.core.context.SecurityContextImpl", javaserializer);
        kryo.setClassSerializers(map);
        SerializeUtils.register(kryo);
    }

    @Before
    public void before() {
        LOGGER.info("Starting...");
        remoteSpaceController.start(isSecuredSpace);
        try {
            LOGGER.info("Deploying space");
            remoteSpaceController.deploy("", isSecuredSpace);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Failed to deploy space");
        }
        expected = new HashMap<String, Map<String, DataUnit>>();
        createSessions();
        isLoggedIn = false;
    }

    public void createSessions() {
        sessions = new ArrayList<HTTPUtils.HTTPSession>();
        for (int i = 0; i < usersCount; i++) {
            HTTPUtils.HTTPSession user = new HTTPUtils.HTTPSession();
            sessions.add(user);
        }
    }

    @After
    public void teardownWebserver() throws IOException, InterruptedException {
        LOGGER.info("Tearing down...");

        if (getOutputDir() != null && serverController != null) {
            try {
                serverController.dumpLogsToDir(getOutputDir());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                LOGGER.error("Failed to dump logs", throwable);
            }
        }

        LOGGER.info("Stopping web server");
        stopWebServer();
    }

    @After
    public void teardownSpace() {
        LOGGER.info("Undeploying space");
        try {
            remoteSpaceController.undeploy();
        } catch (Exception e) {
            LOGGER.error("Failed to undeploy!", e);
        }

        LOGGER.info("Stopping spacecontroller");

        if (getOutputDir() != null) {
            LOGGER.info("Dumping XAP logs...");
            try {
                remoteSpaceController.dumpLogsToDir(getOutputDir());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                LOGGER.error("Failed to dump XAP logs", throwable);
            }
        }

        LOGGER.info("Stopping XAP");

        remoteSpaceController.stop();
    }

    private File getOutputDir() {
        String newmanTestDir = System.getProperty("newman.test.path");
        if (newmanTestDir == null){
            return null;
        }
        return new File(newmanTestDir);
    }


    protected String getWebAppAddress() {
        return webAppAddress;
    }

    public void test() throws IOException {
        test(expectedTestResult);
    }

    public void performLogin(HTTPUtils.HTTPSession session, String username, String password) {
        if (shiroSecurityConfiguration instanceof WithLoginShiroSecurityConfiguration) {
            HTTPUtils.HTTPPostRequest postRequest = new HTTPUtils.HTTPPostRequest(getWebAppAddress() + "/login.jsp");
            postRequest.withParameter("username", username)
                    .withParameter("password", password);
            HTTPUtils.HTTPResponse response = session.send(postRequest);
            System.out.println(response.getStatusCode()+","+response.getCookies());
            Assert.assertEquals("Unexpected status code", 302, response.getStatusCode());
        } else if (shiroSecurityConfiguration instanceof WithLoginSpringSecurityConfiguration) {
            HTTPUtils.HTTPPostRequest postRequest = new HTTPUtils.HTTPPostRequest(getWebAppAddress() + "/j_spring_security_check");
            postRequest.withParameter("j_username", username)
                    .withParameter("j_password", password);
            HTTPUtils.HTTPResponse response = session.send(postRequest);
            System.out.println(response.getStatusCode()+","+response.getCookies());
            Assert.assertEquals("Unexpected status code", 302, response.getStatusCode());
        }
    }

    public void login() {
        if (!isLoggedIn) {
            System.out.println("Performing Login");
            for (int i = 0; i < sessions.size(); i++) {
                performLogin(sessions.get(i), "user" + (i+1), "user" + (i+1));
            }
            isLoggedIn = true;
            System.out.println("Finished Login Phase");
        }
    }

    public void test(ExpectedTestResult expectedTestResult) throws IOException {
        int iterations = 4;
        System.out.println("isLoggedIn? " + isLoggedIn);
        login();

        String requestsAddress = getWebAppAddress();
        //Write
        for (int j = 0; j < iterations; j++) {

            for (int i = 0; i < sessions.size(); i++) {
                HTTPUtils.HTTPSession session = sessions.get(i);
                Map<String, DataUnit> data = DataGenerator.generateData();
                //HTTPUtils.HTTPGetRequest getRequest = new HTTPUtils.HTTPGetRequest(requestsAddress);

                //HTTPUtils.HTTPResponse response = session.send(getRequest);
                //System.out.println(response.getCookies() + "," + response.getStatusCode());

                updateSession(session, data, requestsAddress, expectedTestResult);
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
                validateSessionAttributes(session, expected.get(sessionid), requestsAddress, expectedTestResult);
            }
        }
        /*try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        if(expectedTestResult.getResponseCode() != 500)
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

            updateSession(session, expectedBySessionID, requestsAddress, expectedTestResult);
            validateSessionAttributes(session, expected.get(sessionid), requestsAddress, expectedTestResult);
        }
        System.out.println("UPDATE ENDS");
       /* try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        if(expectedTestResult.getResponseCode() != 500)
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

            updateSession(session, expectedBySessionID, requestsAddress, expectedTestResult);
            validateSessionAttributes(session, expected.get(sessionid), requestsAddress, expectedTestResult);
        }
       /* try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        if(expectedTestResult.getResponseCode() != 500)
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
        performLogin(session, "user1", "user1");

        Map<String, DataUnit> data = DataGenerator.generateData(1);
        DataUnit attribute = data.values().iterator().next();

        HTTPUtils.HTTPResponse firstResponse = sendRequest(session, attribute, getWebAppAddress());

        Assert.assertEquals("Unexpected status code. Body: "+firstResponse.getBody(), 200, firstResponse.getStatusCode());
        JSONObject firstResponseJSON  = new JSONObject(firstResponse.getBody());

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
        performLogin(session, "user1", "user1");

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

    public void validateSessionAttributes(HTTPUtils.HTTPSession session, Map<String, DataUnit> expected, String requestAddress, ExpectedTestResult expectedTestResult) throws IOException {
        HTTPUtils.HTTPGetRequest getRequest = new HTTPUtils.HTTPGetRequest(requestAddress+"/json/view");
        HTTPUtils.HTTPResponse response = session.send(getRequest);

        if (response.getStatusCode() != expectedTestResult.getResponseCode()) {
            System.out.println(response.getBody());
            Assert.assertEquals("Unexpected status code. Response body: [" + response.getBody() + "]", expectedTestResult.getResponseCode(), response.getStatusCode());
        }

        if(expectedTestResult.getResponseCode() == 500)
            return;

        for (String key : expected.keySet()) {
            DataUnit dataUnit = expected.get(key);
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
        }

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

    public void updateSession(HTTPUtils.HTTPSession session, Map<String, DataUnit> expected, String requestsAddress, ExpectedTestResult expectedTestResult) throws IOException {
        for (String key : expected.keySet()) {
            DataUnit dataUnit = expected.get(key);
            if (dataUnit.getDatavalue() == null) {
                //Means that this attribute was deleted!
                continue;
            }

            HTTPUtils.HTTPResponse response = sendRequest(session, expected.get(key), requestsAddress);
            System.out.println(response.getStatusCode()+","+response.getCookies());
            if (response.getStatusCode() != expectedTestResult.getResponseCode()) {
                System.out.println(response.getBody());
                Assert.assertEquals("Unexpected status code. Response body: [" + response.getBody() + "]", expectedTestResult.getResponseCode(), response.getStatusCode());
            }

            if(expectedTestResult.getResponseCode() == 500)
                return;

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


    public void testMultiSessionsForSameUser() throws Exception{
        //Login with user1 and add one attribute
        HTTPUtils.HTTPSession session1 = new HTTPUtils.HTTPSession();
        performLogin(session1, "user1", "user1");

        Map<String, DataUnit> data = DataGenerator.generateData(2);
        Iterator<DataUnit> iterator = data.values().iterator();
        DataUnit attribute1 = iterator.next();
        DataUnit attribute2 = iterator.next();

        HTTPUtils.HTTPResponse firstResponse = sendRequest(session1, attribute1, getWebAppAddress());

        Assert.assertEquals("Unexpected status code. Body: " + firstResponse.getBody(), 200, firstResponse.getStatusCode());
        JSONObject firstResponseJSON  = new JSONObject(firstResponse.getBody());

        //Check that there are 3 attributes for the first session
        Assert.assertEquals("First session must have three attributes", 3, mapper.readValue(firstResponseJSON.getJSONObject("attributes").toString(), HashMap.class).size());


        //Login with user1 (another session)
        HTTPUtils.HTTPSession session2 = new HTTPUtils.HTTPSession();
        performLogin(session2, "user1", "user1");

        //Check that the attributes are still there...
        HTTPUtils.HTTPGetRequest getRequest = new HTTPUtils.HTTPGetRequest(getWebAppAddress()+"/json/view");
        HTTPUtils.HTTPResponse secondResponse = session2.send(getRequest);
        System.out.println("Got: "+secondResponse.getBody());
        JSONObject secondResponseJSON = new JSONObject(secondResponse.getBody());
        Assert.assertEquals("Attributes should be the same after login", mapper.readValue(firstResponseJSON.getJSONObject("attributes").toString(), HashMap.class), mapper.readValue(secondResponseJSON.getJSONObject("attributes").toString(), HashMap.class));
        Assert.assertEquals("Second session must have three attributes", 3, mapper.readValue(secondResponseJSON.getJSONObject("attributes").toString(), HashMap.class).size());
        Assert.assertFalse("session id should not be the same", firstResponseJSON.getJSONObject("more").getString("sessionid").equals(secondResponseJSON.getJSONObject("more").getString("sessionid")));


        //Add attribute via the second session and check that it is added to the first session
        HTTPUtils.HTTPResponse thirdResponse = sendRequest(session2, attribute2, getWebAppAddress());
        Assert.assertEquals("Unexpected status code. Body: " + thirdResponse.getBody(), 200, thirdResponse.getStatusCode());
        JSONObject thirdResponseJSON  = new JSONObject(thirdResponse.getBody());
        Assert.assertEquals("Second session must have four attributes", 4, mapper.readValue(thirdResponseJSON.getJSONObject("attributes").toString(), HashMap.class).size());

        //Check first session
        HTTPUtils.HTTPGetRequest fourthRequest = new HTTPUtils.HTTPGetRequest(getWebAppAddress()+"/json/view");
        HTTPUtils.HTTPResponse fourthResponse = session1.send(fourthRequest);
        JSONObject fourthResponseJSON = new JSONObject(fourthResponse.getBody());
        Assert.assertEquals("First session must have four attributes", 4, mapper.readValue(fourthResponseJSON.getJSONObject("attributes").toString(), HashMap.class).size());

        Assert.assertEquals("Attributes should be the same after adding new attribute", mapper.readValue(thirdResponseJSON.getJSONObject("attributes").toString(), HashMap.class), mapper.readValue(fourthResponseJSON.getJSONObject("attributes").toString(), HashMap.class));
    }

}
