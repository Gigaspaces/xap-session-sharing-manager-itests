package com.gigaspaces.httpsession.qa.utils;

public class JmeterTask extends Runner {

	private static final String APP_NAME = "app_name";
	private static final String JMETER_SCRIPT_COMPLETE = "... end of run";
	private static final String PORT = "port";
	private static final String HOST="host";
	
	public JmeterTask() {
		super(Config.getJmeterHome()+"/bin", null);
        builder = new ProcessBuilder(commands);
		commands.add(Config.getJmeterHome() + "/bin/jmeter");
		commands.add("-n");
		or(new StringPredicate(JMETER_SCRIPT_COMPLETE) {

			@Override
			public boolean customTest(String input) {
				return input.endsWith(match);
			}
		});
	}

    public void setScript(String script){
        commands.add("-t");
        commands.add(script);
    }

	public void setHost(String host){
		addParam(HOST, host);
	}
	
	public void setPort(String port){
		addParam(PORT, port);
	}
	
	public void setAppName(String appName){
		addParam(APP_NAME, appName);
	}
	
	public void addParam(String name, String value) {
		commands.add(String.format("-J%s=%s", name, value));
	}
}
