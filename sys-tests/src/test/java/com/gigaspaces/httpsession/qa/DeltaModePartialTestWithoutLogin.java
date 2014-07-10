package com.gigaspaces.httpsession.qa;

import java.util.Map;

import org.junit.Test;

import com.gigaspaces.httpsession.policies.PartialChangeStrategy;
import com.gigaspaces.httpsession.sessions.DeltaStoreMode;

public class DeltaModePartialTestWithoutLogin extends TestWithoutLoginBase {

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

		return "data.delta.partial.withoutlogin.csv";
	}

	@Override
	public Map<String, String> getConfiguration() {

		Map<String, String> properties = super.getConfiguration();

		properties.put("main/storeMode", DeltaStoreMode.class.getName());
		properties.put("main/storeMode.changeStrategy",
				PartialChangeStrategy.class.getName());

		return properties;
	}
}
