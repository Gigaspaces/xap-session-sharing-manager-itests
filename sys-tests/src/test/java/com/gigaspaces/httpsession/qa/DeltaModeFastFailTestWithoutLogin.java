//package com.gigaspaces.httpsession.qa;
//
//import com.gigaspaces.httpsession.policies.FailFastChangeStrategy;
//import com.gigaspaces.httpsession.sessions.DeltaStoreMode;
//import org.junit.Test;
//
//import java.util.Map;
//
//public class DeltaModeFastFailTestWithoutLogin extends TestWithoutLoginBase {
//
//	@Test
//	public void testJboss() {
//
//		runJbossTest();
//
//		assertSpaceDeltaMode(USERS_VALUE);
//	}
//
//	@Test
//	public void testTomcat() {
//
//		runTomcatTest();
//
//		assertSpaceDeltaMode(USERS_VALUE);
//	}
//
//	@Test
//	public void testJetty() {
//
//		runJettyTest();
//
//		assertSpaceDeltaMode(USERS_VALUE);
//	}
//
//	@Override
//	protected String getDataFileName() {
//		return "data.delta.fast.withoutlogin.csv";
//	}
//
//	@Override
//	public Map<String, String> getConfiguration() {
//
//		Map<String, String> properties = super.getConfiguration();
//
//		properties.put("main/storeMode", DeltaStoreMode.class.getName());
//		properties.put("main/storeMode.changeStrategy",
//				FailFastChangeStrategy.class.getName());
//
//		return properties;
//	}
//
//}
