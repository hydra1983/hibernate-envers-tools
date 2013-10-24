package com.wds.tools.envers.cli;

import com.wds.tools.envers.cli.support.CommandLineBuilder;

public class Main {
	public static void main(String[] args) {
		CommandLineBuilder.build(args).run();
	}
}