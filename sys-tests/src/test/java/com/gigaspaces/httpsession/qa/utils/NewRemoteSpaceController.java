package com.gigaspaces.httpsession.qa.utils;

import com.gigaspaces.internal.client.spaceproxy.ISpaceProxy;
import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceDeployment;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class NewRemoteSpaceController extends ServerController {

	private String spaceName = "sessionSpace";
	private int instances = 2;
	private int backs = 1;

	private final static String GS_AGENT = ((File.separatorChar == '\\')) ? "gs-agent.bat"
			: "gs-agent.sh";

	private Admin admin = new AdminFactory().addGroup(Config.getLookupGroups()).createAdmin();

	private Runner starter;
	private ProcessingUnit pu;
	private ISpaceProxy space;

	public NewRemoteSpaceController() {
	}

	public NewRemoteSpaceController(String spaceName, int instances, int backups) {

		if (spaceName != null && spaceName.isEmpty())
			this.spaceName = spaceName;

		if (instances > 0)
			this.instances = instances;

		if (backups > 0)
			this.backs = backups;
	}

	@Override
	public Runner createStarter() {
        Map<String, String> envs = new HashMap<String, String>();
        envs.put("XAP_LOOKUP_GROUPS", Config.getLookupGroups());

 		starter = new Runner(Config.getGSHome(), 10000, envs);

		String path = FilenameUtils.concat(Config.getGSHome(), "bin/"
				+ GS_AGENT);
		starter.getCommands().add(path);

		return starter;
	}

	@Override
	public Runner createStopper() {

		return starter;
	}

	@Override
	public void start() {

		//super.start();
        System.out.println(admin.getGroups()[0]);
        GridServiceManager gsm = admin.getGridServiceManagers().waitForAtLeastOne();
        pu = gsm.deploy(new SpaceDeployment(spaceName).clusterSchema("partitioned-sync2backup").numberOfInstances(1).numberOfBackups(0), 20, TimeUnit.SECONDS);
        Assert.assertNotNull("Failed to deploy space", pu);
        //pu = admin.getProcessingUnits().waitFor(spaceName,30, TimeUnit.SECONDS);

        //pu.waitFor(instances * (backs + 1), 30, TimeUnit.SECONDS);
        Assert.assertTrue("Failed to find space [" + spaceName + "]", pu.waitFor(1, 20, TimeUnit.SECONDS));
        Space space1 = pu.waitForSpace(60, TimeUnit.SECONDS);
        if(space1 == null)
            Assert.fail("Failed to find deployed space");

        space = (ISpaceProxy) pu.getSpace().getGigaSpace().getSpace();
        /*try {
            space.clear(null, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (TransactionException e) {
            e.printStackTrace();
        } catch (UnusableEntryException e) {
            e.printStackTrace();
        }*/
    }

	@Override
	public void stop() {

		//space.close();

		admin.getGridServiceAgents().waitForAtLeastOne();

        GridServiceManager gsm = admin.getGridServiceManagers().waitForAtLeastOne();
        gsm.undeploy(spaceName);

	/*	for (GridServiceAgent gsa : admin.getGridServiceAgents()) {
			gsa.shutdown();
		}*/

		admin.close();

		admin = null;

		
		
//		super.stop();
	}

	@Override
	public void deploy(String appName) throws IOException {

		SpaceDeployment sd = new SpaceDeployment(spaceName);
		sd.numberOfInstances(instances);
		sd.numberOfBackups(backs);

		pu = admin.getGridServiceManagers().deploy(sd);

		pu.waitFor(instances * (backs + 1), 30, TimeUnit.SECONDS);
        Space space1 = pu.waitForSpace(60, TimeUnit.SECONDS);
        if(space1 == null)
            Assert.fail("Failed to find deployed space");

		space = (ISpaceProxy) pu.getSpace().getGigaSpace().getSpace();
	}

	@Override
	public void undeploy(String appName) throws IOException {

		pu.undeploy();

		pu.waitFor(instances * (backs + 1), 30, TimeUnit.SECONDS);
	}

	@Override
	public void saveShiroFile(String appName, List<String> lines)
			throws IOException {

	}

	public ISpaceProxy getSpace() {
		return space;
	}

	public Admin getAdmin(){
		return admin;
	}

}
