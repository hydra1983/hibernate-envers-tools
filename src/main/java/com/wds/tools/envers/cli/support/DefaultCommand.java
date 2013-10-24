package com.wds.tools.envers.cli.support;

import io.airlift.command.Help;
import io.airlift.command.Option;
import io.airlift.command.OptionType;

public class DefaultCommand extends Help {
	@Option(type = OptionType.GLOBAL, name = { "--version" }, description = "Show version")
	public boolean version;

	@Override
	public void run() {
		if (version) {
			System.out.println(RunnerContext.current().getVersion());
		} else {
			super.run();
		}
	}
}