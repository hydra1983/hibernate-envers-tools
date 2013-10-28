package com.wds.tools.envers.cli.utils;

public class ValidateUtils {
	public static Object shouldNotNull(Object value, String message, Object... args) {
		if (value == null) {
			throw Exceptions.runtime(message, args);
		}
		return value;
	}
}
