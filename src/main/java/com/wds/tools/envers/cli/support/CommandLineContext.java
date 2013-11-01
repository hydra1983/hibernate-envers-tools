package com.wds.tools.envers.cli.support;

import java.util.Properties;

import com.google.common.io.Resources;
import com.wds.tools.envers.cli.utils.PropertyUtils;

public class CommandLineContext {
	private static Properties buildinfo;
	{
		buildinfo = PropertyUtils.loadProperties(Resources.getResource("buildinfo.properties"));
	}

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
		return this.commandLineName + " " + this.version;
	}

	private void initialize() {
		this.commandLineName = PropertyUtils.getProperty(buildinfo, "commandline", "envers");
		this.version = PropertyUtils.getProperty(buildinfo, "version", "UNKNOWN");
	}
}
