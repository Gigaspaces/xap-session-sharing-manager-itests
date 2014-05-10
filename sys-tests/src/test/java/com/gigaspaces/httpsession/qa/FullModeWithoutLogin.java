package com.gigaspaces.httpsession.qa;

import java.util.Map;

import org.junit.Test;

import com.gigaspaces.httpsession.sessions.FullModelManager;

public class FullModeWithoutLogin extends TestWithoutLoginBase {

	@Override
	protected String getDataFileName() {

		return "data.full.withoutlogin.csv";
	}

	@Test
	public void testJboss() {

		runJbossTest();

		assertSpaceFullMode(USERS_VALUE);
	}

	@Test
	public void testTomcat() {

		runTomcatTest();

		assertSpaceFullMode(USERS_VALUE);
	}

	@Test
	public void testJetty() {

		runJettyTest();

		assertSpaceFullMode(USERS_VALUE);
	}

	@Override
	public Map<String, String> getConfiguration() {
		Map<String, String> properties = super.getConfiguration();

		properties.put("main/modelManager", FullModelManager.class.getName());

		return properties;
	}
}
