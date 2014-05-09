package com.gigaspaces.httpsession.utils;

public abstract class StringPredicate implements Function<String> {

	protected String match;

	public StringPredicate(String match) {
		this.match = match;
	}

	public final boolean test(String input) {
		return (input != null && !input.isEmpty() && customTest(input));
	}

	public abstract boolean customTest(String input);

}
