package com.gigaspaces.httpsession.qa.utils;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;

import com.gigaspaces.internal.client.spaceproxy.ISpaceProxy;
import com.j_spaces.core.client.FinderException;
import com.j_spaces.core.client.SpaceFinder;

public class SpaceController extends ServerController {

	private static final String SESSION_SPACE = "/./sessionSpace";
	private ISpaceProxy space;

	@Override
	public void start() {
		try {
			space = (ISpaceProxy) SpaceFinder.find(SESSION_SPACE);
		} catch (FinderException e) {
			Log.error("", e, AssertionError.class);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void stop() {
		try {
			space.clean();
		} catch (RemoteException e) {
		}
	}

	public ISpaceProxy getSpace() {
		return space;
	}

	public void setSpace(ISpaceProxy space) {
		this.space = space;
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
	}

	@Override
	public void saveShiroFile(String appName, List<String> lines)
			throws IOException {
	}

	@Override
	public void undeploy(String appName) throws IOException {
		// TODO Auto-generated method stub		
	}

}
