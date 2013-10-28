package com.wds.tools.envers.cli.utils;

public class Exceptions {
	public static RuntimeException runtime(String message, Object... args) {
		return new RuntimeException(StringUtils.replace(message, args));
	}

	public static RuntimeException runtime(Exception exception) {
		if (exception instanceof RuntimeException) {
			return (RuntimeException) exception;
		} else {
			return new RuntimeException(exception);
		}
	}

	public static IllegalArgumentException illegalArgument(String message, Object... args) {
		return new IllegalArgumentException(StringUtils.replace(message, args));
	}
}
