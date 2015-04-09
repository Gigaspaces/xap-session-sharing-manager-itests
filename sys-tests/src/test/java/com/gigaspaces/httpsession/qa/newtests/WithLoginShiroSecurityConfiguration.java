package com.gigaspaces.httpsession.qa.newtests;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Yohana Khoury
 * @since 10.1
 */
public class WithLoginShiroSecurityConfiguration extends ShiroSecurityConfigurationBase {
    private static final int USERS_VALUE = 10;

    @Override
    public String getFile(boolean isSecured) {
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

        return properties;
    }
}
