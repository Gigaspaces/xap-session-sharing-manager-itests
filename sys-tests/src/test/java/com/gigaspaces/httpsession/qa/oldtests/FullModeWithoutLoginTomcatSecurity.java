package com.gigaspaces.httpsession.qa.oldtests;

import com.gigaspaces.httpsession.sessions.FullStoreMode;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;


public class FullModeWithoutLoginTomcatSecurity extends TestWithoutLoginBase {
	
	@Override
	protected String getDataFileName() {

		return "data.full.withoutlogin.csv";
	}

	@Test
	public void testTomcatWithSecurity() {

		runSecuredTomcatTest();

		assertSpaceFullMode(USERS_VALUE, SystemTestCase.DEFAULT_SESSION_BASE_NAME);
	}

	@Override
	public Map<String, String> getConfiguration() {
		Map<String, String> properties = super.getConfiguration();

		properties.put("main/storeMode", FullStoreMode.class.getName());

		return properties;
	}
}
