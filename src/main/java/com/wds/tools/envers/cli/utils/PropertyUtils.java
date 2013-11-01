package com.wds.tools.envers.cli.utils;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import com.google.common.io.Closer;
import com.google.common.io.Resources;
import com.wds.tools.envers.cli.utils.Closers.Void;

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

	public static Properties loadProperties(final URL url) {
		final Properties props = new Properties();
		Closers.close(new Void() {
			@Override
			public void call(Closer closer) throws Throwable {
				InputStream input = Resources.newInputStreamSupplier(url).getInput();
				closer.register(input);
				props.load(input);
			}
		});
		return props;
	}
}
