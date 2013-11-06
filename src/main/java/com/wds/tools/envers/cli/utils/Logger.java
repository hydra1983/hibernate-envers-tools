package com.wds.tools.envers.cli.utils;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
	private static final String CATEGORY = "envers";
	private static final String PADDING = "    ";
	private static final String INFO = "INFO {0} " + CATEGORY + ": ";
	private static final String ERROR = "ERROR {0} " + CATEGORY + ": ";
	private static final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	private static final PrintStream err = System.err;

	public static void info(String message, Object... args) {
		info(0, message, args);
	}

	public static void info(int level, String message, Object... args) {
		print(level, INFO, message, args);
	}

	public static void error(String message, Object... args) {
		error(0, message, args);
	}

	public static void error(int level, String message, Object... args) {
		print(level, ERROR, message, args);
	}

	private static String currentDate() {
		return formatter.format(new Date());
	}

	private static String getPadding(int level) {
		String padding = "";
		for (int i = 0; i < level; i++) {
			padding += PADDING;
		}
		return padding;
	}

	private static void print(int level, String prefix, String message, Object... args) {
		err.println(getPadding(level) + StringUtils.replace(prefix, currentDate())
				+ StringUtils.replace((String) message, args));
	}
}