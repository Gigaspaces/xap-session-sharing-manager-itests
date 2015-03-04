package com.gigaspaces.httpsession.qa;

import java.util.Map;

import org.junit.Test;

import com.gigaspaces.httpsession.sessions.DeltaStoreMode;

public class DeltaModeTestWithLogin extends TestWithLoginBase {

	@Test
	public void testJboss() {

		runJbossTest();

		assertSpaceDeltaMode(USERS_VALUE, SystemTestCase.DEFAULT_SESSION_BASE_NAME);
	}

	@Test
	public void testTomcat() {

		runTomcatTest();

		assertSpaceDeltaMode(USERS_VALUE, SystemTestCase.DEFAULT_SESSION_BASE_NAME);
	}

	@Test
	public void testJetty() {

		runJettyTest();

		assertSpaceDeltaMode(USERS_VALUE, SystemTestCase.DEFAULT_SESSION_BASE_NAME);
	}

	@Override
	protected String getDataFileName() {
		return "data.delta.withlogin.csv";
	}

	@Override
	public Map<String, String> getConfiguration() {
		Map<String, String> properties = super.getConfiguration();
		properties.put("main/storeMode", DeltaStoreMode.class.getName());
		return properties;
	}
}
