package com.gigaspaces.httpsession.qa;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ FullModeWithoutLogin.class	, FullModeTestWithLogin.class,
//		DeltaModeLWWTestWithLogin.class, DeltaModeFastFailTestWithLogin.class,
//		DeltaModePartialTestWithLogin.class,
//		DeltaModeLWWTestWithoutLogin.class,
//		DeltaModeFastFailTestWithoutLogin.class,
//		DeltaModePartialTestWithoutLogin.class, DeltaModeFastFailTestWithoutLoginTomcatSecurity.class,
		FullModeWithoutLoginTomcatSecurity.class
		})
public class SystemTestSuite {

} 
