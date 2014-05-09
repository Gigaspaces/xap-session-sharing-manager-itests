package com.gigaspaces.httpsession.qa.utils;

public class TimeoutPredicate implements Function<String> {

	private Long timeout;

	public TimeoutPredicate(Long timeout) {
		this.timeout = System.currentTimeMillis() + timeout;
	}

	public boolean test(String input) {
		return System.currentTimeMillis() > timeout;
	}
}
