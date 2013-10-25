package com.wds.tools.envers.cli.utils;

public class Console {
	public static void info(Object message, Object... args) {
		System.out.println(StringUtils.replace((String) message, args));
	}

	public static void fail(Object message, Object... args) {
		System.err.println(StringUtils.replace((String) message, args));
		System.exit(Byte.MAX_VALUE);
	}
}