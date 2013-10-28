package com.wds.tools.envers.cli.utils;

import java.util.Map;

public class StringUtils {
	// StringUtils.replace("Hello {0}!", "Edison") = "Hello Edison!";
	public static String replace(String pattern, Object... args) {
		if (args == null || args.length == 0) {
			return pattern;
		}

		String result = pattern;
		for (int i = 0; i < args.length; i++) {
			String key = String.valueOf(i);
			String value = String.valueOf(args[i]);
			result = result.replaceAll("\\{" + key + "?\\}", value);
		}
		
		return result;
	}

	// Map<String,Object> props = ...;
	// props.put("name","Edison");
	// StringUtils.replace("hello ${name}!",props) = "Hello Edison!";
	public static String replace(String pattern, Map<String, Object> map) {
		if (map == null || map.size() == 0) {
			return pattern;
		}

		String result = pattern;

		for (String key : map.keySet()) {
			String value = String.valueOf(map.get(key));
			result = result.replaceAll("\\$\\{" + key + "?\\}", value);
		}

		return result;
	}

	public static StringConcator concat(String string) {
		return new StringConcatorImpl(string);
	}

	public static String trimLeft(String string) {
		if (string == null || string == "") {
			return string;
		}
		return string.replaceAll("^\\s+", "");
	}

	public static String trimRight(String string) {
		if (string == null || string == "") {
			return string;
		}
		return string.replaceAll("\\s+$", "");
	}

	public static interface StringConcator {
		StringConcator concat(String string);

		String toString();
	}

	private static class StringConcatorImpl implements StringConcator {
		public StringConcatorImpl(String string) {
			this.builder = new StringBuilder();
			if (string != null) {
				builder.append(string);
			}
		}

		private final StringBuilder builder;

		@Override
		public StringConcator concat(String string) {
			this.builder.append(string);
			return this;
		}

		@Override
		public String toString() {
			return this.builder.toString();
		}
	}
}