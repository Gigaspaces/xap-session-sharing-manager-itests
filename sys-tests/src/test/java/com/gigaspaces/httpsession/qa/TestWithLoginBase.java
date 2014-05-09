package com.gigaspaces.httpsession.qa;

import java.util.HashMap;
import java.util.Map;

import com.gigaspaces.httpsession.qa.utils.JmeterTask;

public class TestWithLoginBase extends SystemTestCase {

	protected static final int USERS_VALUE = 10;
	protected static final int LOOP_COUNT_VALUE = 30;

	@Override
	protected String getScript() {
		return "src/test/resources/jmeter/withLogin.jmx";
	}

	
	@Override
	public String getFile() {
		return "src/test/resources/config/shiro.ini.withlogin";
	}

	@Override
	public Map<String, String> getConfiguration() {

		Map<String, String> properties = new HashMap<String, String>();
		
		return properties;
	}

	@Override
	protected void setJmeterParameters(JmeterTask jmeter) {

		jmeter.addParam(USERS_NAME, "" + USERS_VALUE);
		jmeter.addParam(LOOP_COUNT_NAME, "" + LOOP_COUNT_VALUE);

	}
}
