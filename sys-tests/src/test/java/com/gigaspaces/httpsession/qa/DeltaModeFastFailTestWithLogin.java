package com.gigaspaces.httpsession.qa;

import java.util.Map;

import org.junit.Test;

import com.gigaspaces.httpsession.policies.FailFastChangeStrategy;
import com.gigaspaces.httpsession.sessions.DeltaStoreMode;

public class DeltaModeFastFailTestWithLogin extends TestWithLoginBase {

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
		return "data.delta.fast.withlogin.csv";
	}

	@Override
	public Map<String, String> getConfiguration() {

		Map<String, String> properties = super.getConfiguration();

		properties.put("main/storeMode", DeltaStoreMode.class.getName());
		properties.put("main/storeMode.policyType",
				FailFastChangeStrategy.class.getName());

		return properties;
	}

}
