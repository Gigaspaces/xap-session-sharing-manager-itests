package com.gigaspaces.httpsession.qa.newtests.bases;

import java.util.Map;

/**
 * @author Yohana Khoury
 * @since 10.1
 */
public class WithLoginShiroSecurityConfigurationWithCompression extends WithLoginShiroSecurityConfiguration {

    @Override
    public Map<String, String> getConfiguration() {
        Map<String, String> conf = super.getConfiguration();
        conf.put("main/compressor", "com.gigaspaces.httpsession.serialize.CompressorImpl");
        conf.put("main/cacheManager.compressor", "$compressor");
        return conf;
    }
}
