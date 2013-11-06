package com.wds.tools.envers.cli.support.command;

import io.airlift.command.Command;

import com.wds.tools.envers.cli.support.CommandLineContext;
import com.wds.tools.envers.cli.utils.Logger;

@Command(name = "version", description = "Show version")
public class VersionCommand implements Runnable {
	@Override
	public void run() {
		Logger.info(CommandLineContext.current().getVersion());
	}
}
