package com.wds.tools.envers.cli.support;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import com.google.common.io.Closer;
import com.google.common.io.Resources;
import com.wds.tools.envers.cli.utils.Closers;
import com.wds.tools.envers.cli.utils.Closers.Void;
import com.wds.tools.envers.cli.utils.PropertyUtils;

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
		return this.commandLineName + " " + this.version;
	}

	private void initialize() {
		Properties buildInfo = loadBuildinfo();
		this.commandLineName = PropertyUtils.getProperty(buildInfo, "commandline", "envers");
		this.version = PropertyUtils.getProperty(buildInfo, "version", "UNKNOWN");
	}

	private Properties loadBuildinfo() {
		final Properties buildInfo = new Properties();
		final URL propsUrl = Resources.getResource("buildinfo.properties");

		Closers.close(new Void() {
			@Override
			public void call(Closer closer) throws Throwable {
				InputStream propsStream = Resources.newInputStreamSupplier(propsUrl).getInput();
				closer.register(propsStream);
				buildInfo.load(propsStream);
			}
		});

		return buildInfo;
	}
}
