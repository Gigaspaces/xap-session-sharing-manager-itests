package com.gigaspaces.httpsession.qa.newtests;

import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.httpsession.models.AttributeData;
import com.gigaspaces.httpsession.qa.DataUnit;
import com.gigaspaces.httpsession.qa.newtests.StoreModeBase;
import com.gigaspaces.httpsession.qa.utils.HTTPUtils;
import com.gigaspaces.httpsession.serialize.SerializeUtils;
import com.gigaspaces.httpsession.sessions.DeltaStoreMode;
import com.gigaspaces.httpsession.sessions.StoreMode;
import com.gigaspaces.internal.client.spaceproxy.ISpaceProxy;
import org.junit.Assert;
import org.openspaces.core.GigaSpace;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
    public void assertSpaceMode(ISpaceProxy space, int expectedSessions, HashMap<String, Map<String, DataUnit>> expected, String type, int expectedVersion) {
        SpaceDocument[] data = readSpaceData(space, type);

        Assert.assertEquals("invalid length result:", expectedSessions, data.length);

        for (SpaceDocument buff : data) {

            Map<String, AttributeData> actual =  buff.getProperty(StoreMode.PROPERTY_ATTRIBUTES);

            Map<String, DataUnit> expectedForSession = expected.get(buff.getProperty("SESSION_ID"));
            Iterator<String> keyIterator = expectedForSession.keySet().iterator();

            while (keyIterator.hasNext()) {
                String key = keyIterator.next();

                Object expectedValue = expectedForSession.get(key).getDatavalue();
                if (!actual.containsKey(key)) {
                    Assert.fail("session does not have the key ["+key+"]");
                }
                Object actualValue= SerializeUtils.deserialize(actual.get(key).getValue());
                Assert.assertEquals("Unexpected attribute ["+key+"] version", expectedVersion, actual.get(key).getVersion());
                Assert.assertEquals("Unexpected attribute ["+key+"] value", expectedValue, actualValue);
            }
        }
    }
}
