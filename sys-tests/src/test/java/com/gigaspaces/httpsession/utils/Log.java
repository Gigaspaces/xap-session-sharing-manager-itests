package com.gigaspaces.httpsession.utils;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log {

	protected static final Logger LOGGER = LoggerFactory.getLogger(Log.class);

	public static <T extends Error> void error(String message, Throwable e,
			Class<T> type) {
		T thr = null;
		try {
			LOGGER.debug(message, e);

			thr = type.getDeclaredConstructor(String.class, Throwable.class)
					.newInstance(message, e);
		} catch (InstantiationException e1) {
		} catch (IllegalAccessException e1) {
		} catch (IllegalArgumentException e1) {
		} catch (InvocationTargetException e1) {
		} catch (NoSuchMethodException e1) {
		} catch (SecurityException e1) {
		}

		throw thr;
	}
}
