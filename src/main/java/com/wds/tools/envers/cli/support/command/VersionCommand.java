package com.wds.tools.envers.cli.support.command;

import io.airlift.command.Command;

import com.wds.tools.envers.cli.support.RunnerContext;

@Command(name = "version", description = "Show version")
public class VersionCommand implements Runnable {
	@Override
	public void run() {
		System.out.println(RunnerContext.current().getVersion());
	}
}
