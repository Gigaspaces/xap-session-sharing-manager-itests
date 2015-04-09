package com.gigaspaces.httpsession.qa.oldtests;

import java.util.Map;

import org.junit.Test;

import com.gigaspaces.httpsession.sessions.FullStoreMode;

public class FullModeTestWithLogin extends TestWithLoginBase {

	@Override
	protected String getDataFileName() {
		return "data.full.withlogin.csv";
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
