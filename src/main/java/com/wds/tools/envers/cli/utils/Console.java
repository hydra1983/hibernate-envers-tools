package com.wds.tools.envers.cli.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Console {
	private static final String NAME = "envers";
	private static final String INFO = "INFO {0} " + NAME + ": ";
	private static final String ERROR = "ERROR {0} " + NAME + ": ";
	private static final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

	public static void info(Object message, Object... args) {
		System.out.println(StringUtils.replace(INFO, currentDate()) + StringUtils.replace((String) message, args));
	}

	public static void fail(Object message, Object... args) {
		System.err.println(StringUtils.replace(ERROR, currentDate()) + StringUtils.replace((String) message, args));
		System.exit(Byte.MAX_VALUE);
	}

	private static String currentDate() {
		return formatter.format(new Date());
	}
}