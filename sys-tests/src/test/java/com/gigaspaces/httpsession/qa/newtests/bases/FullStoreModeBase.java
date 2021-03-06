package com.gigaspaces.httpsession.qa.newtests.bases;

import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.httpsession.qa.DataUnit;
import com.gigaspaces.httpsession.serialize.SerializeUtils;
import com.gigaspaces.httpsession.sessions.FullStoreMode;
import com.gigaspaces.httpsession.sessions.StoreMode;
import org.junit.Assert;
import org.openspaces.core.GigaSpace;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Yohana Khoury
 * @since 10.1
 */
public class FullStoreModeBase extends StoreModeBase {
    @Override
    public String getStoreModeClassName() {
        return FullStoreMode.class.getName();
    }

    @Override
    public void assertSpaceMode(GigaSpace space, int expectedSessions, HashMap<String, Map<String, DataUnit>> expected, String type) {
        SpaceDocument[] data = readSpaceData(space, type);
        Assert.assertEquals("invalid length result:", expectedSessions, data.length);

        for (SpaceDocument buff : data) {

            Map<Object, Object> actual = (Map<Object, Object>) SerializeUtils
                    .deserialize((byte[]) buff.getProperty(StoreMode.PROPERTY_ATTRIBUTES));
            actual.remove("javax.security.auth.subject"); // This is temporarily until this attribute is filtered and not written to the space
            actual.remove("org.apache.shiro.subject.support.DefaultSubjectContext_AUTHENTICATED_SESSION_KEY");
            actual.remove("org.apache.shiro.subject.support.DefaultSubjectContext_PRINCIPALS_SESSION_KEY");
            Map<String, DataUnit> expectedForSession = expected.get(buff.getProperty("SESSION_ID"));
            Iterator<String> keyIterator = expectedForSession.keySet().iterator();

            int count=0;
            while (keyIterator.hasNext()) {
                String key = keyIterator.next();
                Object expectedValue = expectedForSession.get(key).getDatavalue();
                Object actualValue = actual.get(key);

                if (actualValue == null && expectedValue != null) { // null=deleted
                    Assert.fail("session in space does not have the key ["+key+"]");
                } else if (expectedValue != null) { // not deleted
                    count++;
                }

                Assert.assertEquals("Unexpected value for attribute [" + key + "]", expectedValue, actualValue);
            }

            Assert.assertEquals("Unexpected attributes size in space", count, actual.keySet().size());

        }
    }
}
