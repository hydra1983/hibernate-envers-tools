package com.wds.tools.envers.cli.utils;

public class ClassUtils {
	public static Class<?> forName(String className) {
		return forName(className, false);
	}

	public static Class<?> forName(String className, boolean suppressException) {
		Class<?> klass = null;
		try {
			klass = Class.forName(className);
		} catch (Exception e) {
			if (!suppressException) {
				throw Exceptions.runtime(e);
			}
		}
		return klass;
	}
}
