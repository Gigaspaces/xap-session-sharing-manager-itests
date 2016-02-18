package com.gigaspaces.httpsession.qa.utils;

import com.gigaspaces.internal.dump.log.LogDumpProcessor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.dump.DumpResult;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceDeployment;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.space.UrlSpaceConfigurer;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
	private Admin admin;// = new AdminFactory().addGroup(Config.getLookupGroups()).createAdmin();

	private Runner starter;
	private ProcessingUnit pu;
	private GigaSpace space;
    private UrlSpaceConfigurer urlSpaceConfigurer;
    private Space space1;

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
        envs.put("LOOKUPGROUPS", Config.getLookupGroups());
        envs.put("EXT_JAVA_OPTIONS", "-Xmx200m -Xms200m");

        System.out.println("Will start with Groups = "+Config.getLookupGroups());
        starter = new Runner(Config.getGSHome(), 10000, envs);
        starter.setWaitForTermination(false);
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
			admin = new AdminFactory().addGroup(Config.getLookupGroups())
                    .useDaemonThreads(true)
                    .credentials("user1", "user1").createAdmin();
        }else{
            System.out.println("Creating admin...");
            admin = new AdminFactory().addGroup(Config.getLookupGroups())
                    .useDaemonThreads(true)
                    .createAdmin();
		}

        System.out.println("Waiting for one GSM");
        GridServiceManager gsm = admin.getGridServiceManagers().waitForAtLeastOne(20, TimeUnit.SECONDS);
        Assert.assertNotNull("Failed to find GSM", gsm);
	}

	@Override
	public void stop() {

//		space.close();

        //((ISpaceProxy) space.getSpace()).close();
        if (!useExistingAgent && admin != null) {
            GridServiceAgent oneGSA = admin.getGridServiceAgents().waitForAtLeastOne(10, TimeUnit.SECONDS);

            if (oneGSA != null) {
                LOGGER.info("Shutting down GSAs:");
                for (GridServiceAgent gsa : admin.getGridServiceAgents()) {
                    LOGGER.info("Shutting down GSA: " + gsa.getUid());
                    gsa.shutdown();
                }
            } else {
                LOGGER.error("Failed to find at least one GSA on stop");
            }
        }
        if (admin != null) {
            admin.close();
            admin = null;
        }

        //space = null;

//		super.stop();
	}

    @Override
    public void dumpLogsToDir(File dir) {
        //System.getProperty("newman.test.path")
        if (admin == null){
            LOGGER.error("Cannot dump logs, admin is null!");
            return;
        }
        DumpResult result = admin.generateDump(null, null, LogDumpProcessor.NAME);
        Date date = new Date();
        DateFormat date1 = new SimpleDateFormat("dd-MM-yyyy");
        DateFormat hour = new SimpleDateFormat("HH-mm-ss.SSS");

        File zipFile = new File(dir.getAbsolutePath() + "/" + date1.format(date) + "_" + hour.format(date) + "_" + "_dump.zip");

        result.download(zipFile, null);
        if (!zipFile.exists()) {
            LOGGER.error(zipFile.getPath() + " cannot be found. Probably dump download failed.");
        }

        File testFolder = zipFile.getParentFile();
        ZipUtils.unzipArchive(zipFile, testFolder);
        try {
            copyAllFilesToLogDir(testFolder, testFolder);
        } catch (IOException e) {
            LOGGER.error("Failed to copy all log files - caught " + e, e);
        }

        try {
        zipFile.delete();
        } catch (Throwable e) {
            LOGGER.error("Failed to delete zip file ["+zipFile.getAbsolutePath()+"]", e);
        }
    }

    @Override
	public void deploy(String appName) throws IOException {
		deploy(appName, false);
	}

    public void deploy(String appName, boolean isSecuredSpace) throws IOException {
        if (useExistingSpace) {
            //admin.getGridServiceManagers().waitForAtLeastOne();
            pu = admin.getProcessingUnits().waitFor(SESSION_SPACE, 30, TimeUnit.SECONDS);
            Assert.assertNotNull("Space is not deployed", pu);
            pu.waitFor(instances * (backs + 1), 30, TimeUnit.SECONDS);
            Space space1 = pu.waitForSpace(60, TimeUnit.SECONDS);
            if (space1 == null)
                Assert.fail("Failed to find deployed space");
            space = space1.getGigaSpace();
            space.clear(null);
        } else {

            SpaceDeployment sd = new SpaceDeployment(spaceName);
            sd.numberOfInstances(instances);
            sd.numberOfBackups(backs);
            if (isSecuredSpace) {
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
            if (pu != null) {
                pu.undeployAndWait();
            }
        }
        //pu.waitFor(instances * (backs + 1), 30, TimeUnit.SECONDS);
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

    private void copyAllFilesToLogDir(File node, File parent) throws IOException {
        if (!node.getAbsoluteFile().equals(parent.getAbsoluteFile())
                && node.isFile()
                && !node.getParentFile().equals(parent)) {
            String fileNamePrefix = node.getName().substring(0, node.getName().lastIndexOf('.'));
            String fileNameSuffix = node.getName().substring(node.getName().lastIndexOf('.'));
            String newFilePath = node.getParentFile().getAbsolutePath() + File.separator + fileNamePrefix.replace(".", "_") + fileNameSuffix;

            File newNode = new File(newFilePath);
            if (node.renameTo(newNode)) {
                FileUtils.copyFileToDirectory(newNode, parent);
            }
        }
        if (node.isDirectory()) {
            String[] subNote = node.list();
            for (String filename : subNote) {
                copyAllFilesToLogDir(new File(node, filename), parent);
            }
            if (!node.equals(parent)) {
                FileUtils.deleteDirectory(node);
            }
        }

    }
}
