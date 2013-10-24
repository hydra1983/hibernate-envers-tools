package com.wds.tools.envers.cli.support.command;

import io.airlift.command.Command;

import com.wds.tools.envers.cli.support.AbstractCommand;

@Command(name = "version", description = "Show version")
public class VersionCommand extends AbstractCommand {
	@Override
	public void run() {
		System.out.println(getContext().getVersion());
	}
}
