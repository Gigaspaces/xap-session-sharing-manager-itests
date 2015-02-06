package com.gigaspaces.httpsession.qa.utils;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import junit.framework.Assert;
import org.openspaces.admin.Admin;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;

/**
 * Created by kobi on 2/6/15.
 */
public class AdminUtils {

    public static void restartPrimariesGSCs(Admin admin, String puName){
        ProcessingUnit pu = admin.getProcessingUnits().getProcessingUnit(puName);
        ProcessingUnitInstance instance = null;
        for (ProcessingUnitInstance processingUnitInstance : pu.getInstances()) {
            if (processingUnitInstance.getSpaceInstance().getMode().equals(SpaceMode.PRIMARY)) {
                instance.restart();
            }
        }
    }

    public static void waitForPrimaries(Admin admin, String puName, int numberOfPrimaries){
        ProcessingUnit pu = admin.getProcessingUnits().getProcessingUnit(puName);
        int counter = 0;
        for (ProcessingUnitInstance processingUnitInstance : pu.getInstances()) {
            if (processingUnitInstance.getSpaceInstance().getMode().equals(SpaceMode.PRIMARY)) {
                counter++;
            }
        }
        Assert.assertEquals("wrong number of primaries", numberOfPrimaries, counter);
    }
}
