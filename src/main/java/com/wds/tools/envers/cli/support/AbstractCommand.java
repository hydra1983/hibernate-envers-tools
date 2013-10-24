package com.wds.tools.envers.cli.support;

import io.airlift.command.Option;
import io.airlift.command.OptionType;

import java.util.List;

public abstract class AbstractCommand implements Runnable {
	@Option(type = OptionType.GLOBAL, name = { "-v", "--verbose" }, description = "Verbose mode")
	public boolean verbose;

	@Option(name = { "-D" }, description = "System parameters")
	public List<String> parameters;

	@Override
	public void run() {
	}

	protected CommandLineContext getContext() {
		return CommandLineContext.current();
	}
}