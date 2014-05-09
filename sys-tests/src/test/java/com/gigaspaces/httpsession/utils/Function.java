package com.gigaspaces.httpsession.utils;

public interface Function<T> {
	boolean test(T input);
}
