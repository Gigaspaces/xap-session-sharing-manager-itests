package com.gigaspaces.httpsession.qa.oldtests;

import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.httpsession.models.AttributeData;
import com.gigaspaces.httpsession.qa.DataUnit;
import com.gigaspaces.httpsession.qa.utils.*;
import com.gigaspaces.httpsession.serialize.CompressUtils;
import com.gigaspaces.httpsession.serialize.KryoSerializerImpl;
import com.gigaspaces.httpsession.serialize.NonCompressCompressor;
import com.gigaspaces.httpsession.serialize.SerializeUtils;
import com.gigaspaces.httpsession.sessions.StoreMode;
import com.j_spaces.core.client.SQLQuery;
import org.apache.commons.io.FilenameUtils;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.Ini.Section;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openspaces.admin.Admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class SystemTestCase {

	protected static final String SESSION_SPACE = "sessionSpace";

	public static final String DATA_BASE = "src/test/resources/jmeter/data";

	public static final String APP_NAME = "demo-app";

	// Jmeter constants
	public static final String LOOP_COUNT_NAME = "loop_count";
	public static final String USERS_NAME = "users";
	public static final String DATAFILE_NAME = "datafile";

	protected ServerController server;

	protected static final int JBOSS_SERVER_KEY = 1;
	protected static final int TOMCAT_SERVER_KEY = 2;
	protected static final int JETTY_SERVER_KEY = 3;

	public static final String DEFAULT_SESSION_BASE_NAME = "com.gigaspaces.httpsession.models.DefaultSpaceSessionStore";

	protected RemoteSpaceController space = new RemoteSpaceController(
			SESSION_SPACE, 2, 1);

	//protected EmbeddedSpaceController  space = new EmbeddedSpaceController(); 
	
	protected Map<String, DataUnit> expected;

	@BeforeClass
	public static void init() {

		CompressUtils.register(new NonCompressCompressor());

		SerializeUtils.register(new KryoSerializerImpl());
	}

	/**
	 * @param file
	 *            - template shiro.ini file
	 * @param properties
	 *            - properties to be replaced [section]/[propertyName] = [value]
	 */
	public void config(String file, Map<String, String> properties) {

		properties.put("main/connector.url", "jini://*/*/" + SESSION_SPACE + "?groups="+Config.getLookupGroups());

		try {
			server.deploy(APP_NAME);
		} catch (IOException e) {
			throw new Error(e);
		}

		Ini ini = new Ini();

		ini.loadFromPath(file);

		Iterator<String> keyIterator = properties.keySet().iterator();

		while (keyIterator.hasNext()) {

			String key = keyIterator.next();
			String[] pv = key.split("/");

			if (pv != null && pv.length == 2) {
				String sectionName = pv[0];
				String propertyName = pv[1];
				String propertyValue = properties.get(key);

				ini.setSectionProperty(sectionName, propertyName, propertyValue);
			}
		}

		saveIni(ini);
	}

	/**
	 * @param ini
	 */
	private void saveIni(Ini ini) {
		List<String> lines = new ArrayList<String>();

		Iterator<String> sections = ini.getSectionNames().iterator();

		while (sections.hasNext()) {
			String sectionName = sections.next();

			System.out.println("[" + sectionName + "]");

			lines.add("[" + sectionName + "]");

			Section sec = ini.getSection(sectionName);

			Iterator<String> keys = sec.keySet().iterator();

			while (keys.hasNext()) {
				String key = keys.next();

				System.out.println(key + "=" + sec.get(key));

				lines.add(key + "=" + sec.get(key));
			}
		}

		try {
			server.saveShiroFile(APP_NAME, lines);
		} catch (IOException e) {
			throw new Error("can not save shiro.ini", e);
		}
	}

	protected void setJmeterParameters(JmeterTask jmeter) {
		jmeter.addParam(
				DATAFILE_NAME,
				FilenameUtils.concat(System.getProperty("user.dir")
						+ "/src/test/resources/jmeter/data", getDataFileName()));
	}

	protected String getDataFileName() {
		return "data1.csv";
	}

	protected abstract String getScript();

	protected final void runJmeterScript(String host, int port) {
		JmeterTask jmeter = new JmeterTask();
		jmeter.setHost(host);
		jmeter.setPort("" + port);
		jmeter.setAppName("/" + APP_NAME);

		setJmeterParameters(jmeter);
		jmeter.setScript(System.getProperty("user.dir") + "/" + getScript());
		System.out.println("commands are: " + jmeter.getCommands());
		jmeter.startAndWait();

	}

	public abstract String getFile(boolean isSecured);

	public abstract Map<String, String> getConfiguration();

    @Before
	public final void startup() {
		
		space.start();
		
		try {
			space.deploy(SESSION_SPACE);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	public final void runJbossTest() {
		server = new JbossController(Config.getHost(), 8080);

		runTest();
	}

	public final void runTomcatTest() {
		server = new TomcatController(Config.getHost(), 9090);

		runTest();
	}

	public final void runTomcatTestWithFO() {
		server = new TomcatController(Config.getHost(), 9090);

		runTestWithFO(false);
	}

	public final void runSecuredTomcatTest() {
		server = new TomcatController(Config.getHost(), 9090, true, false);

		runTest(true);
	}

	public final void runJettyTest() {
		server = new JettyController(Config.getHost(), 7070);

		runTest();
	}

	private void runTest() {
		runTest(false);
	}

	private void runTest(boolean isSecured) {
		config(getFile(isSecured), getConfiguration());

		server.start();

		// TODO change the sleep - wait for the end of the server's bootstrap
		// and continue
		try {
			Thread.sleep(1000 * 20);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		generateTestData();

		runJmeterScript(server.getHost(), server.getPort());
	}

	private void runTestWithFO(boolean isSecured) {
		config(getFile(isSecured), getConfiguration());

		server.start();

		// TODO change the sleep - wait for the end of the server's bootstrap
		// and continue
		try {
			Thread.sleep(1000 * 20);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		generateTestData();

		runJmeterScript(server.getHost(), server.getPort());

		Admin admin = space.getAdmin();

		AdminUtils.restartPrimaries(admin, SESSION_SPACE);

		AdminUtils.waitForPrimaries(admin, SESSION_SPACE, 2);
		AdminUtils.waitForBackups(admin, SESSION_SPACE, 2);

	}

	protected void generateTestData() {

		String path = FilenameUtils.concat(DATA_BASE, getDataFileName());

		try {
			expected = DataGenerator.generateDataFile(path);
		} catch (IOException e) {
			throw new Error("can not generate data file:" + path, e);
		}
	}

	@After
	public final void teardown() throws IOException {

		space.undeploy(SESSION_SPACE);
		
		space.stop();
		
		if (server == null)
			return;

		server.stop();

		server.undeploy(APP_NAME);
	}

//	protected void assertSpaceDeltaMode(int count, boolean isSecured) {
//		if(isSecured)
//			assertSpaceDeltaMode(count + 1);//we registered javax.security.auth.Subject
//		else
//			assertSpaceDeltaMode(count);
//	}

//	@SuppressWarnings({})
//	protected void assertSpaceDeltaMode(int count) {
//
//		SpaceSessionAttributes[] data = readSpaceData(SpaceSessionAttributes.class);
//
//		Assert.assertEquals("invalid length result:", count, data.length);
//
//		for (SpaceSessionAttributes buff : data) {
//
//			Map<String, AttributeData> actual = buff.getAttributes();
//
//			Iterator<String> keyIterator = expected.keySet().iterator();
//
//			while (keyIterator.hasNext()) {
//				String key = keyIterator.next();
//
//				Object expectedValue = expected.get(key).getDatavalue();
//				Object actualValue = SerializeUtils.deserialize(actual.get(key)
//						.getValue());
//
//				Assert.assertEquals(expectedValue, actualValue);
//			}
//		}
//	}

//	protected void assertSpaceFullMode(int count, boolean isSecured) {
//		if(isSecured)
//			assertSpaceFullMode(count + 1);//we registered javax.security.auth.Subject
//		else
//			assertSpaceFullMode(count);
//	}

//	@SuppressWarnings({ "unchecked" })
//	protected void assertSpaceFullMode(int count) {
//
//		SpaceSessionByteArray[] data = readSpaceData(SpaceSessionByteArray.class);
//
//		Assert.assertEquals("invalid length result:", count, data.length);
//
//		for (SpaceSessionByteArray buff : data) {
//
//			Map<Object, Object> actual = (Map<Object, Object>) SerializeUtils
//					.deserialize(buff.getAttributes());
//
//			Iterator<String> keyIterator = expected.keySet().iterator();
//
//			while (keyIterator.hasNext()) {
//				String key = keyIterator.next();
//
//				Object expectedValue = expected.get(key).getDatavalue();
//				Object actualValue = actual.get(key);
//
//				Assert.assertEquals(expectedValue, actualValue);
//			}
//		}
//	}

//	@SuppressWarnings({ "unchecked", "deprecation" })
//	private <T extends SpaceSessionBase<?>> T[] readSpaceData(Class<T> clazz) {
//		SQLQuery<T> sqlQuery = new SQLQuery<T>(clazz, "");
//
//		T[] data = (T[]) Array.newInstance(clazz, 0);
//
//		try {
//			data = (T[]) space.getSpace().readMultiple(sqlQuery, null,
//					Integer.MAX_VALUE);
//		} catch (Throwable e) {
//			throw new Error(e);
//		}
//
//		return data;
//	}

	@SuppressWarnings({})
	protected void assertSpaceDeltaMode(int count, String type) {

		SpaceDocument[] data = readSpaceData(type);

		Assert.assertEquals("invalid length result:", count, data.length);

		for (SpaceDocument buff : data) {

			Map<String, AttributeData> actual =  buff.getProperty(StoreMode.PROPERTY_ATTRIBUTES);

			Iterator<String> keyIterator = expected.keySet().iterator();

			while (keyIterator.hasNext()) {
				String key = keyIterator.next();

				Object expectedValue = expected.get(key).getDatavalue();
                if (!actual.containsKey(key)) {
                    Assert.fail("session does not have the key ["+key+"]");
                }
				Object actualValue = SerializeUtils.deserialize(actual.get(key)
						.getValue());

				Assert.assertEquals(expectedValue, actualValue);
			}
		}
	}


	@SuppressWarnings({ "unchecked" })
	protected void assertSpaceFullMode(int count, String type) {

		SpaceDocument[] data = readSpaceData(type);

		Assert.assertEquals("invalid length result:", count, data.length);

		for (SpaceDocument buff : data) {

			Map<Object, Object> actual = (Map<Object, Object>) SerializeUtils
					.deserialize((byte[]) buff.getProperty(StoreMode.PROPERTY_ATTRIBUTES));

			Iterator<String> keyIterator = expected.keySet().iterator();

			while (keyIterator.hasNext()) {
				String key = keyIterator.next();

				Object expectedValue = expected.get(key).getDatavalue();
				Object actualValue = actual.get(key);

				Assert.assertEquals(expectedValue, actualValue);
			}
		}
	}

	private SpaceDocument[]  readSpaceData(String type) {
		SQLQuery<SpaceDocument> sqlQuery = new SQLQuery<SpaceDocument>(type, "");
		SpaceDocument[] data;
		try {
			data = (SpaceDocument[]) space.getSpace().readMultiple(sqlQuery, Integer.MAX_VALUE);
		} catch (Throwable e) {
			throw new Error(e);
		}

		return data;
	}

}
