package com.gigaspaces.httpsession.qa.utils;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public class Config {
	private static final String HOST = "HOST";
	private static final String JBOSS_HOME = "JBOSS_HOME";
	private static final String JBOSS8_HOME = "JBOSS8_HOME";
	private static final String TOMCAT_HOME = "TOMCAT_HOME";
	private static final String JETTY_HOME = "JETTY_HOME";
	private static final String GS_HOME = "GS_HOME";
	private static final String JMETER_HOME = "JMETER_HOME";
	private static final String TEST_RESOURCES_CONFIG_PROPERTIES = "src/test/resources/config/config.properties";
    private static final String APACHE_HOME = "APACHE_HOME";
    private static final String WEBSPHERE_HOME = "WEBSPHERE_HOME";
    private static final String JAVA7_HOME = "JAVA7_HOME";
    private static final String XAP_LOOKUP_GROUPS = "XAP_LOOKUP_GROUPS";

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(Config.class);
    private static Properties properties = new Properties();

    private static Map<String, String> envsWithJavaHome = new HashMap<String, String>();

	static {
		readConfig();
        String prefix = System.getProperty("group", System.getenv("XAP_LOOKUP_GROUPS"));
        boolean useExistingAgent = Boolean.valueOf(System.getProperty("useExistingAgent", "false"));
        String groups = (useExistingAgent ? prefix : prefix + UUID.randomUUID().toString());
        properties.put(XAP_LOOKUP_GROUPS, groups);

        //Set global env variables
        envsWithJavaHome.put("JAVA_HOME", getJava7Home());
    }

	public static String getProperty(String key) {
		return System.getProperty(key, properties.getProperty(key));
	}

	private static void readConfig() {
		properties = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(
                    TEST_RESOURCES_CONFIG_PROPERTIES);
			properties.load(fis);
			
		} catch (FileNotFoundException e) {
			Log.error("File not found:" + TEST_RESOURCES_CONFIG_PROPERTIES, e,
					AssertionError.class);
		} catch (IOException e) {
			Log.error("Error occurs while loading properites", e,
					AssertionError.class);
		} finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    LOGGER.error("Failed to close FileInputStream", e);
                }
            }
        }
    }

	public static String getAbrolutePath(String relativePath){
		
		return FilenameUtils.concat(getBasePath(), relativePath);
	}
	
	private static String getBasePath() {
		
		return getProperty("project.basedir");
	}

	public static String getJbossHome() {

		return getProperty(JBOSS_HOME);
	}

	public static String getJboss8Home() {

		return getProperty(JBOSS8_HOME);
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

    public static String getApacheHome() {
        return getProperty(APACHE_HOME);
    }

    public static String getWebsphereHome() {
        return getProperty(WEBSPHERE_HOME);
    }

    public static String getJava7Home() {
        return getProperty(JAVA7_HOME);
    }

    public static String getLookupGroups() {
        return getProperty(XAP_LOOKUP_GROUPS);
    }

    public static Map<String, String> getEnvsWithJavaHome() {
        return envsWithJavaHome;
    }
}
