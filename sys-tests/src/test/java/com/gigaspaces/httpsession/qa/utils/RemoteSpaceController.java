package com.gigaspaces.httpsession.qa.utils;

import java.io.IOException;
import java.util.List;

public class RemoteSpaceController extends ServerController {

	private String spaceName = "sessionSpace";
	private int instances = 2;
	private int backs = 1;

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Runner createStopper() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deploy(String appName) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void undeploy(String appName) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveShiroFile(String appName, List<String> lines)
			throws IOException {
		// TODO Auto-generated method stub

	}

}
