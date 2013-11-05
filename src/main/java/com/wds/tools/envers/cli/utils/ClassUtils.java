package com.wds.tools.envers.cli.utils;

public class ClassUtils {
	public static <T> Class<T> forName(String className) {
		return forName(className, false);
	}

	@SuppressWarnings("unchecked")
	public static <T> Class<T> forName(String className, boolean suppressException) {
		Class<T> klass = null;
		try {
			klass = (Class<T>)Class.forName(className);
		} catch (Exception e) {
			if (!suppressException) {
				throw Exceptions.runtime(e);
			}
		}
		return klass;
	}

	public static <T> T newInstance(String className) {
		return newInstance(className, false);
	}

	public static <T> T newInstance(String className, boolean suppressException) {
		Class<T> klass = forName(className, suppressException);
		return newInstance(klass);
	}

	public static <T> T newInstance(Class<T> klass) {
		return newInstance(klass, false);
	}

	public static <T> T newInstance(Class<T> klass, boolean suppressException) {
		T instance = null;
		try {
			instance = klass.newInstance();
		} catch (Exception e) {
			if (!suppressException) {
				throw Exceptions.runtime(e);
			}
		}
		return instance;
	}
}
