package com.wds.tools.envers.cli.support.command;

import static com.wds.tools.envers.cli.utils.ValidateUtils.shouldNotNull;
import io.airlift.command.Command;
import io.airlift.command.Option;

import com.wds.tools.envers.cli.support.AbstractCommand;

@Command(name = "install", description = "Track changes of target database using Hibenrate Envers")
public class InstallCommand extends AbstractCommand {

	@Option(name = "--username", description = "username of target database")
	public String username;

	@Option(name = "--password", description = "password of target database")
	public String password;

	@Option(name = "--driver", description = "driver class of target database")
	public String driver;

	@Option(name = "--basepackage", description = "base package to scan entities")
	public String basepackage;

	@Option(name = "--revent", description = "revision entity class")
	public String revent;

	@Override
	public void run() {
		validate();
		getExecutor().install();
	}

	private void validate() {
		shouldNotNull(this.revent, "RevisionEntity class should not be null : ''--revent'' is required");
	}
}
