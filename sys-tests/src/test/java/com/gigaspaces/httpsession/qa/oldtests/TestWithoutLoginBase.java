package com.gigaspaces.httpsession.qa.oldtests;

import com.gigaspaces.httpsession.qa.utils.JmeterTask;
import org.junit.Ignore;

import java.util.HashMap;
import java.util.Map;

@Ignore
public class TestWithoutLoginBase extends SystemTestCase {

	protected static final int USERS_VALUE = 10;
	protected static final int LOOP_COUNT_VALUE = 30;

	@Override
	protected String getScript() {
		return "src/test/resources/jmeter/withoutLogin.jmx";
	}

	@Override
	public String getFile(boolean isSecured) {
		if(isSecured)
			return "src/test/resources/config/shiro.ini.securedwithoutlogin";

		else
			return "src/test/resources/config/shiro.ini.withoutlogin";
	}

	@Override
	public Map<String, String> getConfiguration() {
		Map<String, String> properties = new HashMap<String, String>();

		return properties;
	}

	@Override
	protected void setJmeterParameters(JmeterTask jmeter) {

		super.setJmeterParameters(jmeter);

		jmeter.addParam(USERS_NAME, "" + USERS_VALUE);
		jmeter.addParam(LOOP_COUNT_NAME, "" + LOOP_COUNT_VALUE);
	}
}
