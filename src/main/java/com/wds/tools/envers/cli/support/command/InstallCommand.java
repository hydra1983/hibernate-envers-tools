package com.wds.tools.envers.cli.support.command;

import io.airlift.command.Command;
import io.airlift.command.Option;

import com.wds.tools.envers.cli.support.AbstractCommand;

@Command(name = "install", description = "Track changes of target database using Hibenrate Envers")
public class InstallCommand extends AbstractCommand {
	@Option(name = "--url", description = "url of target database", required = true)
	public String url;

	@Option(name = "--username", description = "username of target database")
	public String username;

	@Option(name = "--password", description = "password of target database")
	public String password;

	@Option(name = "--basepackage", description = "base package to scan entities")
	public String basepackage;

	@Override
	public void run() {
		System.out.println(parameters);
	}
}
