package com.wds.tools.envers.cli.support;

import io.airlift.command.Cli;
import io.airlift.command.Cli.CliBuilder;
import io.airlift.command.Help;

import java.util.HashSet;
import java.util.Set;

import com.wds.tools.envers.cli.support.command.InstallCommand;
import com.wds.tools.envers.cli.support.command.VersionCommand;

public class CommandLineBuilder {
	public static final CommandRunner build(final String[] args) {
		return new CommandRunner() {
			@Override
			public void run() {
				CommandLineContext.begin();
				ConcreteBuilder.build().parse(args).run();
				CommandLineContext.end();
			}
		};
	}

	public static interface CommandRunner {
		void run();
	}

	private static class ConcreteBuilder {
		public static Cli<Runnable> build() {
			Set<Class<? extends Runnable>> commands = new HashSet<Class<? extends Runnable>>();
			commands.add(Help.class);
			commands.add(VersionCommand.class);
			commands.add(InstallCommand.class);

			CliBuilder<Runnable> builder = Cli.<Runnable> builder(CommandLineContext.current().getCommandLineName())
					.withDescription("the stupid content tracker").withDefaultCommand(Help.class)
					.withCommands(commands);

			return builder.build();
		}
	}
}
