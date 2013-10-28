package com.wds.tools.envers.cli.support.command;

import static com.wds.tools.envers.cli.utils.ValidateUtils.shouldNotNull;
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

	@Option(name = "--basepackage", description = "Base package to scan entities")
	public String basepackage;

	@Option(name = "--revent", description = "Revision entity class")
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
