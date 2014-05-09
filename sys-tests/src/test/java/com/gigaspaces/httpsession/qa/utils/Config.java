package com.gigaspaces.httpsession.qa.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Config {
	private static final String HOST = "HOST";
	private static final String JBOSS_HOME = "JBOSS_HOME";
	private static final String TOMCAT_HOME = "TOMCAT_HOME";
	private static final String JETTY_HOME = "JETTY_HOME";
	private static final String GS_HOME = "GS_HOME";
	private static final String JMETER_HOME = "JMETER_HOME";
	private static final String TEST_RESOURCES_CONFIG_PROPERTIES = "src/test/resources/config/config.properties";

	private static Properties properties = new Properties();

	static {
		readConfig();
	}

	public static String getProperty(String key) {
		return properties.getProperty(key);
	}

	private static void readConfig() {
		properties = new Properties();
		try {
			properties.load(new FileInputStream(
					TEST_RESOURCES_CONFIG_PROPERTIES));
		} catch (FileNotFoundException e) {
			Log.error("File not found:" + TEST_RESOURCES_CONFIG_PROPERTIES, e,
					AssertionError.class);
		} catch (IOException e) {
			Log.error("Error occurs while loading properites", e,
					AssertionError.class);
		}
	}

	public static String getJbossHome() {

		return getProperty(JBOSS_HOME);
	}

	public static String getTomcatHome() {

		return getProperty(TOMCAT_HOME);
	}

	public static String getJettyHome() {

		return getProperty(JETTY_HOME);
	}

	public static String getGSHome() {

		return getProperty(GS_HOME);
	}

	public static String getJmeterHome() {
		return getProperty(JMETER_HOME);
	}

	public static String getHost() {
		return getProperty(HOST);
	}

}