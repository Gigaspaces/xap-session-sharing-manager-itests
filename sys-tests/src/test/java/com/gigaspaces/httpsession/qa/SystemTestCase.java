package com.gigaspaces.httpsession.qa;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.Ini.Section;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

import com.gigaspaces.httpsession.models.AttributeData;
import com.gigaspaces.httpsession.models.SpaceSessionAttributes;
import com.gigaspaces.httpsession.models.SpaceSessionBase;
import com.gigaspaces.httpsession.models.SpaceSessionByteArray;
import com.gigaspaces.httpsession.qa.utils.Config;
import com.gigaspaces.httpsession.qa.utils.DataGenerator;
import com.gigaspaces.httpsession.qa.utils.JbossController;
import com.gigaspaces.httpsession.qa.utils.JettyController;
import com.gigaspaces.httpsession.qa.utils.JmeterTask;
import com.gigaspaces.httpsession.qa.utils.ServerController;
import com.gigaspaces.httpsession.qa.utils.EmbeddedSpaceController;
import com.gigaspaces.httpsession.qa.utils.TomcatController;
import com.gigaspaces.httpsession.serialize.CompressUtils;
import com.gigaspaces.httpsession.serialize.KryoSerializerImpl;
import com.gigaspaces.httpsession.serialize.NonCompressCompressor;
import com.gigaspaces.httpsession.serialize.SerializeUtils;
import com.j_spaces.core.client.SQLQuery;

public abstract class SystemTestCase {

	public static final String DATA_BASE = "src/test/resources/jmeter/data";

	public static final String APP_NAME = "app";

	// Jmeter constants
	public static final String LOOP_COUNT_NAME = "loop_count";
	public static final String USERS_NAME = "users";
	public static final String DATAFILE_NAME = "datafile";

	protected ServerController server;

	protected static final int JBOSS_SERVER_KEY = 1;
	protected static final int TOMCAT_SERVER_KEY = 2;
	protected static final int JETTY_SERVER_KEY = 3;

	protected EmbeddedSpaceController space = new EmbeddedSpaceController();

	private Map<String, DataUnit> expected;

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
	public final void config(String file, Map<String, String> properties) {

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
		jmeter.addParam(DATAFILE_NAME,
				FilenameUtils.concat("data", getDataFileName()));
	}

	protected String getDataFileName() {
		return "data1.csv";
	}

	protected abstract String getScript();

	protected final void runJmeterScript(String host, int port) {
		JmeterTask jmeter = new JmeterTask(getScript());

		jmeter.setHost(host);
		jmeter.setPort("" + port);
		jmeter.setAppName("/" + APP_NAME);

		setJmeterParameters(jmeter);

		jmeter.startAndWait();

	}

	public abstract String getFile();

	public abstract Map<String, String> getConfiguration();

	@Before
	public final void startup() {

		space.start();
	}

	public final void runJbossTest() {
		server = new JbossController(Config.getHost(), 8080);

		runTest();
	}

	public final void runTomcatTest() {
		server = new TomcatController(Config.getHost(), 9090);

		runTest();
	}

	public final void runJettyTest() {
		server = new JettyController(Config.getHost(), 7070);

		runTest();
	}

	private void runTest() {
		config(getFile(), getConfiguration());

		server.start();

		generateTestData();

		runJmeterScript(server.getHost(), server.getPort());
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

		if (server == null)
			return;

		server.stop();

		server.undeploy(APP_NAME);

		space.stop();
	}

	@SuppressWarnings({})
	protected void assertSpaceDeltaMode(int count) {

		SpaceSessionAttributes[] data = readSpaceData(SpaceSessionAttributes.class);

		Assert.assertEquals("invalid length result:", count, data.length);

		for (SpaceSessionAttributes buff : data) {

			Map<String, AttributeData> actual = buff.getAttributes();

			Iterator<String> keyIterator = expected.keySet().iterator();

			while (keyIterator.hasNext()) {
				String key = keyIterator.next();

				Object expectedValue = expected.get(key).getDatavalue();
				Object actualValue = SerializeUtils.deserialize(actual.get(key)
						.getValue());

				Assert.assertEquals(expectedValue, actualValue);
			}
		}
	}

	@SuppressWarnings({ "unchecked" })
	protected void assertSpaceFullMode(int count) {

		SpaceSessionByteArray[] data = readSpaceData(SpaceSessionByteArray.class);

		Assert.assertEquals("invalid length result:", count, data.length);

		for (SpaceSessionByteArray buff : data) {

			Map<Object, Object> actual = (Map<Object, Object>) SerializeUtils
					.deserialize(buff.getAttributes());

			Iterator<String> keyIterator = expected.keySet().iterator();

			while (keyIterator.hasNext()) {
				String key = keyIterator.next();

				Object expectedValue = expected.get(key).getDatavalue();
				Object actualValue = actual.get(key);

				Assert.assertEquals(expectedValue, actualValue);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	private <T extends SpaceSessionBase<?>> T[] readSpaceData(Class<T> clazz) {
		SQLQuery<T> sqlQuery = new SQLQuery<T>(clazz, "");

		T[] data = (T[]) Array.newInstance(clazz, 0);

		try {
			data = (T[]) space.getSpace().readMultiple(sqlQuery, null,
					Integer.MAX_VALUE);
		} catch (Throwable e) {
			throw new Error(e);
		}

		return data;
	}

}
