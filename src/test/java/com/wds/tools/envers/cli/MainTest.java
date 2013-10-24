package com.wds.tools.envers.cli;

import java.util.ArrayList;
import java.util.List;

public class MainTest {
	public static void main(String[] args) {
		List<String> params = new ArrayList<String>();

		params.add("install");
		params.add("--url=jdbc:h2:tmp/demo;MODE=MySQL;INIT=CREATE SCHEMA IF NOT EXISTS demo\\;SET SCHEMA demo;");
		params.add("--username=sa");
		params.add("--password=");
		params.add("--basepackage=com.wds.demo.sync");

		Main.main(params.toArray(new String[] {}));
	}
}
