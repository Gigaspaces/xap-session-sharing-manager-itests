package com.gigaspaces.httpsession.qa.utils;

import org.apache.commons.io.FilenameUtils;

import java.util.List;

public class JettyHTTPSController extends JettyController {

    public JettyHTTPSController(int port, String appName) {
        super(port, appName);
    }

    public JettyHTTPSController(int port) {
        super(port);
    }

    @Override
	public Runner createStarter() {
		Runner starter = new Runner(Config.getJettyHome(),10000, Config.getEnvsWithJavaHome());

		String path = FilenameUtils
				.concat(Config.getJettyHome(), BIN_START_JAR);

        starter.setWaitForTermination(false);
		List<String> commands = starter.getCommands();
        commands.add(Config.getJava7Home()+"/bin/java");
		commands.add("-jar");
		commands.add(path);

        commands.add("--module=https");
        commands.add("-Dhttps.port="+port);
		commands.add("-DSTOP.PORT=" + (port - 1));
		commands.add("-DSTOP.KEY=secret");
		commands.add("-Djetty.home=" + Config.getJettyHome());

		starter.or(new StringPredicate("oejs.Server:main: Started") {

			@Override
			public boolean customTest(String input) {
				return input.contains(match);
			}
		});
        /*starter.or(new StringPredicate("demo-app/,AVAILABLE}{/demo-app}") {

            @Override
            public boolean customTest(String input) {
                return input.contains(match);
            }
        });
*/



		return starter;
	}

}
