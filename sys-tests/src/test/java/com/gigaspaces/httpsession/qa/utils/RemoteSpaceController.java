package com.gigaspaces.httpsession.qa.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.space.SpaceDeployment;

import com.gigaspaces.internal.client.spaceproxy.ISpaceProxy;

public class RemoteSpaceController extends ServerController {

	private String spaceName = "sessionSpace";
	private int instances = 2;
	private int backs = 1;
	// private static final String GS_HOME = "GS_HOME";
	// private static final String QA_GROUP = "qa_group";
	// private static final String LOOKUPGROUPS = "LOOKUPGROUPS";

	private final static String GS_AGENT = ((File.separatorChar == '\\')) ? "gs-agent.bat"
			: "gs-agent.sh";

	private Admin admin = new AdminFactory().createAdmin();

	private Runner starter;
	private ProcessingUnit pu;
	private ISpaceProxy space;

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
		starter = new Runner(Config.getGSHome(), 10000);

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

		super.start();

		admin.getGridServiceManagers().waitForAtLeastOne();
	}

	@Override
	public void stop() {

		for (GridServiceAgent gsa : admin.getGridServiceAgents()) {
			gsa.shutdown();
		}

		admin.close();

		admin = null;

		super.stop();
	}

	@Override
	public void deploy(String appName) throws IOException {

		SpaceDeployment sd = new SpaceDeployment(spaceName);
		sd.numberOfInstances(instances);
		sd.numberOfBackups(backs);

		pu = admin.getGridServiceManagers().deploy(sd);

		pu.waitFor(instances * (backs + 1), 30, TimeUnit.SECONDS);

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

}
