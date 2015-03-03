//package com.gigaspaces.httpsession.qa;
//
//import com.gigaspaces.httpsession.policies.FailFastChangeStrategy;
//import com.gigaspaces.httpsession.sessions.DeltaStoreMode;
//import org.junit.Test;
//
//import java.util.Map;
//
//public class DeltaModeFastFailTestWithoutLoginTomcatSecurity extends TestWithoutLoginBase {
//
//	@Test
//	public void testTomcatWithSecurity() {
//
//		runSecuredTomcatTest();
//
//		assertSpaceDeltaMode(USERS_VALUE, true);
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
