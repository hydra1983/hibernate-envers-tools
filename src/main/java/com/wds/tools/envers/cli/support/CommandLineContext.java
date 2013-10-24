package com.wds.tools.envers.cli.support;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import com.google.common.io.Closeables;
import com.google.common.io.Resources;

public class CommandLineContext {
	private static final ThreadLocal<CommandLineContext> current = new ThreadLocal<CommandLineContext>();

	public static void begin() {
		current.set(new CommandLineContext());
	}

	public static void end() {
		current.set(null);
	}

	public static CommandLineContext current() {
		return current.get();
	}

	private CommandLineContext() {
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
