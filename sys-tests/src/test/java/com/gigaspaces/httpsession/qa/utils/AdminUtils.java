package com.gigaspaces.httpsession.qa.utils;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import junit.framework.Assert;
import org.openspaces.admin.Admin;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;

import java.util.concurrent.TimeUnit;

/**
 * Created by kobi on 2/6/15.
 */
public class AdminUtils {

    public static void restartPrimariesGSCs(Admin admin, String puName){
        ProcessingUnit pu = admin.getProcessingUnits().getProcessingUnit(puName);
        for (ProcessingUnitInstance processingUnitInstance : pu.getInstances()) {
            if (processingUnitInstance.getSpaceInstance().getMode().equals(SpaceMode.PRIMARY)) {
                processingUnitInstance.getGridServiceContainer().restart();
            }
        }
    }

    public static void restartPrimaries(Admin admin, String puName){
        ProcessingUnit pu = admin.getProcessingUnits().getProcessingUnit(puName);
        for (ProcessingUnitInstance processingUnitInstance : pu.getInstances()) {
            if (processingUnitInstance.getSpaceInstance().getMode().equals(SpaceMode.PRIMARY)) {
                processingUnitInstance.restart();
            }
        }
    }

    public static void waitForPrimaries(Admin admin, String puName, int numberOfPrimaries){
        boolean allMembersAlive = admin.getProcessingUnits().getProcessingUnit(puName).waitFor(
                admin.getProcessingUnits().getProcessingUnit(puName).getPlannedNumberOfInstances(), 30, TimeUnit.SECONDS);
        if(!allMembersAlive)
            Assert.fail("Not all pu instances alive");

        ProcessingUnit pu = admin.getProcessingUnits().getProcessingUnit(puName);
        boolean arePrimaries = pu.getSpace().waitFor(numberOfPrimaries, SpaceMode.PRIMARY, 60, TimeUnit.SECONDS);
        if(!arePrimaries)
            Assert.fail("wrong number of primaries");

    }

    public static void waitForBackups(Admin admin, String puName, int numberOfBackups){
        boolean allMembersAlive = admin.getProcessingUnits().getProcessingUnit(puName).waitFor(
                admin.getProcessingUnits().getProcessingUnit(puName).getPlannedNumberOfInstances(), 30, TimeUnit.SECONDS);
        if(!allMembersAlive)
            Assert.fail("Not all pu instances alive");

        ProcessingUnit pu = admin.getProcessingUnits().getProcessingUnit(puName);
        boolean areBackups = pu.getSpace().waitFor(numberOfBackups, SpaceMode.BACKUP, 60, TimeUnit.SECONDS);
        if(!areBackups)
            Assert.fail("wrong number of backups");

    }
}
