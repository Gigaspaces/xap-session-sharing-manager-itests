package com.gigaspaces.httpsession.qa.newtests.bases;

import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.httpsession.qa.DataUnit;
import com.gigaspaces.httpsession.serialize.SerializeUtils;
import com.gigaspaces.httpsession.sessions.FullStoreMode;
import com.gigaspaces.httpsession.sessions.StoreMode;
import com.gigaspaces.internal.client.spaceproxy.ISpaceProxy;
import org.junit.Assert;

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
    public void assertSpaceMode(ISpaceProxy space, int expectedSessions, HashMap<String, Map<String, DataUnit>> expected, String type) {
        SpaceDocument[] data = readSpaceData(space, type);
        Assert.assertEquals("invalid length result:", expectedSessions, data.length);

        for (SpaceDocument buff : data) {

            Map<Object, Object> actual = (Map<Object, Object>) SerializeUtils
                    .deserialize((byte[]) buff.getProperty(StoreMode.PROPERTY_ATTRIBUTES));

            Map<String, DataUnit> expectedForSession = expected.get(buff.getProperty("SESSION_ID"));
            Assert.assertEquals("Unexpected attributes size in space", expectedForSession.keySet().size(), actual.keySet().size());
            Iterator<String> keyIterator = expectedForSession.keySet().iterator();

            while (keyIterator.hasNext()) {
                String key = keyIterator.next();
                Object expectedValue = expectedForSession.get(key).getDatavalue();
                Object actualValue = actual.get(key);

                if (!actual.containsKey(key) && expectedValue != null) { // null=deleted
                    Assert.fail("session does not have the key ["+key+"]");
                }

                Assert.assertEquals("Unexpected attribute ["+key+"] value", expectedValue, actualValue);
            }
        }
    }
}
