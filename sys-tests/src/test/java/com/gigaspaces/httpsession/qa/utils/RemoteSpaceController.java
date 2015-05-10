package com.gigaspaces.httpsession.qa.utils;

import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceDeployment;
import org.openspaces.core.GigaSpace;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RemoteSpaceController extends ServerController {

	private String spaceName = "sessionSpace";
	private int instances = 2;
	private int backs = 1;

    private static boolean useExistingAgent = Boolean.valueOf(System.getProperty("useExistingAgent", "false"));
    private static boolean useExistingSpace = Boolean.valueOf(System.getProperty("useExistingSpace", "false"));

	private final static String GS_AGENT = ((File.separatorChar == '\\')) ? "gs-agent.bat"
			: "gs-agent.sh";

	private static final String SPACE_USERS = "sys-tests/src/test/resources/config/security-users";
	private Admin admin = new AdminFactory().addGroup(System.getProperty("group", System.getenv("LOOKUPGROUPS"))).createAdmin();

	private Runner starter;
	private ProcessingUnit pu;
	private GigaSpace space;

	public RemoteSpaceController() {
	}

	public RemoteSpaceController(String spaceName, int instances, int backups) {

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
        envs.put("JAVA_HOME", Config.getJava7Home());
        envs.put("LOOKUPGROUPS", System.getProperty("group", System.getenv("LOOKUPGROUPS")));

 		starter = new Runner(Config.getGSHome(), 10000, envs);

		String path = FilenameUtils.concat(Config.getGSHome(), "bin/"
				+ GS_AGENT);
		starter.getCommands().add(path);


        starter.or(new StringPredicate("GSM started successfully") {

            @Override
            public boolean customTest(String input) {
                return input.contains(match);
            }
        });

		return starter;
	}

	@Override
	public Runner createStopper() {
		return starter;
	}

	@Override
	public void start() {
       start(false);
    }

	public void start(boolean isSecuredSpace) {
		if (!useExistingAgent) {
			super.start();
		}

		if(isSecuredSpace){
			admin = new AdminFactory().addGroup(System.getProperty("group", System.getenv("LOOKUPGROUPS")))
					.credentials("user1", "user1").createAdmin();
		}else{
			admin = new AdminFactory().addGroup(System.getProperty("group", System.getenv("LOOKUPGROUPS")))
					.createAdmin();
		}

		admin.getGridServiceManagers().waitForAtLeastOne();
	}

	@Override
	public void stop() {

//		space.close();

        if (!useExistingAgent) {
            admin.getGridServiceAgents().waitForAtLeastOne();

            for (GridServiceAgent gsa : admin.getGridServiceAgents()) {
                gsa.shutdown();
            }

        }
		admin.close();

		admin = null;

		
		
//		super.stop();
	}

	@Override
	public void deploy(String appName) throws IOException {
		deploy(appName, false);
	}

	public void deploy(String appName, boolean isSecuredSpace) throws IOException {
            if (useExistingSpace) {
                admin.getGridServiceManagers().waitForAtLeastOne();
                pu = admin.getProcessingUnits().waitFor(SESSION_SPACE,30, TimeUnit.SECONDS);
                Assert.assertNotNull("Space is not deployed", pu);
                pu.waitFor(instances * (backs + 1), 30, TimeUnit.SECONDS);
                Space space1 = pu.waitForSpace(60, TimeUnit.SECONDS);
                if (space1 == null)
                    Assert.fail("Failed to find deployed space");
                space = pu.getSpace().getGigaSpace();
                pu.getSpace().getGigaSpace().clear(null);
            } else {

                SpaceDeployment sd = new SpaceDeployment(spaceName);
                sd.numberOfInstances(instances);
                sd.numberOfBackups(backs);
				if(isSecuredSpace) {
					sd.secured(true);
					sd.userDetails("user1", "user1");
					sd.setContextProperty("com.gs.security.fs.file-service.file-path", Config.getAbrolutePath(SPACE_USERS));
				}
                pu = admin.getGridServiceManagers().deploy(sd);

                pu.waitFor(instances * (backs + 1), 30, TimeUnit.SECONDS);
                Space space1 = pu.waitForSpace(60, TimeUnit.SECONDS);
                if (space1 == null)
                    Assert.fail("Failed to find deployed space");

                space = pu.getSpace().getGigaSpace();
			}
	}

    public void undeploy() throws IOException {
        undeploy("");
    }
	@Override
	public void undeploy(String appName) throws IOException {
        if (!useExistingSpace) {
            pu.undeploy();
        }
		pu.waitFor(instances * (backs + 1), 30, TimeUnit.SECONDS);
	}

	@Override
	public void saveShiroFile(String appName, List<String> lines)
			throws IOException {

	}

	public GigaSpace getSpace() {
		return space;
	}

	public Admin getAdmin(){
		return admin;
	}

}
