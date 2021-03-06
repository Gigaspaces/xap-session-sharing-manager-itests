package com.gigaspaces.httpsession.qa.newtests.bases;

import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.httpsession.models.AttributeData;
import com.gigaspaces.httpsession.qa.DataUnit;
import com.gigaspaces.httpsession.serialize.SerializeUtils;
import com.gigaspaces.httpsession.sessions.DeltaStoreMode;
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
public class DeltaStoreModeBase extends StoreModeBase {
    @Override
    public String getStoreModeClassName() {
        return DeltaStoreMode.class.getName();
    }


    @Override
    public void assertSpaceMode(GigaSpace space, int expectedSessions, HashMap<String, Map<String, DataUnit>> expected, String type) {
        SpaceDocument[] data = readSpaceData(space, type);

        Assert.assertEquals("invalid length result:", expectedSessions, data.length);

        for (SpaceDocument buff : data) {

            Map<String, AttributeData> actual =  buff.getProperty(StoreMode.PROPERTY_ATTRIBUTES);
            actual.remove("javax.security.auth.subject"); // This is temporarily until this attribute is filtered and not written to the space
            Map<String, DataUnit> expectedForSession = expected.get(buff.getProperty("SESSION_ID"));
            Assert.assertEquals("Unexpected attributes size in space", expectedForSession.keySet().size(), actual.keySet().size());
            Iterator<String> keyIterator = expectedForSession.keySet().iterator();

            while (keyIterator.hasNext()) {
                String key = keyIterator.next();

                Object expectedValue = expectedForSession.get(key).getDatavalue();
                if (!actual.containsKey(key)) {
                    Assert.fail("session does not have the key ["+key+"]");
                }
                Object actualValue= SerializeUtils.deserialize(actual.get(key).getValue());
                Assert.assertEquals("Unexpected version for attribute [" + key + "]", expectedForSession.get(key).getExpectedVersion(), actual.get(key).getVersion());
                Assert.assertEquals("Unexpected value for attribute [" + key + "]", expectedValue, actualValue);
            }
        }
    }
}
