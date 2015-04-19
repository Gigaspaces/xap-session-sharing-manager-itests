package com.gigaspaces.httpsession.qa.newtests.bases;

import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.httpsession.qa.DataUnit;
import com.gigaspaces.internal.client.spaceproxy.ISpaceProxy;
import com.j_spaces.core.client.SQLQuery;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Yohana Khoury
 * @since 10.1
 */
public abstract class StoreModeBase {
    public abstract String getStoreModeClassName();
    public abstract void assertSpaceMode(ISpaceProxy space, int expectedSessions, HashMap<String, Map<String, DataUnit>> expected, String type);

    protected SpaceDocument[]  readSpaceData(ISpaceProxy space, String type) {
        SQLQuery<SpaceDocument> sqlQuery = new SQLQuery<SpaceDocument>(type, "");
        SpaceDocument[] data;
        try {
            data = (SpaceDocument[]) space.readMultiple(sqlQuery, null, Integer.MAX_VALUE);
        } catch (Throwable e) {
            throw new Error(e);
        }

        return data;
    }
}

