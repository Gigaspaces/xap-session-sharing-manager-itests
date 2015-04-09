package com.gigaspaces.httpsession.qa.oldtests;

import com.gigaspaces.httpsession.sessions.FullStoreMode;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;

@Ignore
public class FullModeWithoutLogin extends TestWithoutLoginBase {
	
	@Override
	protected String getDataFileName() {

		return "data.full.withoutlogin.csv";
	}

	@Test
	public void testJboss() {

		runJbossTest();

		assertSpaceFullMode(USERS_VALUE, SystemTestCase.DEFAULT_SESSION_BASE_NAME);
	}

	@Test
	public void testTomcat() {

		runTomcatTest();

		assertSpaceFullMode(USERS_VALUE, SystemTestCase.DEFAULT_SESSION_BASE_NAME);
	}

	@Test
	public void testWithFOTomcat() {

		runTomcatTestWithFO();

		assertSpaceFullMode(USERS_VALUE, SystemTestCase.DEFAULT_SESSION_BASE_NAME);
	}

	@Test
	public void testJetty() {

		runJettyTest();

		assertSpaceFullMode(USERS_VALUE, SystemTestCase.DEFAULT_SESSION_BASE_NAME);
	}

	@Override
	public Map<String, String> getConfiguration() {
		Map<String, String> properties = super.getConfiguration();

		properties.put("main/storeMode", FullStoreMode.class.getName());

		return properties;
	}
}
