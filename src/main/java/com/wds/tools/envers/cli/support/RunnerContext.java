package com.wds.tools.envers.cli.support;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import com.google.common.io.Closeables;
import com.google.common.io.Resources;

public class RunnerContext {
	private static final ThreadLocal<RunnerContext> current = new ThreadLocal<RunnerContext>();

	public static void set() {
		current.set(new RunnerContext());
	}

	public static void unset() {
		current.set(null);
	}

	public static RunnerContext current() {
		return current.get();
	}

	private RunnerContext() {
		initialize();
	}

	private String commandLineName;
	private String version;

	public String getCommandLineName() {
		return this.commandLineName;
	}

	public String getVersion() {
		return this.commandLineName + " v" + this.version;
	}

	private void initialize() {
		Properties buildInfo = new Properties();

		try {
			URL props = Resources.getResource("buildinfo.properties");
			InputStream propsStream = Resources.newInputStreamSupplier(props).getInput();
			buildInfo.load(propsStream);
			Closeables.closeQuietly(propsStream);
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.commandLineName = getProperty(buildInfo, "commandline", "envers");
		this.version = getProperty(buildInfo, "version", "UNKNOWN");
	}

	private String getProperty(Properties props, String key, String defaultValue) {
		if (props.containsKey(key)) {
			return props.getProperty(key);
		}
		return defaultValue;
	}
}
