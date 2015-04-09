package com.gigaspaces.httpsession.qa.newtests;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Yohana Khoury
 * @since 10.1
 */
public class WithoutLoginShiroSecurityConfiguration extends ShiroSecurityConfigurationBase {
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
}
