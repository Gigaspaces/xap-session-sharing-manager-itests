package com.gigaspaces.httpsession.qa.utils;

import org.apache.commons.io.FilenameUtils;

import java.util.List;

/**
 * @author Yohana Khoury
 * @since 10.2
 */
public class JettyControllerWithoutLicense extends JettyController {
    public JettyControllerWithoutLicense(int port) {
        super(port);
    }

    @Override
    public Runner createStarter() {
        Runner starter = new Runner(Config.getJettyHome(),10000, Config.getEnvsWithJavaHome());
        starter.setWaitForTermination(false);

        String path = FilenameUtils
                .concat(Config.getJettyHome(), BIN_START_JAR);

        List<String> commands = starter.getCommands();
        commands.add(Config.getJava7Home()+"/bin/java");
        commands.add("-jar");
        commands.add(path);

        commands.add("-Dcom.gs.licensekey=");
        commands.add("--module=http");
        commands.add("-Djetty.port="+port);
        commands.add("-DSTOP.PORT=" + (port - 1));
        commands.add("-DSTOP.KEY=secret");
        commands.add("-Djetty.home=" + Config.getJettyHome());

        starter.or(new StringPredicate("com.gigaspaces.license.LicenseException: Invalid License [] - This license does not permit GigaSpaces HTTP_SESSION add-on. Please contact support for more details: http://www.gigaspaces.com/supportcenter") {

            @Override
            public boolean customTest(String input) {
                return input.contains(match);
            }
        });


        return starter;
    }
}
