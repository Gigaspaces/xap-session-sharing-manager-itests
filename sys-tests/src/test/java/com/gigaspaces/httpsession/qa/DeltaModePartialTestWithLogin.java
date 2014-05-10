package com.gigaspaces.httpsession.qa;

import java.util.Map;

import org.junit.Test;

import com.gigaspaces.httpsession.policies.PartialChangePolicy;
import com.gigaspaces.httpsession.sessions.DeltaModeManager;

public class DeltaModePartialTestWithLogin extends TestWithLoginBase {

	@Test
	public void testJboss() {

		runJbossTest();

		assertSpaceDeltaMode(USERS_VALUE);
	}

	@Test
	public void testTomcat() {

		runTomcatTest();

		assertSpaceDeltaMode(USERS_VALUE);
	}

	@Test
	public void testJetty() {

		runJettyTest();

		assertSpaceDeltaMode(USERS_VALUE);
	}

	@Override
	protected String getDataFileName() {

		return "data.delta.partial.withlogin.csv";
	}

	@Override
	public Map<String, String> getConfiguration() {

		Map<String, String> properties = super.getConfiguration();

		properties.put("main/modelManager", DeltaModeManager.class.getName());
		properties.put("main/modelManager.policyType",
				PartialChangePolicy.class.getName());

		return properties;
	}
}
