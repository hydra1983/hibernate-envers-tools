package com.wds.tools.envers.cli.support.command;

import io.airlift.command.Command;
import io.airlift.command.Option;

import com.wds.tools.envers.cli.support.AbstractCommand;

@Command(name = "install", description = "Enable change tracking for target database using Hibenrate Envers")
public class InstallCommand extends AbstractCommand {

	@Option(name = "--username", description = "Username of target database")
	public String username;

	@Option(name = "--password", description = "Password of target database")
	public String password;

	@Option(name = "--driver", description = "Driver class of target database")
	public String driver;

	@Option(name = "--dialect", description = "Dialect class of target database")
	public String dialect;

	@Option(name = "--basepackages", description = "Package names which will be converted to regexp pattern to scan entities")
	public String basepackages;

	@Option(name = "--revent", description = "Revision entity class")
	public String revent;

	@Override
	protected void doRun() {
		getExecutor().install();
	}
}
