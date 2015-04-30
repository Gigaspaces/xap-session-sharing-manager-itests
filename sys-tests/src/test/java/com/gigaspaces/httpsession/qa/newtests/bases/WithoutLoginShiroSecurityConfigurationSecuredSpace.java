package com.gigaspaces.httpsession.qa.newtests.bases;

import java.util.Map;

/**
 * @author Yohana Khoury
 * @since 10.1
 */
public class WithoutLoginShiroSecurityConfigurationSecuredSpace extends WithoutLoginShiroSecurityConfiguration {
    @Override
    public Map<String, String> getConfiguration() {
        Map<String, String> conf = super.getConfiguration();
        conf.put("main/connector.username", "user1");
        conf.put("main/connector.password", "user1");
        return conf;
    }
}
