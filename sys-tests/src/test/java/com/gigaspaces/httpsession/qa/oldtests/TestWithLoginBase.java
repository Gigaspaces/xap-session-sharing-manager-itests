package com.gigaspaces.httpsession.qa.oldtests;

import com.gigaspaces.httpsession.qa.utils.JmeterTask;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.Ignore;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Ignore
public class TestWithLoginBase extends SystemTestCase {

	protected static final int USERS_VALUE = 10;
	protected static final int LOOP_COUNT_VALUE = 30;

	@Override
	protected String getScript() {
		return "src/test/resources/jmeter/withLogin.jmx";
	}

	@Override
	public String getFile(boolean isSecured) {
		//TODO:add test with security and login
		return "src/test/resources/config/shiro.ini.withlogin";
	}

	@Override
	public Map<String, String> getConfiguration() {

		Map<String, String> properties = new HashMap<String, String>();

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < USERS_VALUE; i++) {
			int num = i + 1;
			properties.put("users/user" + num, "user" + num + ",admin");
			sb.append("user" + num);
			sb.append(",");
			sb.append("user" + num);
			sb.append("\n");
		}

		String path = FilenameUtils.concat(DATA_BASE, "users.csv");

		try {
			FileUtils.writeStringToFile(new File(path), sb.toString());
		} catch (IOException e) {
			new Error("can not create user.csv file", e);
		}

		return properties;
	}

	@Override
	protected void setJmeterParameters(JmeterTask jmeter) {

		super.setJmeterParameters(jmeter);

		jmeter.addParam(USERS_NAME, "" + USERS_VALUE);
		// jmeter.addParam(LOOP_COUNT_NAME, "" + LOOP_COUNT_VALUE);

	}
}
