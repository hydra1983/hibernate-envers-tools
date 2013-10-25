package com.wds.tools.envers.cli.utils;

import java.util.Properties;

public class PropertyUtils {
	public static String getProperty(Properties props, String key, String defaultValue) {
		if (props.containsKey(key)) {
			return props.getProperty(key);
		}
		return defaultValue;
	}

	public static void putProperty(Properties props, String key, String defaultValue) {
		if ((!props.containsKey(key) || props.getProperty(key) == null) && defaultValue != null) {
			props.put(key, defaultValue);
		}
	}
}
