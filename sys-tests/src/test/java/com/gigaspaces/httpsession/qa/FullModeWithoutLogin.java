package com.gigaspaces.httpsession.qa;

import java.util.Map;

import org.junit.Test;

import com.gigaspaces.httpsession.sessions.FullModelManager;

public class FullModeWithoutLogin extends TestWithoutLoginBase {

	@Test
	public void testJboss() {

		runJbossTest();

		assertSpace(USERS_VALUE);
	}

	@Test
	public void testTomcat() {

		runTomcatTest();

		assertSpace(USERS_VALUE);
	}

	@Test
	public void testJetty() {

		runJettyTest();

		assertSpace(USERS_VALUE);
	}

	@Override
	public Map<String, String> getConfiguration() {
		Map<String, String> properties = super.getConfiguration();

		properties.put("main/modelManager", FullModelManager.class.getName());

		return properties;
	}
}
