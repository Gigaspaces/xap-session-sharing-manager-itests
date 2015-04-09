package com.gigaspaces.httpsession.qa.newtests;

import java.util.Map;

/**
 * @author Yohana Khoury
 * @since 10.1
 */
public abstract class ShiroSecurityConfigurationBase {
    public abstract String getFile(boolean isSecured);
    public abstract Map<String, String> getConfiguration();
}
